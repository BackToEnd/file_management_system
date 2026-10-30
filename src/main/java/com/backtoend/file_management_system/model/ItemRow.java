package com.backtoend.file_management_system.model;

import com.backtoend.file_management_system.enums.ItemStatus;

import java.text.MessageFormat;
import java.time.Instant;

/**
 * Record class used for keeping track of DB output and scanned files
 * @param id An item's DB ID; -1 if none exists
 * @param name Absolute file name
 * @param path Absolute file path
 * @param displayName Custom display name; is either set via metadata settings or identical to the name
 * @param fileType What kind of file this is
 * @param lastModified When this file was last modified
 * @param size The file's size on disk
 * @param status File status; used to handle missing or deleted files without actual DB deletions
 * @param parent Nullable parent ID
 * @param hasChildren Whether this is a regular node, or a scan location node
 */
public record ItemRow(
        int id, String name, String path, String displayName, String fileType,
        long lastModified, double size, ItemStatus status, Integer parent, boolean hasChildren
) {
    /**
     * Formats the ItemRow into a printable String
     * @return Printable String of all contained values
     */
    public String toString(){
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

    /**
     * Clones this ItemRow instance but replaces the ID
     * @param id The new ID
     * @return Cloned ItemRow instance
     */
    public ItemRow setNewId(int id) {
        return new ItemRow(
                id, this.name, this.path, this.displayName, this.fileType,
                this.lastModified, this.size, this.status, this.parent, this.hasChildren
        );
    }

    /**
     * Clones this ItemRow instance but replaces the parent ID
     * @param id The new parent ID
     * @return Cloned ItemRow instance
     */
    public ItemRow setNewParent(Integer id) {
        return new ItemRow(
                this.id, this.name, this.path, this.displayName, this.fileType,
                this.lastModified, this.size, this.status, id, this.hasChildren
        );
    }

    /**
     * Clones this ItemRow instance but sets the value of `hasChildren` to `true`
     * @return Cloned ItemRow instance
     */
    public ItemRow makeParent() {
        return new ItemRow(
                this.id, this.name, this.path, this.displayName, this.fileType,
                this.lastModified, this.size, this.status, this.parent, true
        );
    }
}
