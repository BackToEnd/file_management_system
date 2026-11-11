package com.backtoend.file_management_system.database;

import com.backtoend.file_management_system.logging.AppLogger;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
        Path dbPath = Path.of("data", path);
        this.dbLocation = "jdbc:sqlite:" + dbPath;
        logger.debug("Database is located at: " + this.dbLocation);
        // Testing the connection
        try (Connection conn = this.connect()) {
            logger.info("Database connection established");
            this.status = DbStatus.NOT_INITIALIZED;
        } catch (SQLException e) {
            logger.error("Unable to connect to database", e);
            this.status = DbStatus.FAILED;
        }
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
}
