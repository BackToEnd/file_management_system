package com.backtoend.file_management_system.service;

/// Functional interface for handling {@link ScanImport}'s changes to the DB
/// @author Rogue049
@FunctionalInterface
public interface ScanImportCallback {
    /// Receives the ID of the newly created or updated parent node as an argument
    /// and defines what to do with that ID after the {@link ScanImport} process has concluded
    /// @param itemId Parent path ID
    void onSuccess(int itemId);
}
