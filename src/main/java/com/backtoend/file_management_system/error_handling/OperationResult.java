package com.backtoend.file_management_system.error_handling;

import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.logging.SourceLocation;

import java.util.ArrayList;

public record OperationResult<T>(
        OperationStatus status,
        T value,
        ArrayList<FeedbackMsg> msgs,
        SourceLocation source,
        Throwable error
) {
    public enum OperationStatus {
        SUCCESS,
        CANCELED,
        WARNING,
        FAILED
    }
    
    // "Constructors"
    // region Complete constructor
    public static <T> OperationResult<T> create(
            OperationStatus status, T value, ArrayList<FeedbackMsg> msgs,
            SourceLocation source, Throwable error
    ) {
        return new OperationResult<>(status, value, msgs, source, error);
    }
    public static <T> OperationResult<T> create(
            OperationStatus status, T value, ArrayList<FeedbackMsg> msgs,
            SourceLocation source
    ) {
        return new OperationResult<>(status, value, msgs, source, null);
    }
    public static <T> OperationResult<T> create(
            OperationStatus status, T value, ArrayList<FeedbackMsg> msgs,
            Throwable error
    ) {
        SourceLocation source = AppLogger.getCaller();
        return new OperationResult<>(status, value, msgs, source, error);
    }
    public static <T> OperationResult<T> create(
            OperationStatus status, T value, ArrayList<FeedbackMsg> msgs
    ) {
        SourceLocation source = AppLogger.getCaller();
        return new OperationResult<>(status, value, msgs, source, null);
    }
    // endregion
    // region Successful operations
    public static <T> OperationResult<T> success(
            T value, ArrayList<FeedbackMsg> msgs,
            SourceLocation source, Throwable error
    ) {
        return create(OperationStatus.SUCCESS, value, msgs, source, error);
    }
    public static <T> OperationResult<T> success(
            T value, ArrayList<FeedbackMsg> msgs,
            SourceLocation source
    ) {
        return success(value, msgs, source, null);
    }
    public static <T> OperationResult<T> success(
            T value, ArrayList<FeedbackMsg> msgs,
            Throwable error
    ) {
        SourceLocation source = AppLogger.getCaller();
        return success(value, msgs, source, error);
    }
    public static <T> OperationResult<T> success(
            T value, ArrayList<FeedbackMsg> msgs
    ) {
        SourceLocation source = AppLogger.getCaller();
        return success(value, msgs, source, null);
    }
    // endregion
    // region Canceled operations
    public static <T> OperationResult<T> canceled(
            T value, ArrayList<FeedbackMsg> msgs,
            SourceLocation source, Throwable error
    ) {
        return create(OperationStatus.CANCELED, value, msgs, source, error);
    }
    public static <T> OperationResult<T> canceled(
            T value, ArrayList<FeedbackMsg> msgs,
            SourceLocation source
    ) {
        return success(value, msgs, source, null);
    }
    public static <T> OperationResult<T> canceled(
            T value, ArrayList<FeedbackMsg> msgs,
            Throwable error
    ) {
        SourceLocation source = AppLogger.getCaller();
        return success(value, msgs, source, error);
    }
    public static <T> OperationResult<T> canceled(
            T value, ArrayList<FeedbackMsg> msgs
    ) {
        SourceLocation source = AppLogger.getCaller();
        return success(value, msgs, source, null);
    }
    // endregion
    // region Operations that resulted in warnings
    public static <T> OperationResult<T> warning(
            T value, ArrayList<FeedbackMsg> msgs,
            SourceLocation source, Throwable error
    ) {
        return create(OperationStatus.WARNING, value, msgs, source, error);
    }
    public static <T> OperationResult<T> warning(
            T value, ArrayList<FeedbackMsg> msgs,
            SourceLocation source
    ) {
        return success(value, msgs, source, null);
    }
    public static <T> OperationResult<T> warning(
            T value, ArrayList<FeedbackMsg> msgs,
            Throwable error
    ) {
        SourceLocation source = AppLogger.getCaller();
        return success(value, msgs, source, error);
    }
    public static <T> OperationResult<T> warning(
            T value, ArrayList<FeedbackMsg> msgs
    ) {
        SourceLocation source = AppLogger.getCaller();
        return success(value, msgs, source, null);
    }
    // endregion
    // region Failed operations
    public static <T> OperationResult<T> failed(
            T value, ArrayList<FeedbackMsg> msgs,
            SourceLocation source, Throwable error
    ) {
        return create(OperationStatus.FAILED, value, msgs, source, error);
    }
    public static <T> OperationResult<T> failed(
            T value, ArrayList<FeedbackMsg> msgs,
            SourceLocation source
    ) {
        return success(value, msgs, source, null);
    }
    public static <T> OperationResult<T> failed(
            T value, ArrayList<FeedbackMsg> msgs,
            Throwable error
    ) {
        SourceLocation source = AppLogger.getCaller();
        return success(value, msgs, source, error);
    }
    public static <T> OperationResult<T> failed(
            T value, ArrayList<FeedbackMsg> msgs
    ) {
        SourceLocation source = AppLogger.getCaller();
        return success(value, msgs, source, null);
    }
    // endregion

    // Shortcuts to determine operation status
    public boolean isSuccess() {
        return this.status == OperationStatus.SUCCESS;
    }
    public boolean isCanceled() {
        return this.status == OperationStatus.CANCELED;
    }
    public boolean isWarning() {
        return this.status == OperationStatus.WARNING;
    }
    public boolean isFailed() {
        return this.status == OperationStatus.FAILED;
    }
    public ArrayList<String> getPrintableMsgs() {
        ArrayList<String> result = new ArrayList<>();
        msgs.forEach(m -> {
            result.add(m.getMsg());
        });
        return result;
    }

    public ArrayList<String> getLoggableMsgs() {
        ArrayList<String> result = new ArrayList<>();
        msgs.forEach(m -> {
            result.add(m.logMsg());
        });
        return result;
    }
}
