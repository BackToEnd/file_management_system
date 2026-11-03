package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.Context;
import com.backtoend.file_management_system.MainApplication;
import com.backtoend.file_management_system.database.DatabaseFetch;
import com.backtoend.file_management_system.enums.OperationStatus;
import com.backtoend.file_management_system.enums.ToastType;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.ItemRow;
import com.backtoend.file_management_system.model.ItemTreeNode;
import com.backtoend.file_management_system.model.ScanLocation;
import com.backtoend.file_management_system.service.I18n;
import com.backtoend.file_management_system.util.FeedbackMsg;
import com.backtoend.file_management_system.util.OperationResult;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.ArrayList;

/// Controller class used to render the overview tree on the main app interface
/// @author Rogue049
public class OverviewController {
    private static final AppLogger logger = AppLogger.getLogger(OverviewController.class);
    /// `TreeTableView` instance from the FXML file
    @FXML
    private TreeTableView<ItemRow> treeView;
    /// Placeholder label of the `TreeTableView` if it does not contain any items
    @FXML
    private Label placeholder;
    /// First `TreeTableColumn`; contains the item's display name
    @FXML
    private TreeTableColumn<ItemRow, String> nameCol;
    /// Second `TreeTableColumn`; contains the item's file type
    @FXML
    private TreeTableColumn<ItemRow, String> typeCol;
    /// Third `TreeTableColumn`; contains the item's path
    @FXML
    private TreeTableColumn<ItemRow, String> pathCol;
    private final Context context;

    /// Default initialization of this controller at first creation - defines text bindings for placeholder label and table headers,
    /// and sets cell value factories for each column
    @FXML
    private void initialize() {
        I18n i = I18n.getInstance();
        placeholder.textProperty().bind(
                i.text("overview.table.rows.placeholder")
        );
        nameCol.textProperty().bind(
                i.text("overview.table.columns.name")
        );
        nameCol.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(
                        cellData.getValue().getValue().name()
                )
        );
        typeCol.textProperty().bind(
                i.text("overview.table.columns.type")
        );
        typeCol.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(
                        cellData.getValue().getValue().fileType()
                )
        );
        pathCol.textProperty().bind(
                i.text("overview.table.columns.path")
        );
        pathCol.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(
                        cellData.getValue().getValue().path()
                )
        );
    }

    public OverviewController() {
        this.context = MainApplication.getContext();
    }

    // === Utility Methods === \\

    // TODO: Make async where necessary

    /// Attempts to build the initial tree structure consisting solely of parent items
    /// @return An operation result with operation status; status is `SUCCESSFUL` if the
    /// base tree can be built without any issues, and is otherwise `FAILED`
    public OperationResult<Void> buildTree() {
        logger.info("Attempting to generate tree table view");
        ArrayList<FeedbackMsg> feedback = new ArrayList<>();

        // Attempting to fetch parent items
        logger.debug("Attempting to fetch parent nodes from DB");
        ArrayList<ItemRow> parentItems;
        try {
            parentItems = this.context.db()
                    .withConnection(DatabaseFetch::getAllParentItems);
        } catch (SQLException e) {
            logger.error("Critical error encountered: ", e);
            feedback.add(new FeedbackMsg(
                    this.getClass().getName(),
                    "overview.fetch.parent.all.failed",
                    new Object[] {}
            ));
            this.context.toast().showToast(ToastType.ERROR, "toast.overview.build.fetch.failed");
            return new OperationResult<>(OperationStatus.FAILED, null, feedback);
        }
        feedback.add(new FeedbackMsg(
                this.getClass().getName(),
                "overview.fetch.parent.all.success",
                new Object[] {}
        ));

        // Attempting to build the tree
        try {
            this.treeView.setRoot(this.buildBaseTree(parentItems));
            feedback.add(new FeedbackMsg(
                    this.getClass().getName(),
                    "overview.build.tree.base.success",
                    new Object[] {}
            ));
            return new OperationResult<>(OperationStatus.SUCCESS, null, feedback);
        } catch (SQLException e) {
            logger.error("Critical error encountered: ", e);
            feedback.add(new FeedbackMsg(
                    this.getClass().getName(),
                    "overview.build.tree.base.failed",
                    new Object[] {}
            ));
            this.context.toast().showToast(ToastType.ERROR, "toast.overview.build.display.failed");
            return new OperationResult<>(OperationStatus.FAILED, null, feedback);
        }
    }


    // region Creating the base tree of parent items

    /// Creates a root `ItemTreeNode` and attaches all fetched base items as children,
    /// sets up expansion event listeners, and returns the created root item
    /// @param parents `ItemRow` list of base items
    /// @return The created `TreeTableView` root item
    /// @throws SQLException If it cannot fetch additional information about each parent's
    /// scan location from the DB
    private ItemTreeNode buildBaseTree(ArrayList<ItemRow> parents) throws SQLException {
        ItemTreeNode root = new ItemTreeNode(null);
        logger.info("Attempting to build base tree");
        for (ItemRow item : parents) {
            ScanLocation scanLocation = this.context.db()
                    .withConnection(conn ->
                            DatabaseFetch.getScanLocationOf(item, conn)
                    );
            logger.debug("Received scan location");
            ItemTreeNode treeNode = new ItemTreeNode(item, scanLocation);
            this.configureNode(treeNode);
            root.getChildren().add(treeNode);
        }
        return root;
    }
    // endregion

    // region Loading new tree nodes

    /// Configures tree items by adding a placeholder child to parent items and adding event listeners.
    /// Passed items with no children will be ignored
    /// @param node The node to be configured
    private void configureNode(ItemTreeNode node) {
        // No need to configure nodes without children
        if (!node.getValue().hasChildren()) {
            return;
        }

        logger.debug("Configuring node '" + node.getValue().name() + "' (ID " + node.getValue().id() + ")");
        // Adding placeholder child
        node.getChildren().add(
                new ItemTreeNode(null)
        );

        // Adding event listener for node expansion
        node.expandedProperty().addListener(
                (observable, wasExpanded, isExpanded) -> {
                    if (isExpanded && !node.areChildrenLoaded()) {
                        this.loadChildren(node);
                    }
                }
        );
    }

    // TODO: Implement a way to add existing children as parents
    /// Adds a new parent item to the tree.
    ///
    /// **TODO**: Adapt this method and all previous fetch steps to be able to add child items as parents
    /// @param parentId The ID of a parent item
    public void addNewParent(int parentId) {
        ItemRow newParent;
        try {
            newParent = this.context.db().withConnection(conn ->
                    DatabaseFetch.getInfo(parentId, conn)
            );
        } catch (SQLException e) {
            logger.error("Database operation failed:", e);
            this.context.toast().showToast(ToastType.ERROR, "toast.overview.add.failed");
            return;
        }
        logger.debug("Attempting to add new parent: " + newParent.name());
        ScanLocation scanLocation;
        try {
            scanLocation = this.context.db()
                    .withConnection(conn ->
                            DatabaseFetch.getScanLocationOf(newParent, conn)
                    );
            logger.debug("Received scan location");
            ItemTreeNode item = new ItemTreeNode(newParent, scanLocation);
            this.configureNode(item);
            this.treeView.getRoot().getChildren().add(item);
        } catch (SQLException e) {
            logger.error("Critical error encountered: ", e);
            this.context.toast().showToast(ToastType.ERROR, "toast.overview.add.source.failed");
        }
    }
    // endregion

    // region Dealing with child items of parent nodes
    // FIXME: How can this method throw exceptions without blowing up the event listener?

    /// Loads all child items of a parent node and attaches them to the parent
    /// @param parent Parent node
    private void loadChildren(ItemTreeNode parent) {
        int parentId = parent.getValue().id();
        logger.info("Attempting to load and display children of node: " + parentId);
        parent.getChildren().clear();

        // Attempting to load child items
        try {
            ArrayList<ItemRow> children = this.context.db()
                    .withConnection(conn ->
                            DatabaseFetch.getChildrenOf(parentId, conn)
                    );
            for (ItemRow item : children) {
                ItemTreeNode child;

                // Fetching scan location if a child is a scan location itself
                if (item.hasChildren()) {
                    ScanLocation scanLocation = this.context.db()
                            .withConnection(conn ->
                                    DatabaseFetch.getScanLocationOf(item, conn)
                            );
                    child = new ItemTreeNode(item, scanLocation);
                } else {
                    child = new ItemTreeNode(item);
                }
                this.configureNode(child);
                parent.getChildren().add(child);
            }
            parent.markChildrenLoaded();
        } catch (SQLException e) {
            logger.error("Critical error encountered: ", e);
            parent.getChildren().add(new TreeItem<>(null));
        }
    }
    // endregion

}
