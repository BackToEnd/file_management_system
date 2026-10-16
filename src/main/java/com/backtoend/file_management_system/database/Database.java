package com.backtoend.file_management_system.database;

import com.backtoend.file_management_system.logging.AppLogger;

import java.sql.*;
import java.nio.file.Path;

public class Database {
    private final String dbLocation;
    private static final AppLogger logger = AppLogger.getLogger(Database.class);

    public Database(String path) {
        logger.debug("Instance created");

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
     * Shortcut to receive a connection to the DB
     * @return DB connection
     * @throws SQLException
     */
    public Connection connect() throws SQLException {
        logger.debug("DB connection requested");
        return DriverManager.getConnection(this.dbLocation);
    }

    /**
     * Checks whether the given column already exists in the given table<br>
     * Relevant for updating tables upon app updates, and for certain column constraints
     * @param table Name of the table
     * @param column Name of the column
     * @return whether the table already contains a column with that name
     * @throws SQLException
     */
    public boolean columnExists(String table, String column) throws SQLException {
        logger.info("Checking table {" + table + "} for column {" + column + "}");
        String test = "PRAGMA table_info(\""
                + table.replace("\"", "\"\"")
                + "\")";
        try (Connection conn = this.connect();
        Statement stmt = conn.createStatement();
        ResultSet result = stmt.executeQuery(test)) {
            logger.debug("Connection established, statement created, result obtained");
            while (result.next()) {
                if (column.equalsIgnoreCase(result.getString("name"))) {
                    logger.info("Column found");
                    return true;
                }
            }
        }
        logger.info("Column not found");
        return false;
    }
}
