package com.backtoend.file_management_system.model;

import java.text.MessageFormat;

public record ItemRow(
        int id, String name, String path, String displayName, String fileType, long lastModified,
        double size, IItemInformation.ItemStatus status, Integer parentId, boolean hasChildren
) implements IItemInformation {
    // Constructor from ScannedItem
    public static ItemRow create(ScannedItem item, int id, Integer parentId) {
        return new ItemRow(
                id, item.name(), item.path(), item.name(), item.fileType(), item.lastModified(),
                item.size(), IItemInformation.ItemStatus.ACTIVE, parentId, false
        );
    }

    public String display() {
        return MessageFormat.format(
                "Item with ID '{0}' >> name: {0} | path: {1} | display name: {2} | file type: {3} | " +
                        "last modified: {4} | size on disk: {5} | status: {6} | parent ID: {7} | is parent: {8}",
                new Object[] { this.id, this.name, this.path, this.displayName, this.fileType, formatLastModified(),
                this.size, this.status.toString(), this.parentId, this.hasChildren}
        );
    }
}
