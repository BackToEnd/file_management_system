package com.backtoend.file_management_system.tree;

import com.backtoend.file_management_system.logging.AppLogger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TreeOperationExecutor implements Runnable {
    private final AppLogger logger = AppLogger.getLogger(TreeOperationExecutor.class);
    private final TreeOperation operation;
    private final TreeData data;

    public TreeOperationExecutor(TreeOperation operation, TreeData data) {
        this.operation = operation;
        this.data = data;
    }

    @Override
    public void run() {
        // TODO
    }

    // === Private Helper Methods === \\
    private TreeData fetchBaseItems() {
        // TODO
    }
    private TreeData fetchItemInfo() {
        // TODO
    }
    private TreeData fetchNormally() {
        // TODO
    }
    private TreeData fetchRecursively() {
        // TODO
    }
}
