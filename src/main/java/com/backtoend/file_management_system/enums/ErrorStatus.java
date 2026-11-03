package com.backtoend.file_management_system.enums;

/// Defines all states an error result may have;
/// - `SUCCESS` if no error was encountered
/// - `CANCELED` if the operation was canceled by means other than
/// an exception
/// - `FAILED` if the operation failed due to means other than a
/// caught exception
/// - `EXCEPTED` if an exception was thrown and caught successfully
/// @author Rogue049
public enum ErrorStatus {
    SUCCESS,
    CANCELED,
    FAILED,
    EXCEPTED
}
