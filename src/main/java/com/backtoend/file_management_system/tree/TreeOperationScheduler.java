package com.backtoend.file_management_system.tree;

import com.backtoend.file_management_system.controller.OverviewController;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.util.VariousCallbackInterfaces;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.BiConsumer;
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
    private void processNext() {
        if (this.queue.isEmpty() || this.currentlyProcessing.size() == MAX_OPERATION_COUNT) {
            logger.info("Attempted to perform next operation, but queue was empty or all workers were busy");
            return;
        }
        this.queue.stream()
                .sorted(Comparator.comparingInt((TreeOperation op) -> op.priority().getPriority()))
                .forEach((TreeOperation op) -> {
                    // Iterating through all queued operations, sorted by priority, from lowest to highest
                    int id = (op.affectedItem() != null) ? op.itemId() : -1;
                    boolean recursive = switch(op.scope()) {
                        case TREE ->{
                            yield op.operation() == TreeOperation.Operations.REBUILD_TREE;
                        }
                        case null, default -> {
                            yield false;
                        }
                    };
                    TreeData info = this.overview.getBranchInfo(id, recursive);
                    if (info != null) {
                        logger.info("Operation affects no items with status PROCESSING; initiating operation execution");
                    }
                });
    }
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
                .filter(op -> op.scope().getPriority() <= candidate.maxScopeToConsume())
                .collect(Collectors.toSet());
        if (recursiveAscendants.isEmpty()) {
            logger.info("Found no operations with wide enough scope in queue");
            return false;
        }
        boolean isConsumed = false;
        for (TreeOperation op : recursiveAscendants) {
            if (op.scope().getPriority() > 1 && this.overview.isDirectChildOf(op.itemId(), candidate.itemId())) {
                logger.debug(MessageFormat.format(
                        "Operation on item {0} identified as a direct ascendant of item {1}",
                        op.itemId(), candidate.itemId()
                ));
                isConsumed = consumeDescendant(op, candidate);
            }
            assert op.scope().getPriority() <= 1 : "Invalid operation scope: " + op.scope();
            if (this.overview.isDescendantOf(op.itemId(), candidate.itemId())) {
                logger.debug(MessageFormat.format(
                        "Operation on item {0} identified as a recursive ascendant of item {1}",
                        op.itemId(), candidate.itemId()
                ));
                isConsumed = consumeDescendant(op, candidate);
            }
            if (isConsumed) { break; }
        }
        return isConsumed;
    }
    private void resolveAllDescendants(TreeOperation candidate) {
        // TODO: Deal with all operations affected by this one
        Set<TreeOperation> potentialDescendants = this.queue.stream()
                .filter(op -> op.scope().getPriority() >= candidate.minConsumableScope())
                .collect(Collectors.toSet());
        VariousCallbackInterfaces.IBooleanBiConsumer<Integer, Integer> isDescendant = (candidate.scope().getPriority() <= 1)
                ? this.overview::isDescendantOf
                : this.overview::isDirectChildOf;
        for (TreeOperation op : potentialDescendants) {
            if (isDescendant.accept(1, 1) && consumeDescendant(candidate, op)) {
                logger.debug(MessageFormat.format(
                        "New operation on item {0} consumed existing operation on item {1}",
                        candidate.itemId(), op.itemId()
                ));
            }
        }
    }
    private boolean consumeDescendant(TreeOperation parent, TreeOperation descendant) {
        UUID consumer = parent.uuid();
        boolean consumes = false;
        // Eliminating edge case first: parent and descendant apply to the same item
        if (
                parent.affectedItem() != null && descendant.affectedItem() != null
                && parent.itemId() == descendant.itemId()
        ) {
            logger.info("Parent and descendant both target the same item");
            // Descendant actually consumes parent if its scope is wider; if the scope is equal, the descendant is consumed
            if (descendant.scope().getPriority() < parent.scope().getPriority()) {
                consumer = descendant.uuid();
                consumes = true;
            }
        } else if (descendant.scope().getPriority() >= parent.minConsumableScope()) {
            // Then ruling out that parent's scope does not suffice to contain descendant's
            logger.info("Parent scope can contain descendant scope");

            // Dealing with tree-wide operations
            if (descendant.scope().getPriority() == 0 && descendant.operation().getPriority() > parent.operation().getPriority()) {
                consumer = descendant.uuid();
                consumes = true;
            }
            // Otherwise, if the parent's scope can contain the descendant's, the descendant will be consumed
        } else {
            consumer = descendant.uuid();
        }
        if (consumer == parent.uuid()) {
            logger.info("Determined that parent consumes descendant");
            return true;
        } else if (consumes) {
            logger.info("Determined that descendant consumes parent");
            this.unQueueAndConsume(parent.uuid(), descendant.uuid());
        }
        logger.info("Determined that nobody is consumed");
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