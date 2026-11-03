package com.backtoend.file_management_system.service;

import com.backtoend.file_management_system.util.FeedbackMsg;

import java.util.ArrayList;

/// Interface providing default utility methods to {@link com.backtoend.file_management_system.util.ErrorResult}
/// and {@link com.backtoend.file_management_system.util.OperationResult}
public interface FeedbackMsgContainer {
    /// Provides a way for this interface to access a list of
    /// {@link FeedbackMsg}s contained within the implementing class
    /// @return All contained feedback messages
    ArrayList<FeedbackMsg> getAllMessages();

    /// Formats all stored messages into a format that can easily be
    /// displayed to the user
    /// @return String list of all stored messages
    default ArrayList<String> getPrintableMessages() {
        ArrayList<String> res = new ArrayList<>();
        getAllMessages().forEach(m -> {
            res.add(m.getMsg());
        });
        return res;
    }

    /// Formats all stored messages into a format that is suitable for
    /// logging
    /// @return Formatted list of all stored messages
    default ArrayList<String> getLoggableMessages() {
        ArrayList<String> res = new ArrayList<>();
        getAllMessages().forEach(m -> {
            res.add(m.logMsg());
        });
        return res;
    }
}
