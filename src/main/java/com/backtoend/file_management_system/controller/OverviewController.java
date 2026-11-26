package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.MainApplication;
import com.backtoend.file_management_system.database.Database;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.ItemRow;
import com.backtoend.file_management_system.model.ScanLocation;
import com.backtoend.file_management_system.tree.CustomTreeItem;
import com.backtoend.file_management_system.tree.TreeData;
import com.backtoend.file_management_system.util.I18n;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

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
    private final HashMap<Integer, UUID> affectedByOperation;

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
        treeView.setRoot(new CustomTreeItem());
    }

    public OverviewController() {
        this.db = MainApplication.getDbInstance();
        this.treeItems = new HashMap<>();
        this.affectedByOperation = new HashMap<>();
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
    public boolean matchTreeItemStatus(CustomTreeItem.TreeItemStatus status, int id) {
        return treeItems.get(id).getItemStatus() == status;
    }
    public boolean reloadIncludesChildren(int id) {
        return treeItems.get(id).reloadIncludesChildren();
    }
    public void updateStateOwner(int id, UUID newStateOwner) {
        affectedByOperation.put(id, newStateOwner);
    }

    public void rebuild() {
        logger.info("Rebuilding the entire tree");
    }

    public TreeData getSingleInfo(int id) {
        CustomTreeItem item = this.treeItems.get(id);
        assert item != null : "getSingleInfo was given an unknown ID";
        if (item.isBeingProcessed()) {
            return null;
        }
        return new TreeData(
                item.getValue(), item.getScanLocation(), new ArrayList<TreeData>(), item.getItemStatus(), item.getChildrenStatus()
        );
    }

    public TreeData getBranchInfo(int id, boolean recursive) {
        TreeData result;
        CustomTreeItem root;
        if (id == -1) {
            result = new TreeData(null, null, new ArrayList<TreeData>(), null, null);
            root = (CustomTreeItem) this.treeView.getRoot();
        } else {
            result = getSingleInfo(id);
            root = (CustomTreeItem) this.treeItems.get(id);
        }
        if (result == null) {
            return null;
        }
        assert root != null : "Unable to fetch root item";
        for (TreeItem<ItemRow> child : root.getChildren()) {
            int childId = child.getValue().id();
            TreeData childData = (recursive) ? getBranchInfo(childId, true) : getSingleInfo(childId);
            if (childData == null) {
                return null;
            }
            result.children().add(childData);
        }
        return result;
    }

    public void markSingleProcessing(int id, UUID responsible) {
        assert this.treeItems.get(id) != null : "Tried updating status of item with invalid ID";
        this.treeItems.get(id).process();
    }

    public void markAllProcessing(int id, UUID responsible, boolean recursive) {
        // TODO
    }
}
