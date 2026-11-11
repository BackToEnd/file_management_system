package com.backtoend.file_management_system.service;

import com.backtoend.file_management_system.model.TreeOperationResult;

import java.sql.SQLException;

@FunctionalInterface
public interface TreeOperationCallback {
    void update(TreeOperationResult result);
}
