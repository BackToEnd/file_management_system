package com.backtoend.file_management_system.enums;

/// Defines all possible error types;
/// - `GENERIC` for broader categories that were not explicitly checked for
/// - `SPECIFIC` for exceptions that were explicitly checked for
/// - `CUSTOM` for application-specific exception classes
/// - `NONE` if no exception was caught
/// @author Rogue049
public enum ErrorType {
    GENERIC,
    SPECIFIC,
    CUSTOM,
    NONE
}
