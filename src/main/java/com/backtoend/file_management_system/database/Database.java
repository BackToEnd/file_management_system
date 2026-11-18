package com.backtoend.file_management_system.database;

import com.backtoend.file_management_system.MainApplication;
import com.backtoend.file_management_system.error_handling.ErrorHandler;
import com.backtoend.file_management_system.error_handling.IErrorCallback;
import com.backtoend.file_management_system.error_handling.OperationResult;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.logging.SourceLocation;
import com.backtoend.file_management_system.util.IExecutorService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.chart.PieChart;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.*;

/// Primary database helper class. Manages connections and transactions,
/// keeps track of database status and initialization, does not perform
/// SQL queries.
/// @author Rogue049
public class Database implements IExecutorService {
    /// Logger instance
    private static final AppLogger logger = AppLogger.getLogger(Database.class);
    /// JDBC path of the database file
    private final String dbLocation;
    /// Current database status
    private volatile DbStatus status;
    /// Read executor service; bi-threaded by default, allowing two read operations at the same time.
    private final ExecutorService readExecutor;
    /// Write executor service; single-threaded since SQLite can only perform one write at a time
    private final ExecutorService writeExecutor;

    /// Enum for all possible states the DB can have;
    /// - `NOT_INITIALIZED`: database initialization has not yet been requested
    /// - `INITIALIZING`: database initialization is currently in progress
    /// - `READY`: database initialization has concluded successfully
    /// - `FAILED`: database could not be initialized
    public enum DbStatus {
        NOT_INITIALIZED,
        INITIALIZING,
        READY,
        FAILED
    }

    /// Constructor. Creates a new `Database` instance, defines the path to
    /// the .db file, and attempts to connect. If a connection can be established,
    /// its status will be set to `NOT_INITIALIZED`, and an initialization process
    /// is attempted. If the connection (or subsequent initialization attempt)
    /// fails, its status will be set to `FAILED`. Status will be set to `READY`
    /// if the initialization process is successful.
    /// @param path name of the database file
    public Database(String path) {
        // Creating data folder if none exists
        Path dbPath = Path.of("data", path);
        this.dbLocation = "jdbc:sqlite:" + dbPath;
        logger.debug("Database is located at: " + this.dbLocation);
        this.readExecutor = Executors.newFixedThreadPool(2);
        this.writeExecutor = Executors.newSingleThreadExecutor();
    }

    /// Asynchronously initializes the database through the write executor; this operation should be performed
    /// at the start of the application lifecycle.
    /// @param handle a callback that defines what to do with the result of this operation
    /// @throws SQLException if any SQL operation fails
    /// @throws IOException if the `data` folder at the root of this directory cannot be created
    public void initialize(IErrorCallback handle) throws SQLException, IOException {
        // Creating DB directory if it does not yet exist
        Path dbFolder = Path.of("data");
        Files.createDirectories(dbFolder);
        // Testing the connection
        logger.info("Database connection established; attempting initialization");
        this.status = DbStatus.INITIALIZING;
        DatabaseInitialization init = new DatabaseInitialization(this);
        CompletableFuture<OperationResult<Void>> result =
                CompletableFuture.supplyAsync(() -> {
                    try (Connection conn = connect()) {
                        return init.initiateDb(conn);
                    } catch (SQLException e) {
                        throw new CompletionException(e);
                    }
                }, writeExecutor);
        result.exceptionally(e -> OperationResult.failed(
                null, null, new SourceLocation(
                        this.getClass().getName(),
                        "initialize"
                ),
                e
        ));
        result.thenAccept(operationResult -> Platform.runLater(() -> processInitResult(handle, operationResult)));
    }

    /// Checks the current status of the DB
    /// @return database status
    public DbStatus getStatus() {
        return this.status;
    }

    /// Shortcut to check if the DB is ready
    /// @return `true` if the status is `READY`, otherwise `false`
    public boolean ready() { return this.status == DbStatus.READY; }

    /// Shortcut for executing a provided database operation with a database connection;
    /// this method has been made private because {@link #writeExecutor} and {@link #readExecutor}
    /// exist now. All database operations are to be performed through the corresponding methods.
    /// @see #read(ISqlQuery)
    /// @see #writeWithConnection(ISqlQuery)
    /// @see #writeWithTransaction(ISqlQuery)
    /// @param query database operation to be performed
    /// @return the result of the performed operation
    /// @param <T> datatype returned by the operation
    /// @throws SQLException if any database operation fails
    private <T> T withConnection(ISqlQuery<T> query) throws SQLException {
        try (Connection conn = this.connect()) {
            return query.execute(conn);
        }
    }

    /// Shortcut for executing a provided database operation with a database transaction;
    /// this method has been made private because {@link #writeExecutor} and {@link #readExecutor}
    /// exist now. All database operations are to be performed through the corresponding methods.
    /// @see #read(ISqlQuery)
    /// @see #writeWithConnection(ISqlQuery)
    /// @see #writeWithTransaction(ISqlQuery)
    /// @param query database operation to be performed
    /// @return the result of the performed operation
    /// @param <T> datatype returned by the operation
    /// @throws SQLException if any database operation fails
    private <T> T withTransaction(ISqlQuery<T> query) throws SQLException {
        try (Connection conn = this.connect()) {
            logger.debug("Connection with transaction established");
            conn.setAutoCommit(false);
            try {
                logger.debug("Attempting to execute query");
                T result = query.execute(conn);
                conn.commit();
                logger.debug("Committed transaction");
                return result;
            } catch (Exception e) {
                logger.error("Caught exception:", e);
                try {
                    conn.rollback();
                    logger.error("Transaction rolled back");
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        }
    }

    // region Interacting with executable services

    /// Asynchronously performs a read operation through the corresponding executor service.\
    /// Read operations are performed without transactions. Do not use this method for anything other than
    /// read operations.
    /// @param query a database operation to be performed
    /// @return a completable future of the result returned by the operation
    /// @param <T> datatype returned by the operation
    public <T> CompletableFuture<T> read(ISqlQuery<T> query) {
        logger.debug("Queueing read operation");
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.withConnection(query);
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }, readExecutor);
    }

    /// Asynchronously performs a write operation through the corresponding executor service.\
    /// This method performs the operation through a connection, not transaction. Do not use this method for
    /// read operations, and do not use it for bulk-inserts where one failure means discarding the rest of the
    /// write as well.
    /// @param query a database operation to be performed
    /// @return a completable future of the result returned by the operation
    /// @param <T> datatype returned by the operation
    public <T> CompletableFuture<T> writeWithConnection(ISqlQuery<T> query) {
        logger.debug("Queueing write-with-connection operation");
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.withConnection(query);
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }, writeExecutor);
    }

    /// Asynchronously performs a write operation through the corresponding executor service.\
    /// This method performs the operation through a transaction. Any failure throughout the transaction
    /// causes all prior operations of the same transaction to be rolled back. Do not use this method for read
    /// operations, and do not use it for single inserts.
    /// @param query a database operation to be performed
    /// @return a completable future of the result returned by the operation
    /// @param <T> datatype returned by the operation
    public <T> CompletableFuture<T> writeWithTransaction(ISqlQuery<T> query) {
        logger.debug("Queueing write-with-transaction operation");
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Processing async write-with-transaction operation");
                return this.withTransaction(query);
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }, writeExecutor);
    }

    /// Shuts down read and write executor services in order to allow the application to exit.
    /// WARNING: There is currently no safety net in place for application termination during ongoing reads or writes.
    public void shutdown() {
        readExecutor.shutdown();
        writeExecutor.shutdown();
    }
    // endregion

    /// Asynchronously waits for 10s to check if read and write executors have terminated (10s per
    /// executor).
    /// @return `true` if both executor services have terminated, otherwise `false`.
    public boolean awaitTermination() {
        try {
            boolean readTerminated = readExecutor.awaitTermination(10, TimeUnit.SECONDS);
            boolean writeTerminated = writeExecutor.awaitTermination(10, TimeUnit.SECONDS);
            logger.debug(
                    "Read executor status: "
                    + ((readTerminated) ? "Terminated" : "Busy")
            );
            logger.debug(
                    "Write executor status: "
                            + ((writeTerminated) ? "Terminated" : "Busy")
            );
            return readTerminated && writeTerminated;
        } catch (InterruptedException e) {
            logger.error(
                    "Unable to shut down database executor services:",
                    "awaitTermination",
                    this.getClass().getName(),
                    e
            );
            Thread.currentThread().interrupt();
            return false;
        }
    }
    // === Private Helper Methods === \\

    /// Shortcut to create a database connection. This method should not
    /// be used by other classes.
    /// @return a database connection
    /// @throws SQLException if the connection cannot be established
    private Connection connect() throws SQLException {
        if (this.status == DbStatus.FAILED) {
            throw new SQLException(
                    "A connection to the database was requested, but the database could not be reached"
            );
        }
        if (this.status != DbStatus.READY) {
            logger.info("A connection to the database was requested before the database had been initialized");
        }
        return DriverManager.getConnection(this.dbLocation);
    }

    /// Final step in the initialization process. Sets the status to `READY` if the operation was
    /// successful, otherwise sets the status to `FAILED`.
    /// @param callback callback to process the result of the initialization
    /// @param result result of the initialization process
    private void processInitResult(IErrorCallback callback, OperationResult<Void> result) {
        if (result.isSuccess()) {
            this.status = DbStatus.READY;
        } else {
            this.status = DbStatus.FAILED;
        }
        callback.handle(result);
    }
}
