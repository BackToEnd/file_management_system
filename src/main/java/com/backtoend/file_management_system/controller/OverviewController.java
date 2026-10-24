package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.Context;
import com.backtoend.file_management_system.database.DatabaseFetch;
import com.backtoend.file_management_system.enums.OperationStatus;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.ItemRow;
import com.backtoend.file_management_system.model.ItemTreeNode;
import com.backtoend.file_management_system.model.ParentNode;
import com.backtoend.file_management_system.service.I18n;
import com.backtoend.file_management_system.util.FeedbackMsg;
import com.backtoend.file_management_system.util.OperationResult;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Controller responsible for displaying any kind of DB output on the main app interface<br>
 * Is rendered by MainController upon app start
 */
public class OverviewController {
    private static final AppLogger logger = AppLogger.getLogger(OverviewController.class);
    @FXML
    private TreeTableView<ItemRow> treeView;
    @FXML
    private Label placeholder;
    @FXML
    private TreeTableColumn<ItemRow, String> nameCol;
    @FXML
    private TreeTableColumn<ItemRow, String> typeCol;
    @FXML
    private TreeTableColumn<ItemRow, String> pathCol;
    private final Context context;

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

    public OverviewController(Context context) {
        this.context = context;
    }

    // === Utility Methods === \\

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
            return new OperationResult<>(OperationStatus.FAILED, null, feedback);
        }
    }


    // region Creating the base tree of parent items
    private ItemTreeNode buildBaseTree(ArrayList<ItemRow> parents) throws SQLException {
        ItemTreeNode root = new ItemTreeNode(null);
        logger.info("Attempting to build base tree");
        for (ItemRow item : parents) {
            ParentNode parentNode = this.context.db()
                    .withConnection(conn ->
                            DatabaseFetch.getParentNodeOf(item, conn)
                    );
            logger.debug("Received scan location");
            ItemTreeNode treeNode = new ItemTreeNode(item, parentNode);
            this.configureNode(treeNode);
            root.getChildren().add(treeNode);
        }
        return root;
    }
    // endregion

    // region Loading new tree nodes
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
                        this.loadChilren(node);
                    }
                }
        );
    }
    // endregion

    // region Dealing with child items of parent nodes
    // TODO: How can this method throw exceptions without blowing up the event listener?
    private void loadChilren(ItemTreeNode parent) {
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
                if (item.hasChildren()) {
                    ParentNode parentNode = this.context.db()
                            .withConnection(conn ->
                                    DatabaseFetch.getParentNodeOf(item, conn)
                            );
                    child = new ItemTreeNode(item, parentNode);
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
