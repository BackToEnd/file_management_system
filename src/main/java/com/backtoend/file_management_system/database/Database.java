package com.backtoend.file_management_system.database;

import com.backtoend.file_management_system.enums.DbStatus;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.service.SqlWorker;

import java.sql.*;
import java.nio.file.Path;

/**
 * Database helper class with a shortcut to the DB connection<br>
 * Not responsible for any kinds of DB operations such as SELECT, INSERT, or DB initialization
 */
public class Database {
    private final String dbLocation;
    private static final AppLogger logger = AppLogger.getLogger(Database.class);
    private DbStatus status;

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

    /**
     * Shortcut for creating a new DB connection
     * @return The created DB connection
     * @throws SQLException If the connection cannot be created
     */
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
