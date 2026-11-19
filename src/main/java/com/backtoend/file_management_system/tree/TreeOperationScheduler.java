package com.backtoend.file_management_system.tree;

import com.backtoend.file_management_system.controller.OverviewController;
import com.backtoend.file_management_system.logging.AppLogger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// TODO: I am moving away from an "active task" mechanic. OverviewController itself should track what items can currently
//  be updated and only allow updates on items that are not currently scheduled or updating
public class TreeOperationScheduler {
    private final AppLogger logger = AppLogger.getLogger(TreeOperationScheduler.class);
    private final ArrayList<TreeOperation> queue;
    private boolean available;
    private final OverviewController overview;

    public TreeOperationScheduler(OverviewController overview) {
        this.queue = new ArrayList<>();
        this.available = true;
        this.overview = overview;
    }

    public void request(TreeOperation request, TreeOperation.ITreeOperationCallback<?> callback) {
        this.queue.add(request);
        if (available) {
            processNext();
        }
    }

    private void processNext() {
        if (available) {
            // TODO: Implement task processing or something
        }
    }

    private TreeOperation getNext() {
        return this.queue.stream()
                .min(Comparator
                        .comparing(TreeOperation::priority))
                .orElse(null);
    }

    private void invalidateAffected(TreeOperation task) {
        // Invalidate everything if the whole tree is affected
        if (task.scope().equals(TreeOperation.Scope.TREE)) {
            this.queue.clear();
            return;
        }
        int id = task.affectedItem().getItem().id();

    }
}
