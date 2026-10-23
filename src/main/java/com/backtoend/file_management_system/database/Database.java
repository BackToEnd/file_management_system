package com.backtoend.file_management_system.database;

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

    public Database(String path) {

        Path dbPath = Path.of("data", path);
        this.dbLocation = "jdbc:sqlite:" + dbPath;
        logger.debug("Identified DB path: " + this.dbLocation);
        // Test DB connection
        try (Connection conn = this.connect()) {
            logger.info("Successfully connected to DB");
        } catch (SQLException e) {
            logger.error("Unable to connect to DB: ", e);
        }
    }

    /**
     * Shortcut for creating a new DB connection
     * @return The created DB connection
     * @throws SQLException If the connection cannot be created
     */
    public Connection connect() throws SQLException {
        logger.debug("DB connection requested");
        return DriverManager.getConnection(this.dbLocation);
    }

    public <T> T withConnection(SqlWorker<T> work) throws SQLException {
        try (Connection conn = connect()) {
            return work.execute(conn);
        }
    }

    public <T> T withTransaction(SqlWorker<T> work) throws SQLException {
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
