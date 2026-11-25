package com.backtoend.file_management_system.tree;

import com.backtoend.file_management_system.controller.OverviewController;
import com.backtoend.file_management_system.logging.AppLogger;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

// TODO: I am moving away from an "active task" mechanic. OverviewController itself should track what items can currently
//  be updated and only allow updates on items that are not currently scheduled or updating
public class TreeOperationScheduler {
    private final AppLogger logger = AppLogger.getLogger(TreeOperationScheduler.class);
    private final int MAX_OPERATION_COUNT;
    private final ArrayList<TreeOperation> queue;
    private final HashMap<Integer, Set<UUID>> currentlyQueued;
    private final Set<UUID> currentlyProcessing;
    private final OverviewController overview;

    public TreeOperationScheduler(OverviewController overview) {
        this.queue = new ArrayList<>();
        this.currentlyProcessing = new HashSet<>();
        this.currentlyQueued = new HashMap<>();
        this.overview = overview;
        this.MAX_OPERATION_COUNT = 1;
    }

    public void requestOperation(TreeOperation.OperationRequest request, TreeOperation.ITreeOperationCallback<?> callback) {
        logger.info("Received new operation request on item with ID " + request.affectedItem().getItem().id());
        TreeOperation candidate = TreeOperation.create(request, callback);
        boolean resolvedDirectly = resolveDirectAscendants(candidate);
        boolean resolvedRecursively = resolvedDirectly || resolveRecursiveAscendants(candidate);
        if (resolvedDirectly || resolvedRecursively) {
            logger.info("Successfully merged candidate with existing operation; ignoring request");
            return;
        }
        logger.info("Unable to merge candidate with existing operation; queuing new operation");
        resolveAllDescendants(candidate);
        queue(candidate);
    }

    // === Private Helper Methods === \\
    private boolean resolveDirectAscendants(TreeOperation candidate) {
        int effectiveId = (candidate.affectedItem() == null) ? -1 : candidate.itemId();
        logger.info("Attempting to resolve direct ascendants of operation affecting item: " + effectiveId);
        Set<UUID> directAscendants = this.currentlyQueued.get(effectiveId);
        if (directAscendants.isEmpty()) {
            logger.info("Found no directly affecting operations in queue map");
            return false;
        }
        Set<TreeOperation> ascendantOperations = this.queue.stream()
                .filter(op ->
                        directAscendants.contains(op.uuid())
                                && candidate.maxScopeToConsume() <= op.scope().getPriority())
                .collect(Collectors.toSet());
        boolean isConsumed = false;
        for (TreeOperation op : ascendantOperations) {
            if (consumeDescendant(op, candidate)) {
                isConsumed = true;
                break;
            }
        }
        return isConsumed;
    }
    private boolean resolveRecursiveAscendants(TreeOperation candidate) {
        // TODO: Deal with all operations recursively affecting this one's affected item
        assert candidate.affectedItem() != null : "Operation with invalid affected item passed into recursive search";
        logger.info("Attempting to resolve indirect/undiscovered connections of existing operations to item: " + candidate.itemId());
        Set<TreeOperation> recursiveAscendants = this.queue.stream()
                .filter(op -> op.scope().getPriority() <3)
                .collect(Collectors.toSet());
        if (recursiveAscendants.isEmpty()) {
            logger.info("Found no operations with wide enough scope in queue");
            return false;
        }
        boolean isConsumed = false;
    }
    private void resolveAllDescendants(TreeOperation candidate) {
        // TODO: Deal with all operations affected by this one
    }
    private boolean consumeDescendant(TreeOperation parent, TreeOperation descendant) {
        UUID consumer = parent.uuid();
        boolean consumes = true;
        // Eliminating edge case first: parent and descendant apply to the same item
        if (
                parent.affectedItem() != null && descendant.affectedItem() != null
                && parent.itemId() == descendant.itemId()
        ) {
            logger.info("Parent and descendant both target the same item");
            // Descendant actually consumes parent in this situation; no need to override 'consumes'
            if (descendant.scope().getPriority() < parent.scope().getPriority()) {
                consumer = descendant.uuid();
            }
        } else if (descendant.scope().getPriority() >= parent.minConsumableScope()) {
            // Then ruling out that parent's scope does not suffice to contain descendant's
            logger.info("Parent scope can contain descendant scope");

            // Dealing with tree-wide operations
            if (descendant.scope().getPriority() == 0 && descendant.operation().getPriority() > parent.operation().getPriority()) {
                consumer = descendant.uuid();
            }
        }
        if (consumer == parent.uuid()) {
            logger.info("Determined that parent consumes descendant");
            this.unQueueAndConsume(consumer, descendant.uuid());
            return true;
        }
        logger.info("Determined that descendant is not consumed");
        return false;
    }
    private void queue(TreeOperation candidate) {
        this.queue.add(candidate);
        this.currentlyQueued.computeIfAbsent(candidate.itemId(), k -> new HashSet<>());
        this.currentlyQueued.get(candidate.itemId()).add(candidate.uuid());
    }
    private void unQueue(UUID uuid) {
        this.currentlyQueued.entrySet().removeIf(entry -> {
            Set<UUID> queued = entry.getValue();
            queued.remove(uuid);
            return queued.isEmpty();
        });
        this.queue.removeIf(op -> op.uuid() == uuid);
    }
    private void unQueueAndConsume(UUID consumer, UUID replace) {
        this.currentlyQueued.forEach((key, queued) -> {
            if (queued.remove(replace)) {
                queued.add(consumer);
            }
        });
        this.queue.removeIf(op -> op.uuid() == replace);
    }
}