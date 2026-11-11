package com.backtoend.file_management_system.database;

import com.backtoend.file_management_system.enums.DbStatus;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.service.SqlWorker;

import java.sql.*;
import java.nio.file.Path;

// TODO: Do some sort of error-handling if the initial DB connection fails

/// Database helper class. One instance is created at each app start, tracking the DB location and initialization status.
/// @author Rogue049
public class Database {
    /// JDBC path to the DB; `jdbc:sqlite:[dir]`
    private final String dbLocation;
    /// DB initialization status
    private DbStatus status;
    private static final AppLogger logger = AppLogger.getLogger(Database.class);

    /// Defines the DB location based on the given path and attempts an initial connection.
    /// If the connection attempt succeeds, DB status will be set to `NOT_INITIALIZED`.
    /// If the connection attempt fails, DB status will be set to `FAILED`.
    /// @param path The path to the .db file. Start location is the root folder of this application.
    public Database(String path) {
        Path dbPath = Path.of("data", path);
        this.dbLocation = "jdbc:sqlite:" + dbPath;
        logger.debug("Identified DB path: " + this.dbLocation);
        // Test DB connection
        try (Connection conn = this.connect()) {
            logger.info("Successfully connected to DB");
            this.status = DbStatus.NOT_INITIALIZED;
        } catch (SQLException e) {
            logger.error("Unable to connect to DB: ", e);
            this.status = DbStatus.FAILED;
        }
    }

    // Getters, Setters
    public void setStatus(DbStatus status) {
        this.status = status;
    }
    public DbStatus getStatus() {
        return this.status;
    }


    ///Shortcut for creating a DB connection
    /// @return DB connection
    /// @throws SQLException If the connection cannot be established
    public Connection connect() throws SQLException {
        if (this.status == DbStatus.FAILED) {
            logger.warning("DB connection requested, but the connection could not be established");
            throw new SQLException(
                    "Unable to connect to database"
            );
        }
        if (this.status != DbStatus.READY) {
            logger.info("DB connection requested, but DB has not yet been initialized");
        }
        return DriverManager.getConnection(this.dbLocation);
    }

    /// Performs a DB operation through a regular connection. Will print a warning to the logs if this
    /// method is called before the DB has been initialized.
    /// @param work The database operation to execute
    /// @return The result of the database operation
    /// @param <T> The return type of the database operation
    /// @throws SQLException Either if the DB status is `FAILED` while this method is called,
    /// or if the database operation throws an SQLException
    public <T> T withConnection(SqlWorker<T> work) throws SQLException {
        if (this.status == DbStatus.FAILED) {
            logger.warning("Attempting DB operation, but DB connection could not be established");
            throw new SQLException(
                    "Unable to connect to database"
            );
        }
        if (this.status != DbStatus.READY) {
            logger.info("Attempting DB operation, but DB has not yet been initialized");
        }
        try (Connection conn = connect()) {
            return work.execute(conn);
        }
    }

    /// Performs a DB operation through a managed transaction. Will print a warning to the logs if this
    /// method is called before the DB has been initialized.\
    /// If an SQLException, RuntimeException, or Error is encountered, a rollback of the transaction
    /// will be initiated.
    /// @param work The database operation to execute
    /// @return The result of the database operation
    /// @param <T> The return type of the database operation
    /// @throws SQLException Either if the DB status is `FAILED` while this method is called,
    /// or if the database operation, commit, or rollback (if one is initiated) throws an SQLException
    public <T> T withTransaction(SqlWorker<T> work) throws SQLException {
        if (this.status == DbStatus.FAILED) {
            logger.warning("Attempting DB transaction, but DB connection could not be established");
            throw new SQLException(
                    "Unable to connect to database"
            );
        }
        if (this.status != DbStatus.READY) {
            logger.info("Attempting DB transaction, but DB has not yet been initialized");
        }
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);

            try {
                T result = work.execute(conn);
                conn.commit();
                return result;
            } catch (SQLException | RuntimeException | Error e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        }
    }
}
