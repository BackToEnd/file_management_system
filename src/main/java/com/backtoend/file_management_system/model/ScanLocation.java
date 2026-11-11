package com.backtoend.file_management_system.model;

import com.backtoend.file_management_system.enums.ItemStatus;

import java.text.MessageFormat;

/// Record class keeping track of scan locations of parent items
/// @param node This scan location's associated `ItemRow`
/// @param scanDepth Relevant for recursive scanning; defaults to 0. Higher numbers
///                  correspond to how many contained directories should be scanned; 1 means children and grandchildren,
///                  2 also includes great-grandchildren, and so on
/// @param updateMode How this scan location should be updated. This feature is not yet planned nor thought through.
///                   Likely, this String will be replaced with some form of enum eventually though.
/// @param scanSource Relevant for recursive scanning; defines how this scan location was created.
/// @param generation How many parent items this scan location's node has. No parents = 0,
///                   Direct child of a root parent = 1, and so on
/// @param status Item status, identical to {@link ItemRow}'s status,
///               but the children of `DISABLED` scan locations are ignored
/// @author Rogue049
public record ScanLocation(
        ItemRow node, int scanDepth, String updateMode,
        String scanSource, int generation, ItemStatus status
) implements PrintableModel{
    /// Formats this instance into a printable String
    /// @return Printable String
    @Override
    public String display() {
        return MessageFormat.format(
                "Printing ParentNode for Item '{0}' (ID: {1}): Scan depth '{2}' | Update mode '{3}' | "
                + "Scan source '{4}' | Generation [{5}] | Status '{6}'",
                new Object[] {
                        this.node.name(), this.node.id(), scanDepth, updateMode, scanSource, generation, status.toString()
                }
        );
    }

    /// Copies all values of this instance into a new instance,
    /// replacing the original ItemRow with the new one
    /// @param node The new ItemRow
    /// @return The cloned instance
    public ScanLocation setNewItemRow(ItemRow node) {
        return new ScanLocation(
                node, this.scanDepth, this.updateMode,
                this.scanSource, this.generation, this.status
        );
    }
}
