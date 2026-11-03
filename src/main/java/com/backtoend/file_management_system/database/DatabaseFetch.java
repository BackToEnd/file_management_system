package com.backtoend.file_management_system.database;

import com.backtoend.file_management_system.enums.ItemStatus;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.ItemRow;
import com.backtoend.file_management_system.model.ScanLocation;

import java.sql.*;
import java.util.ArrayList;

/// Database helper class that fetches data from the DB
/// @author Rogue049
public class DatabaseFetch {

    private static final AppLogger logger = AppLogger.getLogger(DatabaseFetch.class);
    // === Utility Methods === \\

    // TODO: Potentially fixed the problem I thought this method had
    /// Attempts to get a node ID for the specified path. Can properly deal with a path that does not exist in the DB yet.
    /// @param path A directory path
    /// @param conn Database connection
    /// @return The retrieved node ID if one was found, or -1
    /// @throws SQLException If any database operation fails
    public static int getNodeId(String path, Connection conn) throws SQLException {
        int id = -1;
        String query = """
                SELECT id FROM node
                WHERE path = ?
                """;
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, path);
        ResultSet res = stmt.executeQuery();
        if (res.next()) {
            id = res.getInt(1);
            if (res.wasNull()) {
                id = -1;
            }
        }
        return id;
    }

    /// Checks if a node also exists as a scan location
    /// @param nodeId The ID of the node to be checked
    /// @param conn Database connection
    /// @return `true` if the node is a scan location, otherwise `false`
    /// @throws SQLException If any database operation fails
    public static boolean isScanLocation(int nodeId, Connection conn) throws SQLException {
        String query = """
                SELECT node_id FROM scan_location
                WHERE node_id = ?
                """;
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, nodeId);
        ResultSet res = stmt.executeQuery();
        return res.next();
    }

    /// Fetches all children of a parent node
    /// @param parentId The ID of the parent node
    /// @param conn Database connection
    /// @return A list of all retrieved child items
    /// @throws SQLException If any database operation fails
    public static ArrayList<ItemRow> getChildrenOf(int parentId, Connection conn) throws SQLException {
        String query = """
                SELECT *, CASE
                WHEN id IN (
                SELECT node_id FROM scan_location)
                THEN 1
                ELSE 0
                END AS has_children
                FROM node
                WHERE parent = ?;
                """;
        ArrayList<ItemRow> list = new ArrayList<>();
        logger.debug("Fetching children for node with ID: " + parentId);
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, parentId);
            ResultSet result = stmt.executeQuery();
            logger.debug("Result set obtained");
            while (result.next()) {
                list.add(new ItemRow(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("path"),
                        result.getString("display_name"),
                        result.getString("file_type"),
                        result.getLong("last_modified"),
                        result.getDouble("size"),
                        ItemStatus.valueOf(result.getString("status")),
                        parentId,
                        result.getBoolean("has_children")
                ));
            }
            return list;
        }
    }

    /// Fetches all root parent items that have no parents of their own
    /// @param conn Database connection
    /// @return A list of all root parent items
    /// @throws SQLException If any database operation fails
    public static ArrayList<ItemRow> getAllParentItems(Connection conn) throws SQLException {
        String query = """
                SELECT * FROM node
                WHERE id IN (
                SELECT node_id FROM scan_location
                );
                AND parent IS NULL
                """;
        ArrayList<ItemRow> list = new ArrayList<>();
        logger.debug("Fetching all parent items");
        try (Statement stmt = conn.createStatement()) {
            ResultSet result = stmt.executeQuery(query);
            logger.debug("Result set obtained");
            while (result.next()) {
                int id = result.getInt("id");
                String name = result.getString("name");
                String path = result.getString("path");
                String displayName = result.getString("display_name");
                String fileType = result.getString("file_type");
                long lastModified = result.getLong("last_modified");
                double size = result.getDouble("size");
                ItemStatus status = ItemStatus.valueOf(result.getString("status"));
                Integer parent = result.getInt("parent");
                if (result.wasNull()) {
                    parent = null;
                }
                list.add(new ItemRow(
                        id, name, path, displayName, fileType, lastModified, size, status, parent, true
                ));
            }
            return list;
        }
    }

    /// Fetches the scan location of the provided item
    /// @param item A parent item that ideally exists in the DB
    /// @param conn Database connection
    /// @return The retrieved scan location
    /// @throws SQLException Either if any database operation fails, or if the
    /// passed item is not a parent with a scan location
    public static ScanLocation getScanLocationOf(ItemRow item, Connection conn) throws SQLException {
        String query = """
                SELECT * FROM scan_location
                WHERE node_id = ?;
                """;
        logger.debug("Fetching scan location of parent node: " + item.id());
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, item.id());
            ResultSet result = stmt.executeQuery();
            logger.debug("Result set obtained");
            if (result.next()) {
                return new ScanLocation(
                        item,
                        result.getInt("scan_depth"),
                        result.getString("update_mode"),
                        result.getString("scan_source"),
                        result.getInt("generation"),
                        ItemStatus.valueOf(
                                result.getString("status")
                        )
                );
            }
            throw new SQLException(
                    "Requested scan location was not returned by the DB"
            );
        }
    }

    /// Fetches all information about an item based on its ID\
    /// Primarily used when inserting new parents into the overview tree
    /// @param id The ID of the item
    /// @param conn Database connection
    /// @return An item row containing all fetched information
    /// @throws SQLException If any database operation fails
    public static ItemRow getInfo(int id, Connection conn) throws SQLException {
        String query = """
                SELECT *, CASE
                WHEN id IN (
                SELECT node_id FROM scan_location)
                THEN 1
                ELSE 0
                END AS has_children
                FROM node
                WHERE id = ?;
                """;
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                String name = result.getString("name");
                String path = result.getString("path");
                String displayName = result.getString("display_name");
                String fileType = result.getString("file_type");
                long lastModified = result.getLong("last_modified");
                double size = result.getDouble("size");
                ItemStatus status = ItemStatus.valueOf(result.getString("status"));
                Integer parent = result.getInt("parent");
                if (result.wasNull()) {
                    parent = null;
                }
                boolean hasChildren = result.getBoolean("has_children");
                return new ItemRow(
                        id, name, path, displayName, fileType, lastModified, size, status, parent, hasChildren
                );
            }
            throw new SQLException(
                    "Requested item could not be fetched"
            );
        }
    }
}