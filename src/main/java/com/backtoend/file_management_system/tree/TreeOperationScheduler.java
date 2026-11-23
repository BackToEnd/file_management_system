package com.backtoend.file_management_system.tree;

import com.backtoend.file_management_system.controller.OverviewController;
import com.backtoend.file_management_system.logging.AppLogger;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Consumer;

// TODO: I am moving away from an "active task" mechanic. OverviewController itself should track what items can currently
//  be updated and only allow updates on items that are not currently scheduled or updating
public class TreeOperationScheduler {
    private final AppLogger logger = AppLogger.getLogger(TreeOperationScheduler.class);
    private final int MAX_OPERATION_COUNT;
    private final ArrayList<TreeOperation> queue;
    private final Set<UUID> currentlyProcessing;
    private final OverviewController overview;

    public TreeOperationScheduler(OverviewController overview) {
        this.queue = new ArrayList<>();
        this.currentlyProcessing = new HashSet<>();
        this.overview = overview;
        this.MAX_OPERATION_COUNT = 1;
    }

    public void request(TreeOperation.OperationRequest request, TreeOperation.ITreeOperationCallback<?> callback) {
        // TODO: everything HELP
        logger.info(
                "Received operation request on item with ID: "
                        + request.affectedItem().getItem().id()
        );
        TreeOperation operation = getOperationCandidate(request, callback);
        if (!canBeQueued(operation)) {
            logger.info("Item status is not READY; identifying cause");
        }
        UUID affectingOperation = getAffectingOperation(operation);
        if (affectingOperation == null) {
            // Queue operation if
        }
    }

    // === Helper Methods === \\
    private TreeOperation getOperationCandidate(TreeOperation.OperationRequest request, TreeOperation.ITreeOperationCallback<?> callback) {
        TreeOperation.Priority priority;
        TreeOperation.Scope scope = switch (request.operation()) {
            case NUKE, REBUILD_TREE -> {
                priority = TreeOperation.Priority.REBUILD;
                yield TreeOperation.Scope.TREE;
            }
            case REBUILD_BRANCH -> {
                priority = TreeOperation.Priority.RESCAN;
                yield TreeOperation.Scope.DIRECT_CHILDREN;
            }
            case RECURSIVE_REBUILD -> {
                priority = TreeOperation.Priority.RESCAN;
                yield TreeOperation.Scope.ALL_DESCENDANTS;
            }
            case TRANSFORM_SELF -> {
                priority = TreeOperation.Priority.TRANSFORM;
                yield TreeOperation.Scope.SELF;
            }
            case LOAD_CHILDREN -> {
                priority = TreeOperation.Priority.LOAD;
                yield TreeOperation.Scope.DIRECT_CHILDREN;
            }
            case null, default -> throw new IllegalStateException(
                    "Unknown operation type: " + request.operation()
            );
        };
        UUID uuid = UUID.randomUUID();
        return new TreeOperation(
                request.affectedItem(), priority, scope,
                request.operation(), uuid, callback
        );
    }

    /// Tests whether the provided operation can be inserted into the queue.
    /// Does not take into considering that other affecting/affected operations
    /// may already exist in the queue - only cares for scope and tree item status.\
    /// Theoretically, an operation can be queued on an item that is currently
    /// being processed, because at the time the ongoing operation finishes,
    /// the retrieved information may already be outdated.\
    /// However, that would require
    /// @param operation a received operation request
    /// @return `true` if the operation can be queued, `false` if it does
    /// not have an appropriate item status
    private boolean canBeQueued(TreeOperation operation) {
        // Nuke and rebuild operations do not carry affected item IDs
        if (operation.scope() == TreeOperation.Scope.TREE) {
            return true;
        }
        int targetId = operation.affectedItem().getItem().id();
        // If the item's status is READY, it neither has operations waiting
        // in the queue, nor is it currently being processed.
        // Technically,
        return overview.matchTreeItemStatus(
                CustomTreeItem.TreeItemStatus.READY, targetId
        );
    }

    private UUID getAffectingOperation(TreeOperation request) {
        int requestId = request.affectedItem().getItem().id();
        TreeOperation affecting = this.queue.stream()
                .filter((TreeOperation op) ->
                        op.affectedItem().getItem().id() == requestId)
                .findFirst()
                .orElse(null);
        if (affecting == null) {
            logger.info("Found no directly affecting ID in the queue");
            return null;
        }
        return affecting.uuid();
    }
}
