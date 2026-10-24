package com.backtoend.file_management_system.database;

import com.backtoend.file_management_system.enums.ItemStatus;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.ItemRow;
import com.backtoend.file_management_system.model.ParentNode;

import java.sql.*;
import java.util.ArrayList;

/**
 * Database helper class responsible for retrieving any kinds of information from the DB
 */
public class DatabaseFetch {

    private static final AppLogger logger = AppLogger.getLogger(DatabaseFetch.class);
    // === Utility Methods === \\

    /**
     * Fetches the ID of a node with the given path
     * @param path Path to a file or directory
     * @param conn DB connection
     * @return The ID of the node with the specified path, or -1
     * @throws SQLException If the query cannot be executed for any reason
     */
    public static int getNodeId(String path, Connection conn) throws SQLException {
        String query = """
                SELECT id FROM node
                WHERE path = ?
                """;
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, path);
        ResultSet res = stmt.executeQuery();
        int id = res.getInt(1);
        if (res.wasNull()) {
            return -1;
        }
        return id;
    }

    /**
     * Checks whether the given NodeID also exists as a scan location
     * @param nodeId The ID of the node to be checked
     * @param conn DB connection
     * @return Whether the NodeID is also a scan location
     * @throws SQLException If the query cannot be executed for any reason
     */
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

    public static ArrayList<ItemRow> getAllParentItems(Connection conn) throws SQLException {
        String query = """
                SELECT * FROM node
                WHERE id IN (
                SELECT node_id FROM scan_location
                );
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

    public static ParentNode getParentNodeOf(ItemRow item, Connection conn) throws SQLException {
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
                return new ParentNode(
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
}