package com.backtoend.file_management_system.logging;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Custom formatter class for AppLogger
 */
public class CompactFormatter extends Formatter {
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
                    .withZone(ZoneId.systemDefault());

    // Set custom message formatter
    @Override
    public String format(LogRecord record) {
        String time = TIME_FORMAT.format(Instant.ofEpochMilli(record.getMillis()));
        String level = record.getLevel().getName();
        String source = record.getLoggerName();

        if (source != null && source.contains(".")) {
            source = source.substring(source.lastIndexOf('.') + 1);
        }

        String msg = formatMessage(record);
        StringBuilder output = new StringBuilder()
                .append('[').append(time).append("] ")
                .append('[').append(level).append("] ")
                .append('[').append(source).append("] ")
                .append(msg)
                .append(System.lineSeparator());

        // Handling exceptions
        if (record.getThrown() != null) {
            java.io.StringWriter buffer = new java.io.StringWriter();
            try (java.io.PrintWriter writer = new java.io.PrintWriter(buffer)) {
                record.getThrown().printStackTrace(writer);
            }
            output.append(buffer);
        }
        return output.toString();
    }
}
