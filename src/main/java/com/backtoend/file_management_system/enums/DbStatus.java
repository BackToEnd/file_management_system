package com.backtoend.file_management_system.enums;

/// Defines all states the DB may be in;
/// - `NOT_INITIALIZED` if a connection to the DB could be established, but the DB has not yet been initialized
/// - `INITIALIZING` if the DB is currently being initialized
/// - `READY` if DB initialization was successful
/// - `FAILED` if the DB could not be initialized successfully, or no connection could be established
/// @author Rogue049
public enum DbStatus {
    NOT_INITIALIZED,
    INITIALIZING,
    READY,
    FAILED
}
