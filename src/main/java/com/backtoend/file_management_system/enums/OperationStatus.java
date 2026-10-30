package com.backtoend.file_management_system.enums;

/**
 * Defines all possible outcomes of any bigger operation;<br>
 * - `SUCCESS` if the operation has completed successfully and without any errors<br>
 * - `CANCELLED` if the operation was cancelled by the user<br>
 * - `WARNING` if an error was encountered, but the error does not require any action from the user<br>
 * - `FAILED` if something went wrong, and the user should be informed that the operation could not be completed;
 * usually because this failure is fatal and terminated a process initiated by the user
 */
public enum OperationStatus {
    SUCCESS,
    CANCELLED,
    WARNING,
    FAILED
}
