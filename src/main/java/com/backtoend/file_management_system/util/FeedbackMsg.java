package com.backtoend.file_management_system.util;

import com.backtoend.file_management_system.service.I18n;

import java.text.MessageFormat;

/**
 * Record class keeping track of any kinds of process feedback
 * @param source Name of the class that created this message; relevant for message history
 * @param key Resource bundle translation key
 * @param args Any number of additional arguments, which will be formatted with the translation key
 */
public record FeedbackMsg(String source, String key, Object[] args) {
    /**
     * Formats the stored message
     * @return The formatted message
     */
    public String getMsg() {
        I18n i = I18n.getInstance();
        return MessageFormat.format(
                i.get(key), args
        );
    }

    /**
     * Formats the stored message into a loggable String
     * @return The stored message
     */
    public String logMsg() {
        return ("(" + this.source() + ") " + this.getMsg());
    }
}
