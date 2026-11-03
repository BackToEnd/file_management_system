package com.backtoend.file_management_system.model;

import com.backtoend.file_management_system.enums.ItemStatus;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.time.Instant;

/// Record class keeping track of directory scan data and DB output
/// @param id The database ID of this item
/// @param name The item's canonical name
/// @param path The item's absolute file path
/// @param displayName The item's display name; by default the same as the canonical name
/// @param fileType The file type - currently either "File" or "Folder"
/// @param lastModified Timestamp of the last modification done to this item
/// @param size The item's size on the disk
/// @param status Item status; `ACTIVE` by default, `DISABLED` if it's marked for deletion, `NOT_FOUND`
///               if its path no longer exists
/// @param parent The ID of this item's parent folder
/// @param hasChildren Whether this item is a parent item and has children
/// @author Rogue049
public record ItemRow(
        int id, String name, String path, String displayName, String fileType,
        long lastModified, double size, ItemStatus status, Integer parent, boolean hasChildren
) implements PrintableModel {

    /// Transforms this `ItemRow` instance into a printable String
    /// with custom formatting
    /// @return Printable, formatted String
    @Override
    public String display() {
        return MessageFormat.format(
                "Printing ItemRow '{0}' (ID: {1}): Path '{2}' | Display name '{3}' | File type '{4}' | "
                        + "Date modified: {5} | Size: {6} | Status: '{7}' | Parent ID '{8}' | Children '{9}'",
                new Object[] {
                        this.name, this.id, this.path, this.displayName, this.fileType,
                        Instant.ofEpochMilli(this.lastModified), this.size, this.status.toString(),
                        this.parent, this.hasChildren
                }
        );
    }

    /// Copies all values of this instance into a new instance,
    /// replacing the original ID with the new one
    /// @param id The new ID
    /// @return The cloned instance
    public ItemRow setNewId(int id) {
        return new ItemRow(
                id, this.name, this.path, this.displayName, this.fileType,
                this.lastModified, this.size, this.status, this.parent, this.hasChildren
        );
    }

    /// Copies all values of this instance into a new instance,
    /// replacing the original parent ID with the new one
    /// @param id The new parent ID
    /// @return The cloned instance
    public ItemRow setNewParent(Integer id) {
        return new ItemRow(
                this.id, this.name, this.path, this.displayName, this.fileType,
                this.lastModified, this.size, this.status, id, this.hasChildren
        );
    }

    /// Copies all values of this instance into a new instance,
    /// setting `hasChildren` to `true`
    /// @return The cloned instance
    public ItemRow makeParent() {
        return new ItemRow(
                this.id, this.name, this.path, this.displayName, this.fileType,
                this.lastModified, this.size, this.status, this.parent, true
        );
    }
}
