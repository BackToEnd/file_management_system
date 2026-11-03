package com.backtoend.file_management_system.util;

import com.backtoend.file_management_system.enums.ErrorStatus;
import com.backtoend.file_management_system.enums.ErrorType;
import com.backtoend.file_management_system.service.FeedbackMsgContainer;

import java.util.ArrayList;

/// Record class keeping track of the result produced by an error-handling
/// process
/// @param status Operation {@link ErrorStatus} defining whether an error was
///               caught or not
/// @param type Operation {@link ErrorType} defining what kind of error
///             was caught
/// @param value The value returned by the process this instance was created for
/// @param message A list of {@link FeedbackMsg}s containing status updates
/// @param error A throwable error if one was caught
/// @param <T> The datatype returned by the process this instance was
///           created for
/// @author Rogue049
public record ErrorResult<T>(
        ErrorStatus status,
        ErrorType type,
        T value,
        ArrayList<FeedbackMsg> message,
        Throwable error
) implements FeedbackMsgContainer {
    public boolean isSuccessful() { return status == ErrorStatus.SUCCESS; }
    public boolean isCancelled() { return status == ErrorStatus.CANCELED; }
    public boolean isFailed() { return status == ErrorStatus.FAILED; }
    public boolean isExcepted() { return status == ErrorStatus.EXCEPTED; }
    public boolean errorIsGeneric() { return type == ErrorType.GENERIC; }
    public boolean errorIsSpecific() { return type == ErrorType.SPECIFIC; }
    public boolean errorIsCustom() { return type == ErrorType.CUSTOM; }

    @Override
    public ArrayList<FeedbackMsg> getAllMessages() {
        return this.message;
    }
}
