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
            Operations operation
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
        NONEXISTING_CHILDREN (3);
        private final int priority;
        Scope(int priority) { this.priority = priority; }
        public int getPriority() { return this.priority; }
    }
    public enum Operations {
        NUKE (0),
        REBUILD_TREE (1),
        RECURSIVE_REBUILD (2),
        REBUILD_BRANCH (3),
        LOAD_CHILDREN (4),
        TRANSFORM_SELF (5);
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
                yield Scope.DIRECT_CHILDREN;
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
    public int itemId() {
        return this.affectedItem.getItem().id();
    }
    public int maxScopeToConsume() {
        return switch (this.scope) {
            case TREE -> {
                yield 0;
            }
            case ALL_DESCENDANTS, DIRECT_CHILDREN -> {
                yield 1;
            }
            case NONEXISTING_CHILDREN -> {
                yield 2;
            }
        };
    }
    public int minConsumableScope() {
        return switch (this.scope) {
            case TREE -> {
                yield 0;
            }
            case ALL_DESCENDANTS -> {
                yield 1;
            }
            case DIRECT_CHILDREN -> {
                yield 3;
            }
            case NONEXISTING_CHILDREN -> {
                yield 4;    // Yup, this is an invalid number, but new NONEXISTENT_CHILDREN operations cannot consume anything
            }
        };
    }
}
