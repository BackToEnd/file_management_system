package com.backtoend.file_management_system.tree;

import com.backtoend.file_management_system.controller.OverviewController;
import com.backtoend.file_management_system.logging.AppLogger;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        TreeOperation operationRequest = TreeOperation.create(request, callback);
        if (request.status() == CustomTreeItem.TreeItemStatus.PENDING) {
            logger.debug("Requested operation affects item with status 'PENDING'");
            UUID pendingReason = pendingOperationReason(operationRequest);
            if (pendingReason == null) {
                logger.warning(
                        "Failed to fetch UUID of operation pending for item with ID "
                        + operationRequest.affectedItem().getItem().id()
                );
                return;
            }
        }
    }

    // === Private Helper Methods === \\
    // TODO: Make this thing work with UUID sets rather than single UUIDs
    private UUID pendingOperationReason(TreeOperation operation) {
        if (operation.scope() == TreeOperation.Scope.TREE) {
            TreeOperation affector = this.queue.stream()
                    .filter((TreeOperation op) -> op.scope() == TreeOperation.Scope.TREE)
                    .findFirst()
                    .orElse(null);
            return (affector == null) ? null : affector.uuid();
        }
        return null;
    }

    /// This method processes operation stacking by having the broader-scoped operation consume the narrower one.\
    /// The order of passed operations when calling this method is **very important**, do not reverse it!\
    /// Operation A (`superior`) can only affect operation B (`affectedCandidate`)
    /// if the item targeted by B is some form of descendant of A,
    /// in which case A can, in some cases, consume B. Here are all potential situations determined and solved by
    /// this method:
    /// - Both A and B are `TREE`-wide operations; in this case, `NUKE` operations will consume `REBUILD_TREE` operations.
    /// If A and B perform the same operation, A consumes B.
    /// - Both A and B affect the same item; in this case, the wider-scoped operation will consume the other. Yes, this
    /// means that an operation of priority `LOAD` will consume operations of priority `TRANSFORM`.
    /// - If A's scope is wider than B's, A will consume B.
    /// - If B's scope is wider than A's, B will be queued as independent action. This is because A cannot consume B
    /// if A's scope is `DIRECT_CHILDREN` while B's scope is `ALL_DESCENDANTS` - B is recursive while A is not.
    /// @param affectedCandidate operation determined to *be affected by* the other
    /// @param superior operation determined to be *affecting* the other.
    /// @return `true` if the superior operation consumed the affected one, otherwise `false`
    private boolean consume(TreeOperation affectedCandidate, TreeOperation superior) {
        UUID consumer;
        if (superior.scope() == TreeOperation.Scope.TREE) {
            // Superior is a tree-wide operation, meaning it automatically consumes candidate UNLESS candidate
            // has a higher operation priority
            logger.info("Superior operation affects the whole tree");
            consumer = (superior.operation().getPriority() >= affectedCandidate.operation().getPriority()) ?
                    superior.uuid() : affectedCandidate.uuid();
        } else if (superior.scope().getPriority() > affectedCandidate.scope().getPriority()) {
            // Superior has a wider operation scope than candidate, meaning it automatically absorbs candidate
            logger.info("Superior's scope is wider than candidate's");
            consumer = superior.uuid();
        } else {
            // Candidate has a wider or equal scope than/to superior; superior cannot consume it,
            // but candidate, being a descendant of superior, cannot consume superior either
            logger.info("Scope of candidate is wider than or equal to superior's");
            consumer = null;
        }
        if (consumer == superior.uuid()) {
            logger.debug("Determined that superior consumes candidate");
            return true;
        }
        if (consumer == affectedCandidate.uuid()) {
            logger.debug("Determined that candidate consumes superior");
        }
        return false;
    }
}