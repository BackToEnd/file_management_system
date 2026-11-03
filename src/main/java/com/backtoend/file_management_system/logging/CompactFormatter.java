package com.backtoend.file_management_system.logging;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/// Custom formatter class for `AppLogger`
/// @author Rogue049
public class CompactFormatter extends Formatter {

    /// Defines how date and time should be formatted; default format is
    /// "HH:mm:ss.SSS", overridden by the system's default time zone ID
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
                    .withZone(ZoneId.systemDefault());

    // Set custom message formatter

    /// Overrides the `.format` method by adding a formatted time stamp, logging level,
    /// and the name of the source class. Can also properly take care of a throwable passed into the log record
    /// @param record The log record to be formatted.
    /// @return The formatted log record String
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
