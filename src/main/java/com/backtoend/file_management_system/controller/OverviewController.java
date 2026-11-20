package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.MainApplication;
import com.backtoend.file_management_system.database.Database;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.ItemRow;
import com.backtoend.file_management_system.tree.CustomTreeItem;
import com.backtoend.file_management_system.util.I18n;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

import java.util.HashMap;

/// Controller class contained within {@link MainController}.\
/// Responsible for generating and maintaining the tree overview, as well as
/// handling all user interactions with the tree.
public class OverviewController {
    /// Logger instance
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
    /// Database singleton instance
    private final Database db;
    /// I18n instance
    private final I18n i = I18n.getInstance();
    private final HashMap<Integer, CustomTreeItem> treeItems;

    @FXML
    private void initialize() {
        placeholder.textProperty().bind(i.bind("overview.table.placeholder"));
        nameCol.textProperty().bind(i.bind("overview.table.header.name"));
        nameCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(
                cellData.getValue().getValue().displayName()
        ));
        typeCol.textProperty().bind(i.bind("overview.table.header.type"));
        typeCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(
                cellData.getValue().getValue().fileType()
        ));
        pathCol.textProperty().bind(i.bind("overview.table.header.path"));
        pathCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(
                cellData.getValue().getValue().path()
        ));
    }

    public OverviewController() {
        this.db = MainApplication.getDbInstance();
        this.treeItems = new HashMap<>();
    }

    public boolean isDescendantOf(int beingProcessed, int potentialChild) {
        CustomTreeItem cursor = treeItems.get(potentialChild);
        assert cursor != null : "Child ID not registered in item tree map";
        Integer parentId = cursor.getValue().parentId();
        while (
                parentId != null
                && parentId != beingProcessed
        ) {
            cursor = treeItems.get(parentId);
            assert cursor != null : "Parent of a child item not registered in item tree map";
            parentId = cursor.getValue().parentId();
        }
        return parentId != null;
    }
    public boolean isDirectChildOf(int beingProcessed, int potentialChild) {
        CustomTreeItem cursor = treeItems.get(potentialChild);
        assert cursor != null : "Child ID not registered in item tree map";
        return cursor.getValue().parentId() == beingProcessed;
    }
    public boolean exists(int beingProcessed) {
        return treeItems.get(beingProcessed) != null;
    }

    public void rebuild() {
        logger.info("Rebuilding the entire tree");
    }
}
