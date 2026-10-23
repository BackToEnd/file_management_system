package com.backtoend.file_management_system.model;

import javafx.scene.control.TreeItem;

import java.util.ArrayList;

/**
 * Custom implementation of the TreeItem class to be able to handle TreeTableViews better
 */
public class ItemTreeNode extends TreeItem<ItemRow> {
    private boolean childrenLoaded;
    private ParentNode scanLocation = null;

    public ItemTreeNode(ItemRow item) {
        super(item);
    }
    public ItemTreeNode(ItemRow item, ParentNode parent) {
        this.scanLocation = parent;
    }

    // Getters, Setters
    public ParentNode getScanLocation() {
        return this.scanLocation;
    }
    public boolean areChildrenLoaded() {
        return childrenLoaded;
    }
    public void markChildrenLoaded() {
        this.childrenLoaded = true;
    }

    // === Utility Methods === \\
    public void addChildNodes(ArrayList<ItemRow> items) {
        items.forEach(i -> {
            super.getChildren().add(new ItemTreeNode(i));
        });
        this.markChildrenLoaded();
    }
}
