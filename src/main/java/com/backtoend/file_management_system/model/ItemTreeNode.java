package com.backtoend.file_management_system.model;

import javafx.scene.control.TreeItem;

public class ItemTreeNode extends TreeItem<ItemRow> {
    private boolean childrenLoaded;

    public ItemTreeNode(ItemRow item) {
        super(item);
    }
    public boolean areChildrenLoaded() {
        return childrenLoaded;
    }
    public void markChildrenLoaded() {
        this.childrenLoaded = true;
    }
}
