package com.backtoend.file_management_system.model;

import com.backtoend.file_management_system.enums.TreeItemStatus;
import javafx.collections.ObservableList;
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
    private TreeItemStatus status;
    private int generation;

    public ItemTreeNode(ItemRow item) {
        super(item);
        this.status = TreeItemStatus.CHILDLESS;
        this.generation = 0;
    }
    public ItemTreeNode(ItemRow item, ScanLocation parent) {
        super(item);
        this.scanLocation = parent;
        this.status = TreeItemStatus.NOT_LOADED;
        this.generation = 0;
    }

    // Getters, Setters
    public ScanLocation getScanLocation() {
        return this.scanLocation;
    }
    public boolean areChildrenLoaded() {
        return (this.status == TreeItemStatus.READY);
    }
    public void markChildrenLoaded() {
        this.childrenLoaded = true;
        this.status = TreeItemStatus.READY;
    }
    public TreeItemStatus getStatus() {
        return this.status;
    }
    public int getGeneration() {
        return this.generation;
    }
    public void incrementGeneration() {
        this.generation ++;
    }

    // === Utility Methods === \\

    /// Shortcut to remove all child items from this instance
    public void clearChildren() {
        super.getChildren().clear();
        this.status = TreeItemStatus.CHILDLESS;
    }

    /// Updates the existing tree item based on the provided scan location\
    /// Only updates its own value ({@link ItemRow}) and {@link ScanLocation},
    /// but does not reset children or whether child items are already loaded
    /// @param newData A scan location providing all new information
    public void updateAll(ScanLocation newData) {
        this.setValue(newData.node());
        this.scanLocation = newData;
    }

    /// Updates the existing tree item based on the provided scan location\
    /// Overrides its own value ({@link ItemRow}) and {@link ScanLocation}.
    /// If its children were previously loaded, it resets that value and
    /// clears all children.
    /// @param newData A scan location providing all new information
    public void reset(ScanLocation newData) {
        if (this.areChildrenLoaded()) {
            this.status = TreeItemStatus.CHILDLESS;
            this.clearChildren();

        }
        this.setValue(newData.node());
        this.scanLocation = newData;
    }

    /// Updates the existing tree item node based on the provided item row\
    /// Clears all children, resets "children loaded" status, and sets
    /// the contained scan location ({@link ScanLocation}) to `null`.
    /// Overrides its own value (){@link ItemRow}) with the provided one.
    /// @param newData An item row containing all new information
    public void hardReset(ItemRow newData) {
        this.status = TreeItemStatus.CHILDLESS;
        this.clearChildren();
        this.setValue(newData);
        this.scanLocation = null;
    }

    /// Generates a list of IDs of all child items. Returns `null` if
    /// its children have not yet been loaded.
    /// @return A list of child item IDs
    public ArrayList<Integer> getChildIds() {
        if (!this.childrenLoaded) return null;
        ObservableList<TreeItem<ItemRow>> childItems = super.getChildren();
        ArrayList<Integer> result = new ArrayList<>();
        childItems.forEach(item -> {
            result.add(item.getValue().id());
        });
        return result;
    }

    /// Shortcut to adding a dummy child to parent items; relevant for lazy-loading
    public void addDummyChild() {
        this.getChildren().add(new ItemTreeNode(null));
    }
}
