package com.backtoend.file_management_system.database;

import com.backtoend.file_management_system.enums.ParentExistence;
import com.backtoend.file_management_system.logging.AppLogger;

import java.sql.*;

/// Database helper class that performs DB operations that neither qualify as
/// inserts nor selects.
/// @author Rogue049
public class DatabaseCheck {
    private static final AppLogger logger = AppLogger.getLogger(DatabaseCheck.class);

    /// Checks if a node with the given path already exists in the DB, either as regular node, or as scan location
    /// @param path The path to be checked
    /// @param conn A database connection
    /// @return `MISSING` if the path does not exist in the DB yet, `IS_NODE` if the path already exists
    /// as a node, or `IS_SCAN_LOCATION` if the path exists as node and scan location
    /// @throws SQLException If any database operation fails
    public static ParentExistence doesParentExist(String path, Connection conn) throws SQLException {
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

    // TODO: Use `.withConnection` and pass a connection rather than a DB instance as parameter
    /// Checks if the specified table already has a column with the specified name
    /// @param table The name of the table
    /// @param column The name of the column
    /// @param db Database instance
    /// @return `true` if the specified table already contains a column with the specified name, `false` otherwise
    /// @throws SQLException If any database operation fails
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
