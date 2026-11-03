package com.backtoend.file_management_system.enums;

/// Defines all possible states an operation have when returning;
/// - `SUCCESS` for successful operations that have caused no major issues
/// - `CANCELED` for operations canceled either by the user, or by other
/// means that do not require the user's direct attention
/// - `WARNING` for operations that either cannot be initiated, e.g. due
/// to another background process still running, or to let the user know
/// that something non-critical went wrong
/// - `FAILED` for operations that ran into a critical error, which requires
/// the user's direct attention, e.g. a scan/import directory scanning
/// or bulk-database-insert failing
/// @author Rogue049
public enum OperationStatus {
    SUCCESS,
    CANCELED,
    WARNING,
    FAILED
}
