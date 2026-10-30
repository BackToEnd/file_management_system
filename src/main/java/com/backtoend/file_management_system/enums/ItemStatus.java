package com.backtoend.file_management_system.enums;

/**
 * Defines all possible states a scanned item (parent or child) can have;<br>
 * - `ACTIVE` if the item exists<br>
 * - `DISABLED` if the item is marked for deletion<br>
 * - `NOT_FOUND` if an item existed before, but was not found during the last re-scan
 */
public enum ItemStatus {
    ACTIVE,
    DISABLED,
    NOT_FOUND
}
