package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.MainApplication;
import com.backtoend.file_management_system.database.Database;
import com.backtoend.file_management_system.database.DatabaseFetch;
import com.backtoend.file_management_system.enums.OperationStatus;
import com.backtoend.file_management_system.enums.ToastType;
import com.backtoend.file_management_system.enums.TreeItemStatus;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.ItemRow;
import com.backtoend.file_management_system.model.ItemTreeNode;
import com.backtoend.file_management_system.model.ScanLocation;
import com.backtoend.file_management_system.service.I18n;
import com.backtoend.file_management_system.util.FeedbackMsg;
import com.backtoend.file_management_system.util.OperationResult;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
    private final Database db;
    private final ToastController toast;
    private final HashMap<Integer, ItemTreeNode> itemTreeNodes;

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
        this.db = MainApplication.getDatabaseInstance();
        this.toast = MainApplication.getToastControllerInstance();
        this.itemTreeNodes = new HashMap<>();
    }

    // === Utility Methods === \\

    public void initiateBaseTree() {
        logger.info("Attempting to create the base tree table view");
        ArrayList<FeedbackMsg> feedback = new ArrayList<>();
        ArrayList<ItemRow> baseItems;

        // Fetching parent items
        logger.info("Asynchronously loading base tree items");
        Task<ArrayList<ScanLocation>> baseItemTask = getBaseItemsTask(feedback);
        Thread thread = new Thread(baseItemTask);
        thread.setDaemon(true);
        thread.start();
    }

    /// Creates a task used for asynchronously fetching all information
    /// on base items. Base items exist both as node and scan location,
    /// and have no parent of their own.
    /// @param feedback Array list of feedback messages
    /// @return Runnable task
    private Task<ArrayList<ScanLocation>> getBaseItemsTask(
            ArrayList<FeedbackMsg> feedback
    ) {
        Task<ArrayList<ScanLocation>> task = new Task<>() {
            @Override
            protected ArrayList<ScanLocation> call() throws SQLException {
                ArrayList<ItemRow> baseItems = db
                        .withConnection(DatabaseFetch::getAllParentItems);
                ArrayList<ScanLocation> scanLocations = new ArrayList<>();
                logger.info(
                        "Obtained base items",
                        "getBaseItemsTask",
                        this.getClass().getName()
                );
                for (ItemRow item : baseItems) {
                    scanLocations.add(
                            db.withConnection(conn ->
                                    DatabaseFetch.getScanLocationOf(item, conn)
                            )
                    );
                }
                logger.debug(
                        "Obtained scan locations to all items",
                        "getBaseItemsTask",
                        this.getClass().getName()
                );
                return scanLocations;
            }
        };
        task.setOnFailed(event -> {
            feedback.add(FeedbackMsg.create(
                    "overview.fetch.parent.all.failed",
                    new Object[] {},
                    "getBaseItemsTask",
                    this.getClass().getName()
            ));
            logger.error(
                    "Unable to fetch parent items:",
                    "getBaseItemsTask",
                    this.getClass().getName(),
                    task.getException()
                    );
            // TODO: Error-handling
        });
        task.setOnSucceeded(event -> {
            feedback.add(FeedbackMsg.create(
                    "overview.fetch.parent.all.success",
                    new Object[] {},
                    "getBaseItemsTask",
                    this.getClass().getName()
            ));

            // Attempting to build the tree
            this.populateBaseTree(task.getValue());
        });
        return task;
    }

    private void populateBaseTree(ArrayList<ScanLocation> parents) {
        ItemTreeNode root = new ItemTreeNode(null);
        logger.info("Attempting to populate the base tree");
        for (ScanLocation item : parents) {
            ItemTreeNode treeNode = new ItemTreeNode(
                    item.node(),
                    item
            );
            this.configureNode(treeNode);
            root.getChildren().add(treeNode);
        }
        this.treeView.setRoot(root);
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
            ScanLocation scanLocation = this.db
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
        logger.debug(
                "Configuring node '"
                        + node.getValue().name() + "' (ID "
                        + node.getValue().id() + ")"
        );
        // Adding the item to the HashMap
        this.itemTreeNodes.put(
                node.getValue().id(),
                node
        );

        // Attaching dummy child to parent nodes w/o children
        if (node.getValue().hasChildren() && node.isLeaf()) {
            node.getChildren().add(
                    new ItemTreeNode(null)
            );
        }
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
    /// @param parentId The ID of a parent item
    public void addNewParent(int parentId) {
        ItemRow newParent;
        try {
            newParent = this.db.withConnection(conn ->
                    DatabaseFetch.getInfo(parentId, conn)
            );
        } catch (SQLException e) {
            logger.error("Database operation failed:", e);
            this.toast.showToast(ToastType.ERROR, "toast.overview.add.failed");
            return;
        }
        logger.debug("Attempting to add new parent: " + newParent.name());
        ScanLocation scanLocation;
        try {
            scanLocation = this.db
                    .withConnection(conn ->
                            DatabaseFetch.getScanLocationOf(newParent, conn)
                    );
            logger.debug("Received scan location");
            ItemTreeNode item = new ItemTreeNode(newParent, scanLocation);
            this.configureNode(item);
            this.treeView.getRoot().getChildren().add(item);
        } catch (SQLException e) {
            logger.error("Critical error encountered: ", e);
            this.toast.showToast(ToastType.ERROR, "toast.overview.add.source.failed");
        }
    }
    // endregion

    // TODO: Properly deal with thrown IllegalStateExceptions
    //  Also, implement a way to refresh the tree
    public void addOrUpdateParent(ScanLocation parent) {
        ToastController toasts = MainApplication.getToastControllerInstance();

        // Checking if the provided parent already exists as a loaded node
        try {
            if (this.itemTreeNodes.containsKey(parent.node().id())) {
                logger.info(MessageFormat.format(
                        "Item with ID {0} is already a loaded node",
                        new Object[] {parent.node().id()}
                ));
                this.updateWithScanLocation(parent);
                return;
            }
        } catch (IllegalStateException e) {
            logger.error("Illegal state encountered:", e);
            toasts.showToast(
                    ToastType.WARNING,
                    "toast.overview.update.existing"
            );
        }

        // Checking if the provided parent is a root item
        if (parent.node().parent() == null) {
            logger.info(MessageFormat.format(
                            "Adding item with ID {0} as new base item",
                            new Object[] {parent.node().id()}
            ));
            this.updateBaseTree(parent);
            return;
        }

        // Checking if the provided parent has a loaded parent item
        try {
            int parentRootId = parent.node().parent();
            if (this.itemTreeNodes.containsKey(parentRootId)) {
                ItemTreeNode parentRootNode = this.itemTreeNodes.get(parentRootId);
                if (parentRootNode.areChildrenLoaded()) {
                    logger.info(MessageFormat.format(
                            "Adding item with ID {0} to its parent node" +
                                    "with ID {1}",
                            new Object[] {parent.node().id(), parentRootId}
                    ));
                    return;
                }
            }
        } catch (IllegalStateException e) {
            logger.error("Illegal state encountered:", e);
            toasts.showToast(
                    ToastType.WARNING,
                    "toast.overview.update.parent.add"
            );
        }

        // New parent is neither an existing node, nor a new base item,
        // nor the child of a node whose children are already loaded -
        // This item can be ignored and lazy-loaded later
        logger.info(MessageFormat.format(
                        "Item with ID {0} will be ignored",
                        new Object[] {parent.node().id()}
        ));
    }

    /// Updates an existing node with the provided scan location,
    /// updating the node's value and scan location, removing all
    /// existing children, reloading existing children if the node was
    /// previously expanded, and add a dummy child if it was not
    /// @param scanLocation New scan location of the target node
    private void updateWithScanLocation(ScanLocation scanLocation) {
        int itemId = scanLocation.node().id();
        ItemTreeNode node = this.itemTreeNodes.get(itemId);
        // Checking if this node is currently being reloaded
        if (node.getStatus() == TreeItemStatus.LOADING) {
            logger.warning("Unable to update node while a reload is already in progress");
            throw new IllegalStateException("Cannot update nodes while a reload is in progress");
        }
        boolean initialState = node.isExpanded();
        this.removeChildrenOf(node);
        node.reset(scanLocation);
        // Loading child items and expanding the node if it was previously expanded
        if (initialState) {
            this.loadChildren(node);
            node.setExpanded(true);
        } else {
            // Otherwise add dummy child
            node.getChildren().add(new ItemTreeNode(null));
        }
    }

    /// Inserts a new base item into the overview tree
    /// @param scanLocation Scan location with information about the new base item
    private void updateBaseTree(ScanLocation scanLocation) {
        ItemTreeNode item = new ItemTreeNode(scanLocation.node(), scanLocation);
        this.configureNode(item);
        this.treeView.getRoot().getChildren().add(item);
    }

    /// Attaches a new child item to the ItemTreeNode with the child's
    /// parent ID
    /// @param newChild ScanLocation of the new child item
    private void addNewChildTo(ScanLocation newChild) {
        int parentId = newChild.node().parent();
        ItemTreeNode parentNode = this.itemTreeNodes.get(parentId);
        // Checking if the parent node is currently reloading
        if (parentNode.getStatus() == TreeItemStatus.LOADING) {
            logger.warning("Unable to update parent node while a reload is already in progress");
            throw new IllegalStateException("Cannot update nodes while a reload is in progress");
        }
        ItemTreeNode childNode = new ItemTreeNode(newChild.node(), newChild);
        this.configureNode(childNode);
        parentNode.getChildren().add(childNode);
    }

    /// Removes the IDs of all child items of the provided parent from
    /// this instance's ID -> ItemTreeNode map, does not clear children
    /// of the parent ItemTreeNode.
    /// @param parent Item tree node whose children should be removed
    private void removeChildrenOf(ItemTreeNode parent) {
        ArrayList<Integer> childrenIds = parent.getChildIds();
        childrenIds.forEach(this.itemTreeNodes::remove);
    }

    // region Dealing with child items of parent nodes
    // FIXME: How can this method throw exceptions without blowing up the event listener?
    // TODO: Make this process async
    /// Loads all child items of a parent node and attaches them to the parent
    /// @param parent Parent node
    private void loadChildren(ItemTreeNode parent) {
        int parentId = parent.getValue().id();
        logger.info("Attempting to load and display children of node: " + parentId);
        parent.getChildren().clear();

        // Attempting to load child items
        try {
            ArrayList<ItemRow> children = this.db
                    .withConnection(conn ->
                            DatabaseFetch.getChildrenOf(parentId, conn)
                    );
            for (ItemRow item : children) {
                ItemTreeNode child;

                // Fetching scan location if a child is a scan location itself
                if (item.hasChildren()) {
                    ScanLocation scanLocation = this.db
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
