package com.backtoend.file_management_system.enums;

/**
 * Defines all possible toast types; each toast type has its own color and display duration<br>
 * - `SUCCESS` if the toast informs the user of a successfully completed operation
 * - `INFO` if the toast is a general info notification
 * - `WARNING` if something prevented an action from being processed, e.g. attempting to scan a location before the DB initialization completes
 * - `ERROR` if something went wrong, but no immediate user input is required
 */
public enum ToastType {
    SUCCESS,
    INFO,
    WARNING,
    ERROR
}
