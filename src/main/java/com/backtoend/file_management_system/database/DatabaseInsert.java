package com.backtoend.file_management_system.database;

import com.backtoend.file_management_system.enums.OperationStatus;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.ItemRow;
import com.backtoend.file_management_system.model.ScanLocation;
import com.backtoend.file_management_system.util.FeedbackMsg;
import com.backtoend.file_management_system.util.OperationResult;
import java.sql.*;
import java.util.ArrayList;

/// Database helper class that inserts data into the DB
/// @author Rogue049
public class DatabaseInsert {
    private static final AppLogger logger = AppLogger.getLogger(DatabaseInsert.class);

    // TODO: This method is NOT yet equipped to deal with parents that are child items as well
    /// Inserts a newly scanned directory into the DB.\
    /// This method is equipped to adequately deal with a parent node that already exists in the DB
    /// as node and as scan location.
    /// @param parent Scan location of the parent item, containing an item row with all parent info
    /// @param children List of child items
    /// @param conn Database connection
    /// @return An operation result with status and messages; status will be `SUCCESS` if the
    /// bulk-insert was successful, or `FAILED` if any error was encountered
    /// @throws SQLException If any database operation fails or is caught within a helper function
    /// @throws RuntimeException If the helper function used to insert child items receives either two
    /// valid parent IDs, or no valid parent ID
    public static OperationResult<Integer> insertNewSet(
            ScanLocation parent, ArrayList<ItemRow> children, Connection conn
    ) throws SQLException {
    ArrayList<FeedbackMsg> feedback = new ArrayList<>();
        logger.info("Beginning insert transaction");

        // Getting the parent ID
        int parentId;
        ItemRow parentItem = parent.node();
        try {
            if (parentItem.id() == -1) {
                logger.debug("Parent node has no ID; inserting new parent");
                parentId = DatabaseInsert.insertParentNode(parentItem, conn);
                // This operation will either return a valid ID,
                // or throw an SQLException
                feedback.add(FeedbackMsg.create(
                        "import.db.insert.parent.success",
                        new Object[] {parentItem.name()}
                ));
            } else {
                parentId = parentItem.id();
            }
            logger.debug("Parent ID obtained: " + parentId);
            if (!DatabaseFetch.isScanLocation(parentId, conn)) {
                logger.debug("Inserting scan location for parent node");
                DatabaseInsert.insertScanLocation(parentId, parent, conn);
                feedback.add(FeedbackMsg.create(
                        "import.db.insert.parent.scanlocation",
                        new Object[] {}
                ));
            }
        } catch (SQLException e) {
            logger.error("Critical error encountered: ", e);
            throw new SQLException(
                    "Unable to insert new parent node or fetch information about the existing parent node",
                    e
            );
        }
        feedback.add(FeedbackMsg.create(
                "import.db.insert.parent.process.success",
                new Object[] { parentItem.name() }
        ));

        // Inserting all child items
        try {
            for (ItemRow item : children) {
                DatabaseInsert.insertChildNode(parentId, item, conn);
            }
            feedback.add(FeedbackMsg.create(
                    "import.db.insert.children.success",
                    new Object[] {}
            ));
        } catch (SQLException e) {
            logger.error("Critical error encountered: ", e);
            throw new SQLException(
                    "Unable to insert child items into the DB due to a database error",
                    e
            );
        } catch (IllegalArgumentException e) {
            logger.error("Critical bug encountered: ", e);
            throw new RuntimeException(
                    "Unable to insert child items into the DB, either due to a missing or duplicate parent ID",
                    e
            );
        }
        feedback.add(FeedbackMsg.create(
                "import.db.insert.commit.success",
                new Object[] {}
        ));
        return OperationResult.create(OperationStatus.SUCCESS, parentId, feedback);
    }

    // === Utility Methods === \\

    /// Inserts a new parent node into the DB and returns the resulting ID
    /// @param item Information about the parent item
    /// @param conn Database connection
    /// @return The ID of the inserted item
    /// @throws SQLException If the insert fails, returns no ID, or the operation otherwise fails
    private static int insertParentNode(ItemRow item, Connection conn) throws SQLException {
        logger.trace(item.toString());
        String query = """
                INSERT INTO node (
                name, path, display_name, file_type,
                last_modified, size, status, parent
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?);
                """;
        PreparedStatement stmt = conn.prepareStatement(
                query, Statement.RETURN_GENERATED_KEYS
        );
        stmt.setString(1, item.name());
        stmt.setString(2, item.path());
        stmt.setString(3, item.displayName());
        stmt.setString(4, item.fileType());
        stmt.setLong(5, item.lastModified());
        stmt.setDouble(6, item.size());
        stmt.setString(7, item.status().toString());
        stmt.setObject(8, item.parent(), Types.INTEGER);
        int rowsAffected = stmt.executeUpdate();
        if (rowsAffected != 1) {
            throw new SQLException(
                    "Expected one inserted parent node, got " + rowsAffected
            );
        }
        ResultSet keys = stmt.getGeneratedKeys();
        if (!keys.next()) {
            throw new SQLException(
                    "Inserted parent element did not return a generated ID"
            );
        }
        int parentId = keys.getInt(1);
        if (keys.wasNull()) {
            throw new SQLException(
                    "Generated parent ID was NULL"
            );
        }
        return parentId;
    }

    /// Inserts a child item into the DB.\
    /// To account for newly scanned directories, which first create `ItemRow` instances and *then* insert into
    /// the DB (hence, neither child nor parent items have IDs), a parent ID can be passed into this method
    /// as a nullable parameter.
    /// @param parentId The ID of the parent node; can be `null`
    /// @param item An item; if the parent ID is `null`, this one should have a valid parent ID
    /// @param conn Database connection
    /// @throws SQLException If the insert operation fails
    /// @throws IllegalArgumentException If either two valid or two invalid parent IDs are passed as parameters
    private static void insertChildNode(Integer parentId, ItemRow item, Connection conn) throws SQLException {
        logger.trace(item.toString() + " || Alternative ID: " + parentId);
        if (parentId != null && item.parent() != null) {
            throw new IllegalArgumentException(
                    "Only one parent ID should be provided"
            );
        }
        Integer effectiveParent = parentId != null ? parentId : item.parent();
        if (effectiveParent == null) {
            throw new IllegalArgumentException(
                    "Parent ID is required to insert child node"
            );
        }

        String query = """
                INSERT INTO node (
                name, path, display_name, file_type,
                last_modified, size, status, parent
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?);
                """;
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, item.name());
        stmt.setString(2, item.path());
        stmt.setString(3, item.displayName());
        stmt.setString(4, item.fileType());
        stmt.setLong(5, item.lastModified());
        stmt.setDouble(6, item.size());
        stmt.setString(7, item.status().toString());
        stmt.setInt(8, effectiveParent);
        int affectedRows = stmt.executeUpdate();
        if (affectedRows != 1) {
            throw new SQLException(
                    "Unable to insert child node into DB"
            );
        }
    }

    /// Inserts the scan location of a parent item.\
    /// For newly scanned directories, the passed `ScanLocation` may not contain an `ItemRow` with a
    /// valid parent ID; therefore, a nullable ID may be passed as an argument.
    /// @param nodeId The ID of the node; can be `null`
    /// @param node A parent item's scan location; if the ID is null, this one should
    ///             contain an `ItemRow` with a valid ID
    /// @param conn Database connection
    /// @throws SQLException If the insert operation fails
    /// @throws IllegalArgumentException If either two valid or two invalid node IDs are passed as parameters
    private static void insertScanLocation(Integer nodeId, ScanLocation node, Connection conn) throws SQLException {
        logger.trace(node.toString() + " || Alternative ID: " + nodeId);
        if (nodeId != null && node.node().parent() != null) {
            throw new IllegalArgumentException(
                    "Only one parent ID should be provided"
            );
        }
        Integer effectiveParent = nodeId != null ? nodeId : node.node().parent();
        if (effectiveParent == null) {
            throw new IllegalArgumentException(
                    "Parent ID is required to insert child node"
            );
        }
        logger.trace(node.toString());
        String query = """
                INSERT INTO scan_location (
                node_id, scan_depth, update_mode, scan_source,
                generation, status
                ) VALUES (?, ?, ?, ?, ?, ?);
                """;
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, effectiveParent);
        stmt.setInt(2, node.scanDepth());
        stmt.setString(3, node.updateMode());
        stmt.setString(4, node.scanSource());
        stmt.setInt(5, node.generation());
        stmt.setString(6, node.status().toString());
        int affectedRows = stmt.executeUpdate();
        if (affectedRows != 1) {
            throw new SQLException(
                    "Unable to insert scan location into DB"
            );
        }
    }
}
