package com.backtoend.file_management_system.enums;

public enum TreeOperationPriority {
    FOLLOWUP(0),
    REBUILD(1),
    RESCAN(2),
    TRANSFORM(3),
    LOAD(4);

    private final int priority;

    TreeOperationPriority(int priority) {
        this.priority = priority;
    }
    public int getPriority() {
        return this.priority;
    }
}
