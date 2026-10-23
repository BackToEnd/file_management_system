package com.backtoend.file_management_system.database;

import com.backtoend.file_management_system.enums.ParentExistence;
import com.backtoend.file_management_system.logging.AppLogger;

import java.sql.*;

/**
 * Utility DB helper class that does everything that isn't considered a fetch or insert per se
 */
public class DatabaseCheck {
    private static final AppLogger logger = AppLogger.getLogger(DatabaseCheck.class);

public static ParentExistence doesParentExist(String path, Database db) throws SQLException {
        Connection conn = db.connect();
        logger.debug("Checking if '" + path + "' exists in DB");
        int parentId = DatabaseFetch.getNodeId(path, conn);
        if (parentId == -1) {
            logger.debug("Path is not a node");
            return ParentExistence.MISSING;
        }
        if (DatabaseFetch.isScanLocation(parentId, conn)) {
            logger.debug("Path is a scan location");
            return ParentExistence.IS_SCAN_LOCATION;
        }
        logger.debug("Path is a node, but not a scan location");
        return ParentExistence.IS_NODE;
    }

    /**
     * Check if the given column exists in the given table;<br>
     * Used during initialization to add missing columns only if necessary
     * in order to prevent SQLExceptions
     * @param table The table
     * @param column The column
     * @param db Database instance
     * @return Whether the given table contains a column with the given name
     * @throws SQLException In case the connection to the DB cannot be established, or the given table does not exist
     */
    public static boolean columnExists(String table, String column, Database db) throws SQLException {
        logger.info("Checking table {" + table + "} for column {" + column + "}");
        // The PRAGMA statement required to retrieve information about the specified table
        String test = "PRAGMA table_info(\""
                + table.replace("\"", "\"\"")
                + "\")";

        // Try-with-resource block to auto-close Connection, Statement, and ResultSet
        try (Connection conn = db.connect();
             Statement stmt = conn.createStatement();
             ResultSet result = stmt.executeQuery(test)) {
            logger.debug("Connection established, statement created, result obtained");
            // Iterating through the result set to check if the specified column name is mentioned
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
