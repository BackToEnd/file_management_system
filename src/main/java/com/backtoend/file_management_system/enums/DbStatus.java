package com.backtoend.file_management_system.enums;

/**
 * Defines all possible DB states;<br>
 * - `NOT_INITIALIZED` if the DB initialization process was not yet started<br>
 * - `INITIALIZING` if the DB initialization is currently in progress<br>
 * - `READY` if the DB initialization has been completed successfully<br>
 * - `FAILED` if the DB could not be initialized - this is usually a fatal state
 */
public enum DbStatus {
    NOT_INITIALIZED,
    INITIALIZING,
    READY,
    FAILED
}
