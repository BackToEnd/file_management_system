package com.backtoend.file_management_system.service;

@FunctionalInterface
public interface ScanImportCallback {
    void onSuccess(int itemId);
}
