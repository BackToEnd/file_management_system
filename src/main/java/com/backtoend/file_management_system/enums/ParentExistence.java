package com.backtoend.file_management_system.enums;

/// Defines all possible answers to the question "Does this path
/// already exist in the DB?";
/// - `IS_SCAN_LOCATION` if the path exists both as a node, and as a scan
/// location
/// - `IS_NODE` if the path exists as a node, but not as a scan location
/// - `MISSING` if the path does not exist in the DB
/// @author Rogue049
public enum ParentExistence {
    IS_SCAN_LOCATION,
    IS_NODE,
    MISSING
}
