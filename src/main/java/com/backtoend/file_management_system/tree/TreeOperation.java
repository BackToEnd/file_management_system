package com.backtoend.file_management_system.tree;

import com.backtoend.file_management_system.error_handling.OperationResult;
import com.backtoend.file_management_system.model.IItemInformation;

import java.util.UUID;

public record TreeOperation(
        IItemInformation affectedItem,
        Priority priority,
        Scope scope,
        Operations operation,
        UUID uuid,
        ITreeOperationCallback<?> callback
) {
    public record OperationRequest(
            IItemInformation affectedItem,
            Operations operation,
            CustomTreeItem.TreeItemStatus status
    ) {}
    public enum Priority {
        REBUILD (0),
        RESCAN (1),
        TRANSFORM(2),
        LOAD(3);
        private final int priority;
        Priority(int priority) {
            this.priority = priority;
        }
        public int getPriority() {
            return this.priority;
        }
    }
    public enum Scope {
        TREE (0),
        ALL_DESCENDANTS (1),
        DIRECT_CHILDREN (2),
        SELF (3);
        private final int priority;
        Scope(int priority) { this.priority = priority; }
        public int getPriority() { return this.priority; }
    }
    public enum Operations {
        NUKE (0),
        REBUILD_TREE (1),
        REBUILD_BRANCH (2),
        RECURSIVE_REBUILD (3),
        TRANSFORM_SELF (4),
        LOAD_CHILDREN (5);
        private final int priority;
        Operations(int priority) { this.priority = priority; }
        public int getPriority() { return this.priority; }
    }
    public interface ITreeOperationCallback<T> {
        T update(OperationResult<T> result);
    }

    // === Methods === \\
    public static TreeOperation create(
            OperationRequest request,
            ITreeOperationCallback<?> callback
    ) {
        Priority priority;
        Scope scope = switch (request.operation()) {
            case NUKE, REBUILD_TREE -> {
                priority = Priority.REBUILD;
                yield Scope.TREE;
            }
            case REBUILD_BRANCH -> {
                priority = Priority.RESCAN;
                yield Scope.DIRECT_CHILDREN;
            }
            case RECURSIVE_REBUILD -> {
                priority = Priority.RESCAN;
                yield Scope.ALL_DESCENDANTS;
            }
            case TRANSFORM_SELF -> {
                priority = Priority.TRANSFORM;
                yield Scope.SELF;
            }
            case LOAD_CHILDREN -> {
                priority = Priority.LOAD;
                yield Scope.DIRECT_CHILDREN;
            }
            case null, default -> throw new IllegalStateException(
                    "Unknown operation type: " + request.operation()
            );
        };
        UUID uuid = UUID.randomUUID();
        return new TreeOperation(
                request.affectedItem, priority, scope,
                request.operation(), uuid, callback
        );
    }
}
