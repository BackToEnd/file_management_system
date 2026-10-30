package com.backtoend.file_management_system.enums;

/**
 * Defines all values returned by DatabaseCheck's `doesParentExist` method;<br>
 * - `IS_SCAN_LOCATION` if an item exists as both node and scan location<br>
 * - `IS_NODE` if an item exists as node, but not as scan location<br>
 * - `MISSING` if the item does not exist as node
 */
public enum ParentExistence {
    IS_SCAN_LOCATION,
    IS_NODE,
    MISSING
}
