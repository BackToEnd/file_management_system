package com.backtoend.file_management_system.tree;

import com.backtoend.file_management_system.model.IItemInformation;
import com.backtoend.file_management_system.model.ItemRow;
import com.backtoend.file_management_system.model.ScanLocation;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;

public class CustomTreeItem extends TreeItem<ItemRow> {
    private ScanLocation scanLocation;
    private Status status;

    public enum Status {
        LEAF,
        NOT_LOADED,
        SCHEDULED,
        LOADING,
        READY
    }
    public CustomTreeItem (IItemInformation item) {
        super(item.getItem());
        if (item instanceof ScanLocation) {
            this.scanLocation = (ScanLocation) item;
        } else {
            this.scanLocation = null;
        }
    }

    public ScanLocation getScanLocation() {
        return this.scanLocation;
    }
    public void setScanLocation(ScanLocation scanLocation) {
        this.scanLocation = scanLocation;
    }
    public boolean areChildrenLoaded() {
        return this.status == Status.READY;
    }
    public boolean canUpdate() {
        return this.status != Status.LOADING && this.status != Status.SCHEDULED;
    }
    public void markChildrenLoaded() {
        this.status = Status.READY;
    }
    public Status getStatus() {
        return this.status;
    }
    public void setStatus(Status status) {
        this.status = status;
    }

    public void addChildNodes(ArrayList<IItemInformation> children) {
        children.forEach(child -> {
            super.getChildren().add(new CustomTreeItem(child));
        });
        this.markChildrenLoaded();
    }
    public void clearChildren() {
        super.getChildren().clear();
    }
}
