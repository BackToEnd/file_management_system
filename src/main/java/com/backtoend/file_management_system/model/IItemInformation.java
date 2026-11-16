package com.backtoend.file_management_system.model;

import java.time.Instant;

public interface IItemInformation {
    String display();
    long lastModified();
    default Instant formatLastModified() {
        return Instant.ofEpochMilli(lastModified());
    }
    enum ItemStatus {
        ACTIVE,
        DISABLED,
        NOT_FOUND,
        DIRTY
    }
    enum UpdateMode {
        AUTOMATIC,
        MANUAL
    }
    enum ScanSource {
        USER_INPUT,
        RECURSIVE,
        USER_UPDATE
    }
}
