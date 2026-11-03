package com.backtoend.file_management_system.util;

import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.logging.SourceLocation;
import com.backtoend.file_management_system.service.I18n;

import java.text.MessageFormat;

// FIXME: Message logging, usages

/// Record class keeping track of operation feedback messages
/// @param source Name of the class that created this instance
/// @param key Translation key describing what process caused this
///            message to be created
/// @param args Additional arguments that will later be formatted
///             into the message
/// @author Rogue049
public record FeedbackMsg(String key, Object[] args, SourceLocation source) {

    public static FeedbackMsg create(String key, Object[] args) {
        return new FeedbackMsg(
                key, args, AppLogger.getCaller()
        );
    }
    /// Transforms the message into a properly formatted message,
    /// including all passed arguments
    /// @return The formatted message
    public String getMsg() {
        I18n i = I18n.getInstance();
        return MessageFormat.format(
                i.get(key), args
        );
    }

    /// Transforms the message into a loggable String containing
    /// its source as well as the formatted message content
    /// @return Loggable String
    public String logMsg() {
        return (
                "Message: [" + source.getEffectiveClassName() + " / "
                + source.sourceMethod() + "] " + getMsg()
                );
    }
}
