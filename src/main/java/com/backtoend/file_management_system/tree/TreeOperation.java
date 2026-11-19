package com.backtoend.file_management_system.tree;

import com.backtoend.file_management_system.error_handling.OperationResult;
import com.backtoend.file_management_system.model.IItemInformation;

public record TreeOperation(
        IItemInformation affectedItem,
        Priority priority,
        Scope scope
) {
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
        TREE,
        ALL_DESCENDANTS,
        DIRECT_CHILDREN,
        SELF
    }
    public interface ITreeOperationCallback<T> {
        T update(OperationResult<T> result);
    }
}
