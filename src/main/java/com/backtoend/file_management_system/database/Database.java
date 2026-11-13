package com.backtoend.file_management_system.database;

import com.backtoend.file_management_system.MainApplication;
import com.backtoend.file_management_system.error_handling.IErrorCallback;
import com.backtoend.file_management_system.error_handling.OperationResult;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.logging.SourceLocation;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.chart.PieChart;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.*;

/// Primary database helper class. Manages connections and transactions,
/// keeps track of database status and initialization, does not perform
/// SQL queries.
/// @author Rogue049
public class Database {
    private static final AppLogger logger = AppLogger.getLogger(Database.class);
    /// JDBC path of the database file
    private final String dbLocation;
    /// Current database status
    private DbStatus status;
    private final ExecutorService readExecutor;
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

    // TODO: readExecutor *can* be multi-threaded if that is properly accounted for
    /// Constructor. Creates a new `Database` instance, defines the path to
    /// the .db file, and attempts to connect. If a connection can be established,
    /// its status will be set to `NOT_INITIALIZED`, and an initialization process
    /// is attempted. If the connection (or subsequent initialization attempt)
    /// fails, its status will be set to `FAILED`. Status will be set to `READY`
    /// if the initialization process is successful.
    /// @param path name of the database file
    public Database(String path) {
        Path dbPath = Path.of("data", path);
        this.dbLocation = "jdbc:sqlite:" + dbPath;
        logger.debug("Database is located at: " + this.dbLocation);
        this.readExecutor = Executors.newSingleThreadExecutor();
        this.writeExecutor = Executors.newSingleThreadExecutor();
    }

    public void initialize(IErrorCallback handle) throws SQLException {
        // Testing the connection
        logger.info("Database connection established; attempting initialization");
        this.status = DbStatus.NOT_INITIALIZED;
        DatabaseInitialization init = new DatabaseInitialization(this);
        CompletableFuture<OperationResult<Void>> result =
                CompletableFuture.supplyAsync(() -> {
                    try (Connection conn = connect()) {
                        return init.initiateDb(conn);
                    } catch (SQLException e) {
                        throw new CompletionException(e);
                    }
                }, writeExecutor);
        result.exceptionally(e -> {
            return OperationResult.failed(
                    null, null, new SourceLocation(
                            this.getClass().getName(),
                            "initialize"
                    ),
                    e
            );
        });
        result.thenAccept(operationResult -> {
            Platform.runLater(() -> {
                processInitResult(handle, operationResult);
            });
        });
    }

    public DbStatus getStatus() {
        return this.status;
    }

    /// Perform the given DB operation.\
    /// For multi-step insert queries, use {@link Database#withTransaction}
    /// instead.
    /// @param query a database operation to be executed
    /// @return the result of the database operation
    /// @param <T> the datatype returned by the database operation
    /// @throws SQLException if the given operation throws
    public <T> T withConnection(ISqlQuery<T> query) throws SQLException {
        try (Connection conn = this.connect()) {
            return query.execute(conn);
        }
    }

    /// Perform the given DB operation through a managed transaction. If
    /// any exception is encountered, a rollback of the transaction is
    /// initiated.\
    /// This method should be preferred over {@link Database#withConnection(ISqlQuery)}
    /// if any task to be executed either consists of multiple separate
    /// queries, such as a bulk-insert, and every step has to be un-done
    /// if any operation throws any exception.\
    /// It should *not* be used if the operation consists of one single
    /// query, or if the operation is a longer read.
    /// @param query a database operation to be executed
    /// @return the result of the database operation
    /// @param <T> the datatype returned by the database operation
    /// @throws SQLException if the given operation throws
    public <T> T withTransaction(ISqlQuery<T> query) throws SQLException {
        try (Connection conn = this.connect()) {
            conn.setAutoCommit(false);

            try {
                T result = query.execute(conn);
                conn.commit();
                return result;
            } catch (Exception e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        }
    }

    // region Interacting with executable services
    public void executeWrite(Runnable task) {
        writeExecutor.execute(task);
    }
    public void executeRead(Runnable task) {
        readExecutor.execute(task);
    }
    public void shutdown() {
        readExecutor.shutdown();
        writeExecutor.shutdown();
    }
    // endregion

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

    private void processInitResult(IErrorCallback callback, OperationResult<Void> result) {
        if (result.isSuccess()) {
            this.status = DbStatus.READY;
        } else {
            this.status = DbStatus.FAILED;
        }
        callback.handle(result);
    }
}
