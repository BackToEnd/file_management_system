package com.backtoend.file_management_system.database;

import com.backtoend.file_management_system.enums.OperationStatus;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.ItemRow;
import com.backtoend.file_management_system.model.ParentNode;
import com.backtoend.file_management_system.util.FeedbackMsg;
import com.backtoend.file_management_system.util.OperationResult;
import java.sql.*;
import java.util.ArrayList;

/**
 * Database helper class that handles any sort of DB insert operations
 */
public class DatabaseInsert {
    private static final AppLogger logger = AppLogger.getLogger(DatabaseInsert.class);

    /**
     * Inserts the content of a directory into the DB<br>
     * This method can currently not update existing directories, but it CAN
     * deal with existing parent nodes. Even with existing nodes that are not yet scan locations.
     * @param parent An ItemRow of the parent directory
     * @param children A list of ItemRows located within the parent directory
     * @param db Database instance
     * @return Operation status and status messages
     */
    public static OperationResult<Void> insertNewSet(
            ParentNode parent, ArrayList<ItemRow> children, Database db
    ) {
        ArrayList<FeedbackMsg> feedback = new ArrayList<>();

        try (Connection conn = db.connect()) {
            conn.setAutoCommit(false);
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
                    feedback.add(new FeedbackMsg(
                            DatabaseInsert.class.getName(),
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
                    feedback.add(new FeedbackMsg(
                            DatabaseInsert.class.getName(),
                            "import.db.insert.parent.scanlocation", new Object[] {}
                    ));
                }
            } catch (SQLException e) {
                logger.error("Critical error encountered: ", e);
                feedback.add(new FeedbackMsg(
                        DatabaseInsert.class.getName(),
                        "import.db.insert.parent.failed", new Object[] {}
                ));
                return new OperationResult<>(OperationStatus.FAILED, null, feedback);
            }
            feedback.add(new FeedbackMsg(
                    DatabaseInsert.class.getName(),
                    "import.db.insert.parent.process.success",
                    new Object[] { parentItem.name() }
            ));

            // Inserting all child items
            try {
                for (ItemRow item : children) {
                    DatabaseInsert.insertChildNode(parentId, item, conn);
                }
                feedback.add(new FeedbackMsg(
                        DatabaseInsert.class.getName(),
                        "import.db.insert.children.success", new Object[] {}
                ));
            } catch (SQLException e) {
                logger.error("Critical error encountered: ", e);
                feedback.add(new FeedbackMsg(
                        DatabaseInsert.class.getName(),
                        "import.db.insert.children.failed", new Object[] {}
                ));
                try {
                    conn.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }
                return new OperationResult<>(OperationStatus.FAILED, null, feedback);
            } catch (IllegalArgumentException e) {
                logger.error("Critical bug encountered: ", e);
                feedback.add(new FeedbackMsg(
                        DatabaseInsert.class.getName(),
                        "import.db.insert.illegalargument", new Object[] {}
                ));
                try {
                    conn.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }
                return new OperationResult<>(OperationStatus.FAILED, null, feedback);
            }

            // Committing transaction
            conn.commit();

            feedback.add(new FeedbackMsg(
                    DatabaseInsert.class.getName(),
                    "import.db.insert.commit.success", new Object[] {}
            ));
            return new OperationResult<>(OperationStatus.SUCCESS, null, feedback);

        } catch (SQLException e) {
            logger.error("Critical error encountered: ", e);
            feedback.add(new FeedbackMsg(
                    DatabaseInsert.class.getName(),
                "import.db.insert.sqlexception", new Object[] {}
            ));

            return new OperationResult<>(
                    OperationStatus.FAILED, null, feedback
            );
        }
    }

    // === Utility Methods === \\

    /**
     * Inserts a parent item into the DB and returns the newly inserted record's ID
     * @param item The ItemRow of the parent item
     * @param conn A DB connection
     * @return The ID of the inserted item
     * @throws SQLException If the query cannot be executed for any reason
     */
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

    /**
     * Inserts a child item into the DB, linking to the provided parent ID
     * @param parentId Nullable parent ID in case ItemRows were created before parent ID was known
     * @param item ItemRow of the child item to be inserted
     * @param conn DB connection
     * @throws SQLException If the query cannot be executed for any reason
     */
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

    /**
     * Inserts a parent node's scan location into the DB
     * @param parentId Nullable parent ID in case parent ItemRow and ParentNode were created before its ID was known
     * @param node ParentNode of the scan location
     * @param conn DB connection
     * @throws SQLException If the query cannot be executed for any reason
     */
    private static void insertScanLocation(Integer parentId, ParentNode node, Connection conn) throws SQLException {
        logger.trace(node.toString() + " || Alternative ID: " + parentId);
        if (parentId != null && node.node().parent() != null) {
            throw new IllegalArgumentException(
                    "Only one parent ID should be provided"
            );
        }
        Integer effectiveParent = parentId != null ? parentId : node.node().parent();
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
