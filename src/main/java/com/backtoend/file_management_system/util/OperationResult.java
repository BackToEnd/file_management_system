package com.backtoend.file_management_system.util;


import com.backtoend.file_management_system.enums.OperationStatus;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.logging.SourceLocation;
import com.backtoend.file_management_system.service.FeedbackMsgContainer;

import java.util.ArrayList;

/// Record class keeping track of the outcome of an operation
/// @param status Operation {@link OperationStatus} defining the outcome of
///               the operation
/// @param value The value returned by the process creating this instance
/// @param msg A list of {@link FeedbackMsg}s containing status updates
/// @param <T> The datatype returned by the process creating this instance
/// @author Rogue049
public record OperationResult<T>(
        OperationStatus status,
        T value,
        ArrayList<FeedbackMsg> msg,
        SourceLocation source
) implements FeedbackMsgContainer {
    public boolean isSuccessful() {
        return status == OperationStatus.SUCCESS;
    }
    public boolean isCancelled() {
        return status == OperationStatus.CANCELED;
    }
    public boolean isFailed() {
        return status == OperationStatus.FAILED;
    }

    public static <T> OperationResult<T> create(OperationStatus status, T value, ArrayList<FeedbackMsg> msg) {
        return new OperationResult<>(status, value, msg, AppLogger.getCaller());
    }
    public static <T> OperationResult<T> create(OperationStatus status, T value, ArrayList<FeedbackMsg> msg, String sourceMethod, String sourceClass) {
        SourceLocation source = new SourceLocation(sourceClass, sourceMethod);
        return new OperationResult<>(status, value, msg, source);
    }

    @Override
    public ArrayList<FeedbackMsg> getAllMessages() {
        return this.msg;
    }
}
