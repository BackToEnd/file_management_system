package com.backtoend.file_management_system.util;

import java.util.List;

public record OperationResult<T>(
        OperationStatus status,
        T value,
        List<FeedbackMsg> messages
) {
    public boolean isSuccessful() {
        return status == OperationStatus.SUCCESS;
    }
    public boolean isCancelled() {
        return status == OperationStatus.CANCELLED;
    }
    public boolean isFailed() {
        return status == OperationStatus.FAILED;
    }
}
