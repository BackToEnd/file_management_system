package com.backtoend.file_management_system.tree;

import com.backtoend.file_management_system.model.IItemInformation;
import com.backtoend.file_management_system.model.ItemRow;
import com.backtoend.file_management_system.model.ScanLocation;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;

public class CustomTreeItem extends TreeItem<ItemRow> {
    private ScanLocation scanLocation;
    private TreeChildrenStatus childrenStatus;
    private TreeItemStatus itemStatus;

    public enum TreeItemStatus {
        PROCESSING,
        PENDING,
        READY
    }
    public enum TreeChildrenStatus {
        LEAF,
        NOT_LOADED,
        LOADING,
        READY
    }
    public CustomTreeItem() {
        super();
    }
    public CustomTreeItem (IItemInformation item) {
        super(item.getItem());
        if (item instanceof ScanLocation) {
            this.scanLocation = (ScanLocation) item;
        } else {
            this.scanLocation = null;
        }
        // Determining status
        ItemRow itemRow = item.getItem();
        this.itemStatus = TreeItemStatus.READY;
        if (itemRow.hasChildren()) {
            this.childrenStatus = TreeChildrenStatus.NOT_LOADED;
            super.getChildren().add(new CustomTreeItem());
        }
    }

    // Getters, setters
    public ScanLocation getScanLocation() {
        return this.scanLocation;
    }
    public void setScanLocation(ScanLocation scanLocation) {
        this.scanLocation = scanLocation;
    }
    // Child status
    public TreeChildrenStatus getChildrenStatus() { return this.childrenStatus; }
    public void setChildrenStatus(TreeChildrenStatus status) { this.childrenStatus = status; }
    public boolean reloadIncludesChildren() {
        return this.childrenStatus == TreeChildrenStatus.LOADING
                || this.childrenStatus == TreeChildrenStatus.READY;
    }
    // Item status
    public TreeItemStatus getItemStatus() { return this.itemStatus; }
    public boolean isBeingProcessed() { return this.itemStatus == TreeItemStatus.PROCESSING; }
    public boolean isQueued() { return this.itemStatus == TreeItemStatus.PENDING; }
    public boolean isReady() { return this.itemStatus == TreeItemStatus.READY; }
    public void setItemStatus(TreeItemStatus status) { this.itemStatus = status; }
    public void process() { setItemStatus(TreeItemStatus.PROCESSING); }
    public void queue() { setItemStatus(TreeItemStatus.PENDING); }
    public void ready() { setItemStatus(TreeItemStatus.READY); }

    public void addChildNodes(ArrayList<IItemInformation> children) {
        children.forEach(child -> {
            super.getChildren().add(new CustomTreeItem(child));
        });
        // TODO: Mark children loaded
    }
    public void clearChildren() {
        super.getChildren().clear();
    }
}
