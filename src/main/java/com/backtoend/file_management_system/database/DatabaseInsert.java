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

    // === Utility Methods === \\

    // TODO: Does not support re-scanning directories
    /// Inserts a newly scanned directory, updates an existing node
    /// into a scan location, does not yet overwrite an existing
    /// scan location upon re-scan
    /// @param parentScanLocation The scan location containing the parent node
    /// @param children A list of all scanned children of the parent directory
    /// @param conn Database connection
    /// @return Scan location of the parent path with database ID
    /// @throws SQLException If any database operation cannot be completed
    public static OperationResult<ScanLocation> insertOrUpdateSet(
            ScanLocation parentScanLocation, ArrayList<ItemRow> children, Connection conn
    ) throws SQLException {
        // General setup
        ArrayList<FeedbackMsg> feedback = new ArrayList<>();
        ItemRow parentItemRow;
        ScanLocation effectiveParentScanlocation;
        logger.info("Beginning insert transaction");

        // Checking if this parent already exists in the DB
        if (parentScanLocation.node().id() == -1) {
            logger.debug("Parent node has no ID; inserting new parent");
            int parentId = DatabaseInsert.insertParentNode(parentScanLocation.node(), conn);
            parentItemRow = parentScanLocation.node().setNewId(parentId);
            feedback.add(FeedbackMsg.create(
                    "import.db.insert.parent.success",
                    new Object[] {}
            ));
        } else {
            parentItemRow = parentScanLocation.node();
        }
        effectiveParentScanlocation = parentScanLocation.setNewItemRow(parentItemRow);

        // Checking if this parent already exists as scan location
        if (!DatabaseFetch.isScanLocation(parentItemRow.id(), conn)) {
            logger.debug("Parent does not yet exist as scan location");
            DatabaseInsert.insertScanLocation(
                    effectiveParentScanlocation,
                    conn
            );
            feedback.add(FeedbackMsg.create(
                    "impor.db.insert.parent.scanlocation",
                    new Object[] {}
            ));
        }

        // Concluding parent insert
        feedback.add(FeedbackMsg.create(
                "import.db.insert.parent.process.success",
                new Object[] {}
        ));

        // Inserting child items
        for (ItemRow item : children) {
            insertChildNode(
                    item.setNewParent(parentItemRow.id()),
                    conn
            );
        }
        feedback.add(FeedbackMsg.create(
                "import.db.insert.children.success",
                new Object[] {}
        ));
        return OperationResult.create(
                OperationStatus.SUCCESS,
                effectiveParentScanlocation,
                feedback
        );
    }

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
    /// @param item An item; if the parent ID is `null`, this one should have a valid parent ID
    /// @param conn Database connection
    /// @throws SQLException If the insert operation fails
    /// @throws IllegalArgumentException If either two valid or two invalid parent IDs are passed as parameters
    private static void insertChildNode(ItemRow item, Connection conn) throws SQLException {
        logger.trace(item.toString());

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
        stmt.setInt(8, item.parent());
        int affectedRows = stmt.executeUpdate();
        if (affectedRows != 1) {
            throw new SQLException(
                    "Unable to insert child node into DB"
            );
        }
    }

    // TODO: This one should now be able to work entirely without
    //  passing a nullable Integer ID
    /// Inserts the scan location of a parent item.\
    /// For newly scanned directories, the passed `ScanLocation` may not contain an `ItemRow` with a
    /// valid parent ID; therefore, a nullable ID may be passed as an argument.
    /// @param node A parent item's scan location; if the ID is null, this one should
    ///             contain an `ItemRow` with a valid ID
    /// @param conn Database connection
    /// @throws SQLException If the insert operation fails
    /// @throws IllegalArgumentException If either two valid or two invalid node IDs are passed as parameters
    private static void insertScanLocation(ScanLocation node, Connection conn) throws SQLException {
        logger.trace(node.toString());
        logger.trace(node.toString());
        String query = """
                INSERT INTO scan_location (
                node_id, scan_depth, update_mode, scan_source,
                generation, status
                ) VALUES (?, ?, ?, ?, ?, ?);
                """;
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, node.node().id());
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
