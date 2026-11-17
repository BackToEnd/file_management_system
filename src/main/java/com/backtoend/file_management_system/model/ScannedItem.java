package com.backtoend.file_management_system.model;

import java.text.MessageFormat;
import java.time.Instant;

public record ScannedItem(
    String name, String path, String fileType, long lastModified, double size, boolean hasChildren
) implements IItemInformation {
    public String display() {
        return MessageFormat.format(
                "Scanned item '{0}' >> path: {1} | file type: {2} | last modified at: {3} | size on disk: {4} | has children: {5}",
                new Object[] { this.name, this.path, this.fileType, formatLastModified(), this.size, (this.hasChildren) ? "yes": "no" }
        );
    }
}
