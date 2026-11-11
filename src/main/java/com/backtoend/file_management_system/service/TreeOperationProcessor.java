package com.backtoend.file_management_system.service;

import com.backtoend.file_management_system.enums.TreeOperationPriority;
import com.backtoend.file_management_system.enums.TreeOperationScope;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.ItemRow;
import com.backtoend.file_management_system.model.ScanLocation;
import com.backtoend.file_management_system.model.TreeOperationResult;
import com.backtoend.file_management_system.util.QueuedTreeOperation;
import com.backtoend.file_management_system.util.TreeOperationRequest;
import javafx.concurrent.Task;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TreeOperationProcessor {
    private final ArrayList<QueuedTreeOperation> queue;
    private QueuedTreeOperation activeTask;
    private final AppLogger logger = AppLogger.getLogger(this.getClass());

    // Constructor
    public TreeOperationProcessor() {
        this.queue = new ArrayList<>();
        this.activeTask = null;
    }

    // === Utility Methods === \\
    public void request(TreeOperationRequest request, TreeOperationCallback update) {
        this.queue.add(
                new QueuedTreeOperation(0, request, update)
        );
        if (activeTask != null) {
            processNext();
        }
    }

    public void processNext() {
        if (activeTask != null) {
            return;
        }

        QueuedTreeOperation next = this.getNext();

        if (next == null) {
            return;
        }

        this.activeTask = next;
        this.invalidateAffected();
        this.execute();
    }

    // === Helper Methods === \\
    private QueuedTreeOperation getNext() {
        return this.queue.stream()
                .min(Comparator
                        .comparingInt((QueuedTreeOperation op) ->
                            op.request().operation().getPriority())
                        .thenComparingLong(QueuedTreeOperation::generation))
                .orElse(null);
    }

    private void invalidateAffected() {
        // Skipping all of this if the whole tree is affected
        if (this.activeTask.request().scope().equals(TreeOperationScope.TREE)) {
            this.queue.clear();
            return;
        }

        int id = this.activeTask.request().scanLocation().node().id();

        // Removing all requests targeting the current node
        this.queue.removeIf(r ->
                r.request().scanLocation().node().id() == id
        );

        // Removing all immediate child items for operations affecting direct children
        this.queue.removeIf(r ->
                r.request().scanLocation().node().parent() == id
        );
    }

    private void execute() {
        Task<TreeOperationResult> todo;
        switch (activeTask.request().type()) {
            case NUKE -> todo = getBaseItemsTask(this.activeTask);
            // REBUILD_TREE split between initial and follow-up
            case RESCAN_DIRECTORY -> todo = getRescanNodeTask(activeTask);
        }
    }

    // region Preparing all required tasks
    private Task<TreeOperationResult> getBaseItemsTask(QueuedTreeOperation request) {
        Task<TreeOperationResult> baseItems = new Task<>() {
            @Override
            protected TreeOperationResult call() throws SQLException {
                ArrayList<ScanLocation> items = TreeOperationExecutor.getBaseItems(request);
                return new TreeOperationResult(
                        request.request().type(),
                        null,
                        items
                );
            }
        };
        baseItems.setOnFailed(event -> {
            logger.error(
                    "Unable to fetch base items",
                    "getBaseItemsTask",
                    this.getClass().getName(),
                    baseItems.getException()
            );
            activeTask = null;
            processNext();
            // TODO: Proper error-handling
        });
        baseItems.setOnCancelled(event -> {
            logger.warning(
                    "Operation was canceled",
                    "getBaseItemsTask",
                    this.getClass().getName()
            );
            activeTask = null;
            processNext();
        });
        baseItems.setOnSucceeded(event -> {
            logger.info(
                    "Successfully fetched base parents",
                    "getBaseItemsTask",
                    this.getClass().getName()
            );
            activeTask.update().update(baseItems.getValue());
            activeTask = null;
            processNext();
        });
        return baseItems;
    }

    private Task<TreeOperationResult> getRescanNodeTask(QueuedTreeOperation request) {
        Task<TreeOperationResult> rescan = new Task<>() {
            @Override
            protected TreeOperationResult call() throws SQLException {
                TreeOperationResult rescanChildren = TreeOperationExecutor.getAllChildrenOf(request);
                // Root has already been reloaded if this is a FOLLOWUP
                if (request.request().operation().equals(TreeOperationPriority.FOLLOWUP)) {
                    return rescanChildren;
                }
                // This is the root call; fetching additional info on this item
                ScanLocation root = request.request().scanLocation();
                ArrayList<ItemRow> children = rescanChildren.items();
                ArrayList<ScanLocation> scans = rescanChildren.locations();
                TreeOperationResult rootInfo = TreeOperationExecutor.getInfoOn(request);
                children.addAll(rootInfo.items());
                scans.addAll(rootInfo.locations());
                return new TreeOperationResult(
                        request.request().type(),
                        children,
                        scans
                );
            }
        };
        rescan.setOnFailed(event -> {
            logger.error(
                    "Unable to fetch children of node with ID "
                    + request.request().scanLocation().node().id(),
                    "getRescanNodeTask",
                    this.getClass().getName(),
                    rescan.getException()
            );
            activeTask = null;
            processNext();
        });
        rescan.setOnCancelled(event -> {
            logger.warning(
                    "Operation was canceled",
                    "getRescanNodeTask",
                    this.getClass().getName()
            );
            activeTask = null;
            processNext();
        });
        rescan.setOnSucceeded(event -> {
            logger.info(
                    "Successfully fetched children of node with ID "
                            + request.request().scanLocation().node().id(),
                    "getRescanNodeTask",
                    this.getClass().getName()
            );
            activeTask.update().update(rescan.getValue());
            activeTask = null;
            processNext();
        });
        return rescan;
    }

}
