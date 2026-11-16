package com.backtoend.file_management_system.database;

import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.IItemInformation;
import com.backtoend.file_management_system.model.ItemRow;
import com.backtoend.file_management_system.model.ScanLocation;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseSelect {
    private static final AppLogger logger = AppLogger.getLogger(DatabaseSelect.class);

    public static int getItemId(String path, Connection conn) throws SQLException {
        int id = -1;
        String query = """
                SELECT id FROM item
                WHERE path = ?;
                """;
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, path);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                id = result.getInt("id");
                if (result.wasNull()) {
                    throw new SQLException(
                            "Attempting to fetch the PK of a record returned NULL"
                    );
                }
            }
            return id;
        }
    }

    public static boolean isScanLocation(int itemId, Connection conn) throws SQLException {
        String query = """
                SELECT item_id FROM scan_location
                WHERE item_id = ?;
                """;
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, itemId);
            ResultSet result = stmt.executeQuery();
            return result.next();
        }
    }

    public static ArrayList<ItemRow> getChildrenOf(int parentId, Connection conn) throws SQLException {
        String query = """
                SELECT *, CASE
                WHEN id IN (
                SELECT DISTINCT parent
                FROM item
                ) THEN 1
                ELSE 0
                END AS has_children
                FROM item
                WHERE parent = ?;
                """;
        ArrayList<ItemRow> list = new ArrayList<>();
        logger.debug("Fetching children of item with ID: " + parentId);
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, parentId);
            ResultSet result = stmt.executeQuery();
            logger.debug("Result set obtained");
            while (result.next()) {
                list.add(getItemRow(result));
            }
            return list;
        }
    }

    public static ArrayList<ScanLocation> getBaseItems(Connection conn) throws SQLException {
        String query = """
                SELECT *, CASE
                WHEN id IN (
                    SELECT DISTINCT parent
                    FROM item
                ) THEN 1
                ELSE 0
                END AS has_children
                FROM item
                INNER JOIN scan_location ON item.id = scan_location.item_id
                WHERE item.parent IS NULL;
                """;
        ArrayList<ScanLocation> list = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
        ResultSet result = stmt.executeQuery(query)) {
            logger.debug("Result set obtained");
            while (result.next()) {
                ItemRow item = getItemRow(result);
                logger.trace("Obtained info: " + item.display());
                list.add(getScanLocation(item, result));
            }
            return list;
        }
    }

    public static ScanLocation getScanLocationOf(ItemRow item, Connection conn) throws SQLException {
        String query = """
                SELECT * FROM scan_location
                WHERE item_id = ?;
                """;
        logger.debug("Fetching scan location of: " + item.display());
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, item.id());
            ResultSet result = stmt.executeQuery();
            logger.debug("Result set obtained");
            if (result.next()) {
                return getScanLocation(item, result);
            }
            throw new SQLException(
                    "A requested scan location was not returned by the database"
            );
        }
    }

    public static IItemInformation getInfo(int id, Connection conn) throws SQLException {
        String itemQuery = """
                SELECT *, CASE
                WHEN id IN (
                    SELECT DISTINCT parent
                    FROM item
                ) THEN 1
                ELSE 0
                END AS has_children
                FROM item
                WHERE id = ?;
                """;
        String scanLocationQuery = """
                SELECT * FROM scan_location
                WHERE item_id = ?;
                """;
        try (PreparedStatement itemStmt = conn.prepareStatement(itemQuery);
        PreparedStatement scanLocationStmt = conn.prepareStatement(scanLocationQuery)) {
            ResultSet itemResult = itemStmt.executeQuery();
            ResultSet scanLocationResult = scanLocationStmt.executeQuery();
            if (itemResult.next()) {
                ItemRow item = getItemRow(itemResult);
                if (scanLocationResult.next()) {
                    return getScanLocation(item, scanLocationResult);
                }
                return item;
            }
            throw new SQLException(
                    "Database returned no item with ID '" + id + "'"
            );
        }
    }

    // === Helper Methods === \\
    private static ItemRow getItemRow(ResultSet result) throws SQLException {
        int id = result.getInt("id");
        String name = result.getString("name");
        String path = result.getString("path");
        String displayName = result.getString("display_name");
        String fileType = result.getString("file_type");
        long lastModified = result.getLong("last_modified");
        double size = result.getDouble("size");
        IItemInformation.ItemStatus status = IItemInformation.ItemStatus.valueOf(result.getString("status"));
        Integer parent = result.getInt("parent");
        boolean hasChildren = result.getBoolean("has_children");
        if (result.wasNull()) {
            parent = null;
        }
        return new ItemRow(
                id, name, path, displayName, fileType, lastModified, size, status, parent, hasChildren
        );
    }

    private static ScanLocation getScanLocation(ItemRow item, ResultSet result) throws SQLException {
        int scanDepth = result.getInt("scan_depth");
        IItemInformation.UpdateMode updateMode = IItemInformation.UpdateMode.valueOf(result.getString("update_mode"));
        IItemInformation.ScanSource scanSource = IItemInformation.ScanSource.valueOf(result.getString("scan_source"));
        int generation = result.getInt("generation");
        IItemInformation.ItemStatus status = IItemInformation.ItemStatus.valueOf(result.getString("status"));
        return new ScanLocation(item, scanDepth, updateMode, scanSource, generation, status);
    }
}
