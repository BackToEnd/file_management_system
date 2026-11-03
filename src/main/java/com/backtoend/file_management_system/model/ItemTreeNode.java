package com.backtoend.file_management_system.model;

import javafx.scene.control.TreeItem;

import java.util.ArrayList;

/// Custom implementation of {@link TreeItem} adapted to better match this application's structure
/// @author Rogue049
public class ItemTreeNode extends TreeItem<ItemRow> {
    /// Whether the child items of this instance have already been loaded;
    /// children are only loaded once at first expansion, and only refreshed
    /// if this item is re-scanned or a direct child is modified
    private boolean childrenLoaded;
    /// `ScanLocation` instance, in case this is a parent item
    private ScanLocation scanLocation = null;

    public ItemTreeNode(ItemRow item) {
        super(item);
    }
    public ItemTreeNode(ItemRow item, ScanLocation parent) {
        super(item);
        this.scanLocation = parent;
    }

    // Getters, Setters
    public ScanLocation getScanLocation() {
        return this.scanLocation;
    }
    public boolean areChildrenLoaded() {
        return childrenLoaded;
    }
    public void markChildrenLoaded() {
        this.childrenLoaded = true;
    }

    // === Utility Methods === \\

    /// Adds the provided list of child items to this instance's children
    /// @param items A list of child items
    public void addChildNodes(ArrayList<ItemRow> items) {
        items.forEach(i -> {
            super.getChildren().add(new ItemTreeNode(i));
        });
        this.markChildrenLoaded();
    }

    /// Shortcut to remove all child items from this instance
    public void clearChildren() {
        super.getChildren().clear();
    }
}
