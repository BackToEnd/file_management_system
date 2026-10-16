package com.backtoend.file_management_system.database;

import com.backtoend.file_management_system.Context;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.Item;
import com.backtoend.file_management_system.model.ItemHandler;
import com.backtoend.file_management_system.service.DirectoryHandler;

import java.io.File;
import java.sql.*;

public class DatabaseInsert {
    private static final AppLogger logger = AppLogger.getLogger(DatabaseInsert.class);

    // All required prepared statements
    private static final String insertNode = """
            INSERT INTO node (name, path, file_type, status, parent)
            VALUES (?, ?, ?, ?, ?);
            """;
    private static final String insertParentNode = """
            INSERT INTO node (name, path, file_type, status)
            VALUES (?, ?, ?, ?);
            """;
    private static final String insertParent = """
            INSERT INTO scan_location
            (node_id, scan_depth, update_mode, scan_source, generation, status)
            VALUES (?, ?, ?, ?, ?);
            """;
    private static final String insertCategory = """
            INSERT INTO category (name)
            VALUES (?);
            """;
    private static final String insertMetaType = """
            INSERT INTO metadata_type
            (name, allow_multiple, category)
            VALUES (?, ?, ?);
            """;
    private static final String insertMetaValue = """
            INSERT INTO metadata_value (type, value)
            VALUES (?, ?);
            """;
    private static final String insertNodeMetadata = """
            INSERT INTO node_metadata (node_id, metadata)
            VALUES (?, ?);
            """;
    // === Utility Methods === \\

    /**
     * Insert items from an ItemHandler into the DB
     * @param items ItemHandler with items to insert
     * @param db DB object
     * @return whether the operation was successful
     */
    public static boolean insertItems(ItemHandler items, Database db) {
        logger.info("Attempting to insert items into DB");
        String insertInto = """
                INSERT INTO node (path, name, file_type)
                VALUES (?, ?, ?);
                """;
        try (Connection conn = db.connect();
             PreparedStatement stmt = conn.prepareStatement(insertInto);) {
            logger.debug("Successfully connected to DB and prepared statement");
            for (Item i : items.getItemList()) {
                String fileType = (i.getIsDir()) ? "Folder" : "File";
                stmt.setString(1, i.getPath());
                stmt.setString(2, i.getDisplayName());
                stmt.setString(3, fileType);
                stmt.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            logger.error("Unable to insert into DB:", e);
            return false;
        }
    }

    // TODO: Ensure that duplicate parent nodes cannot be inserted!
    public static boolean insertNewSet(ItemHandler items, Database db) {
        logger.info("Attempting to insert new item set");
        int parentId = insertParentOnly(items.getPath(), db);
        if (parentId == -1) {
            logger.warning("Unable to insert parent item");
            return false;
        }
        try (Connection conn = db.connect();
        PreparedStatement stmtParent = conn.prepareStatement(DatabaseInsert.insertParent);
        PreparedStatement stmtNode = conn.prepareStatement(DatabaseInsert.insertNode);
        ) {
            // Inserting scan location
            stmtParent.setInt(1, parentId);
            stmtParent.setInt(2, 0);
            stmtParent.setString(3, "NONE");
            stmtParent.setString(4, "USER_SELECTION");
            stmtParent.setInt(5, 0);
            stmtParent.setString(6, "Active");
            stmtParent.executeUpdate();

            // Inserting child items
            for (Item i : items.getItemList()) {
                stmtNode.setString(1, i.getDisplayName());
                stmtNode.setString(2, i.getPath());
                stmtNode.setString(3,
                        (i.getIsDir()) ? "Folder" : "File");
                stmtNode.setString(4, "Active");
                stmtNode.setInt(5, parentId);
                stmtNode.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            logger.error("Unable to insert into DB: ", e);
            return false;
        }
    }

    private static int insertParentOnly(String path, Database db) {
        logger.debug("Attempting to insert parent item: " + path);
        File f = new File(path);
        String name = f.getAbsoluteFile().getName();
        boolean isDir = DirectoryHandler.isDir(path);
        try (Connection conn = db.connect();
        PreparedStatement stmt = conn.prepareStatement(
                DatabaseInsert.insertParentNode,
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setString(2, path);
            stmt.setString(3, (isDir) ? "Folder" : "File");
            stmt.setString(4, "Active");
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new SQLException("Inserted parent node did not return a generated ID");
        } catch (SQLException e) {
            logger.error("Unable to insert parent node: ", e);
            return -1;
        }
    }
}
