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
        SELF (3);
        private final int priority;
        Scope(int priority) { this.priority = priority; }
        public int getPriority() { return this.priority; }
    }
    public enum Operations {
        NUKE,
        REBUILD_TREE,
        REBUILD_BRANCH,
        RECURSIVE_REBUILD,
        TRANSFORM_SELF,
        LOAD_CHILDREN
    }
    public interface ITreeOperationCallback<T> {
        T update(OperationResult<T> result);
    }
}
