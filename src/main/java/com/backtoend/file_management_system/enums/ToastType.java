package com.backtoend.file_management_system.enums;

/// Defines all possible toast notification types;
/// - `SUCCESS` to let the user know that an otherwise invisible process
/// was successful; e.g. adding metadata to a scanned item
/// - `INFO` is a general debug message that gives the user insight into
/// non-critical process updates; they will only be shown in debug mode
/// - `WARNING` to let the user know that an operation either failed due
/// to things outside the user's control (e.g. if a translation key
/// cannot be found), or to let the user know that a process they requested
/// is not currently/yet available (e.g. attempting to start a scan/import
/// process while one is already running)
/// - `ERROR` for critical process failures; this is again debugging
/// territory, and actual error-handling should be done by a modal display
/// rather than a toast notification
/// @author Rogue049
public enum ToastType {
    SUCCESS,
    INFO,
    WARNING,
    ERROR
}
