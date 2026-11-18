package com.backtoend.file_management_system.database;

import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.IItemInformation;
import com.backtoend.file_management_system.model.ItemRow;
import com.backtoend.file_management_system.model.ScanLocation;
import com.backtoend.file_management_system.model.ScannedItem;

import java.sql.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DatabaseInsert {
    private static final AppLogger logger = AppLogger.getLogger(DatabaseInsert.class);

    public static void insertChildrenOf(Integer parentId, ArrayList<ScannedItem> children, HashMap<String, Integer> descendants, Connection conn)
            throws SQLException {
        logger.info(MessageFormat.format(
                "Attempting to format {0} children of parent with ID {1}", children.size(), parentId
        ));
        for (ScannedItem child : children) {
            Integer existingId = descendants.get(child.path());
            if (existingId != null) {
                ItemRow childItem = ItemRow.create(child, existingId, parentId);
                updateItem(existingId, childItem, conn);
            } else {
                insertChildOf(parentId, child, conn);
            }
        }
        logger.info("Successfully inserted all children");
    }
    public static int insertChildOf(Integer parentId, ScannedItem child, Connection conn) throws SQLException {
        String query = """
        INSERT INTO item(
                         name, path, display_name, file_type, last_modified, size, status, parent
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?);
        """;
        try (PreparedStatement stmt = conn.prepareStatement(
                query, PreparedStatement.RETURN_GENERATED_KEYS
        )) {
            stmt.setString(1, child.name());
            stmt.setString(2, child.path());
            stmt.setString(3, child.name());
            stmt.setString(4, child.fileType());
            stmt.setLong(5, child.lastModified());
            stmt.setDouble(6, child.size());
            stmt.setString(7, IItemInformation.ItemStatus.ACTIVE.toString());
            stmt.setObject(8, parentId, Types.INTEGER);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows != 1) {
                throw new SQLException(MessageFormat.format(
                        "Expected to insert one row, got {0} affected rows", affectedRows
                ));
            }
            ResultSet result = stmt.getGeneratedKeys();
            if (!result.next()) {
                throw new SQLException(
                        "Expected to receive ID of inserted item, but no ID was returned"
                );
            }
            int newId = result.getInt(1);
            if (result.wasNull()) {
                throw new SQLException(
                        "Expected to receive a valid database ID, but received NULL"
                );
            }
            return newId;
        }
    }

    public static void updateItems(
            int parentId, ArrayList<ScannedItem> children, HashMap<String, Integer> potentialDescendants, HashMap<String, Integer> dbDescendants,
            Connection conn
            ) throws SQLException {
        HashSet<String> seen = new HashSet<>();
        for (ScannedItem item : children) {
            String path = item.path();
            seen.add(path);
            Integer dbId = dbDescendants.get(path);
            Integer potentialId = potentialDescendants.get(path);
            if (dbId != null) {
                updateItem(ItemRow.create(item, dbId, parentId), conn);
            } else if (potentialId != null) {
                updateItem(ItemRow.create(item, potentialId, parentId), conn);
            } else {
                insertChildOf(parentId, item, conn);
            }
        }
        for (var entry : dbDescendants.entrySet()) {
            if (!seen.contains(entry.getKey())) {
                markDisabled(entry.getValue(), conn);
            }
        }
    }
    public static void updateItem(int id, ItemRow item, Connection conn) throws SQLException {
        String query = """
                UPDATE item
                SET name = ?,
                    path = ?,
                    display_name = ?,
                    file_type = ?,
                    last_modified = ?,
                    size = ?,
                    status = ?,
                    parent = ?
                WHERE id = ?;
                """;
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, item.name());
            stmt.setString(2, item.path());
            stmt.setString(3, item.displayName());
            stmt.setString(4, item.fileType());
            stmt.setLong(5, item.lastModified());
            stmt.setDouble(6, item.size());
            stmt.setString(7, item.status().toString());
            stmt.setObject(8, item.parentId(), Types.INTEGER);
            stmt.setInt(9, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected != 1) {
                throw new SQLException(
                        "Inserted one item, expected one affected row, got: " + rowsAffected
                );
            }
        }
    }
    public static void updateItem(ItemRow item, Connection conn) throws SQLException {
        updateItem(item.id(), item, conn);
    }

    public static void markDisabled(int id, Connection conn) throws SQLException {
        String query = """
                UPDATE item
                SET status = ?
                WHERE id = ?;
                """;
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, IItemInformation.ItemStatus.DISABLED.toString());
            stmt.setInt(2, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected != 1) {
                throw new SQLException(
                        "Updated status of one item, expected one affected row, got " + rowsAffected
                );
            }
        }
    }

    // TODO: This method does not update scan locations if their item has changed ID;
    //  items changing DB IDs should never be possible anyways
    public static void updateScanLocation(ScanLocation item, Connection conn) throws SQLException {
        String query = """
                UPDATE scan_location
                SET scan_depth = ?,
                    update_mode = ?,
                    scan_source = ?,
                    generation = ?,
                    status = ?
                WHERE item_id = ?
                """;
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, item.scanDepth());
            stmt.setString(2, item.updateMode().toString());
            stmt.setString(3, item.scanSource().toString());
            stmt.setInt(4, item.generation());
            stmt.setString(5, item.status().toString());
            stmt.setInt(6, item.item().id());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected != 1) {
                throw new SQLException(
                        "Updated one row, expected one affected row, got: " + rowsAffected
                );
            }
            updateItem(item.item(), conn);
        }
    }

    public static void createScanLocation(int itemId, ScanLocation scanLocation, Connection conn) throws SQLException {
        String query = """
                INSERT INTO scan_location (
                                           item_id, scan_depth, update_mode, scan_source, generation,status
                ) VALUES (?, ?, ?, ?, ?, ?);
                """;
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, itemId);
            stmt.setInt(2, scanLocation.scanDepth());
            stmt.setString(3, scanLocation.updateMode().toString());
            stmt.setString(4, scanLocation.scanSource().toString());
            stmt.setInt(5, scanLocation.generation());
            stmt.setString(6, scanLocation.status().toString());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected != 1) {
                throw new SQLException(
                        "Expected to insert one scan location, expected one affected row, got: " + rowsAffected
                );
            }
        }
    }
}
