package com.backtoend.file_management_system.enums;

/// Defines all possible states a scanned item
/// (parent or child) may have;
/// - `ACTIVE` is the default state for scanned and found items with a
/// path that still exists
/// - `DISABLED` for files that have been marked for deletion, either due
/// to automatic re-scans, or by the user themselves
/// - `NOT_FOUND` for files that were previously scanned into the DB
/// but were not found during a re-scan of the parent directory
/// @author Rogue049
public enum ItemStatus {
    ACTIVE,
    DISABLED,
    NOT_FOUND
}
