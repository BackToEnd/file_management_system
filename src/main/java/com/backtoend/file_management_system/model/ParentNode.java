package com.backtoend.file_management_system.model;

import com.backtoend.file_management_system.enums.ItemStatus;

import java.text.MessageFormat;

/**
 * Record class used to keep track of scan locations of parent nodes
 * @param node The associated ItemRow
 * @param scanDepth How far recursive scanning will scan; should default to 0
 * @param updateMode Whether this scan location should be updated automatically
 * @param scanSource Whether this node was a user-selected scan location, or automatically generated via recursive scanning
 * @param generation Counts how many generations recursive scanning has scanned; starts with 0 for the original parent
 * @param status File status; used to handle missing or deleted files recursively without actual DB deletions
 */
public record ParentNode(
        ItemRow node, int scanDepth, String updateMode,
        String scanSource, int generation, ItemStatus status
) {
    /**
     * Formats the ParentNode into a printable String
     * @return Printable String
     */
    public String toString() {
        return MessageFormat.format(
                "Printing ParentNode for Item '{0}' (ID: {1}): Scan depth '{2}' | Update mode '{3}' | "
                + "Scan source '{4}' | Generation [{5}] | Status '{6}'",
                new Object[] {
                        this.node.name(), this.node.id(), scanDepth, updateMode, scanSource, generation, status.toString()
                }
        );
    }
}
