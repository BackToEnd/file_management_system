package com.backtoend.file_management_system.util;


import com.backtoend.file_management_system.enums.OperationStatus;

import java.util.ArrayList;

/**
 * Record class keeping track of the result of a complex operation, a return type, and any feedback messages
 * @param status The success status of the operation
 * @param value A value that would normally have been returned by the operation that returns an instance of this class
 * @param msg A list of feedback messages
 * @param <T> The return type of an operation that returns an instance of this class
 */
public record OperationResult<T>(
        OperationStatus status,
        T value,
        ArrayList<FeedbackMsg> msg
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

    /**
     * Formats all stored messages into a format that can easily be displayed to the user
     * @return List of all stored messages
     */
    public ArrayList<String> getAllMsgs() {
        ArrayList<String> res = new ArrayList<>();
        this.msg.forEach(m -> {
            res.add(m.getMsg());
        });
        return res;
    }

    /**
     * Formats all stored messages into a format that can be used for logging
     * @return List of all stored messages
     */
    public ArrayList<String> logAllMsg() {
        ArrayList<String> res = new ArrayList<>();
        this.msg.forEach(m -> {
            res.add(m.logMsg());
        });
        return res;
    }
}
