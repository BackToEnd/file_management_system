package com.backtoend.file_management_system.error_handling;

import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.logging.SourceLocation;
import com.backtoend.file_management_system.util.I18n;

import java.text.MessageFormat;

public record FeedbackMsg(
        String key, Object[] args, SourceLocation source
) {
    // "Constructor"
    public static FeedbackMsg create(String key, Object[] args) {
        return new FeedbackMsg(key, args, AppLogger.getCaller());
    }

    public String getMsg() {
        I18n i = I18n.getInstance();
        return MessageFormat.format(
                i.get(key), args
        );
    }

    public String logMsg() {
        return (
                "Message: [" + source.getEffectiveClassName() + " / "
                + source.sourceMethod() + "] " + getMsg()
                );
    }
}
