package com.backtoend.file_management_system.model;

import java.text.MessageFormat;
import java.time.Instant;

public record ScanLocation(
        ItemRow item, int scanDepth, IItemInformation.UpdateMode updateMode, IItemInformation.ScanSource scanSource,
        int generation, IItemInformation.ItemStatus status
) implements IItemInformation {
    public String display() {
        return MessageFormat.format(
                "",
                new Object[] {}
        );
    }
    // Because the interface demands it
    @Override
    public long lastModified() {
        return this.item.lastModified();
    }
}
