package com.backtoend.file_management_system.util;

import com.backtoend.file_management_system.service.TreeOperationCallback;

public record QueuedTreeOperation(
        long generation,
        TreeOperationRequest request,
        TreeOperationCallback update
) {
}
