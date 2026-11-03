package com.backtoend.file_management_system.logging;

import com.backtoend.file_management_system.util.OperationResult;

import java.util.ArrayList;
import java.util.logging.Level;

/// App-specific logger system, based on `java.util.logging.Logger`\
/// Currently supports both console and file logging (one file at a time), utilizes
/// the custom formatter {@link CompactFormatter}, and has several different log levels
/// @author Rogue049
public class AppLogger {

    private final java.util.logging.Logger logger;

    /// Defines a `java.util.logging.Logger` instance that will be utilized by this `AppLogger` instance.\
    /// The Logger instance uses the provided class name as its own name; see {@link LoggingConfig}
    /// for additional details on logging configuration.
    /// @param owner The class that is utilizing this `AppLogger` instance
    private AppLogger(Class<?> owner) {
        this.logger = java.util.logging.Logger.getLogger(owner.getName());
    }

    /// Creates a new `AppLogger` instance. Any class wanting to utilize this logging system should pass
    /// its own class into this method to gain access to a customized console and file logging system.
    /// @param owner The class creating this `AppLogger` instance
    /// @return A new `AppLogger` instance
    public static AppLogger getLogger(Class<?> owner) {
        return new AppLogger(owner);
    }

    // === All logging methods === \\

    /// Logs a regular log message
    /// @param msg String message to be logged
    public void info(String msg) {
        logger.info(msg);
    }

    /// Logs a warning message
    /// @param msg String message to be logged
    public void warning(String msg) {
        logger.warning(msg);
    }

    /// Logs a severe message
    /// @param msg String message to be logged
    public void severe(String msg) {
        logger.severe(msg);
    }

    /// Logs a config message
    /// @param msg String message to be logged
    public void config(String msg) {
        logger.config(msg);
    }

    /// Logs a debug message (not visible on the console by default)
    /// @param msg String message to be logged
    public void debug(String msg) {
        logger.fine(msg);
    }

    /// Logs a message on the lowest-priority logging level (`FINEST`)
    /// @param msg String message to be logged
    public void trace(String msg) {
        logger.finest(msg);
    }

    /// Logs an error message, as well as a throwable error
    /// @param msg String message to be logged
    /// @param error Throwable responsible for this log message
    public void error(String msg, Throwable error) {
        logger.log(Level.SEVERE, msg, error);
    }

    /// Logs a debug message and throwable error on second-lowest logging level (`FINE`)
    /// @param msg String message to be logged
    /// @param error Throwable to be logged
    public void debug(String msg, Throwable error) {
        logger.log(Level.FINE, msg, error);
    }

    /// Logs an operation result. Logging level depends on operation status:
    /// - A `SUCCESSFUL` operation is logged at second-lowest level (`FINE`)
    /// - A `CANCELED` operation is logged at info level
    /// - Any other operation status (`FAILED`, `WARNING`) is logged at warning level
    /// @param className The name of the class that has received this operation result
    /// @param res The received operation result
    public void result(String className, OperationResult<?> res) {
        String source;
        if (className.contains(".")) {
            source = className.substring(className.lastIndexOf('.'));
        } else {
            source = className;
        }
        ArrayList<String> msgs = res.getLoggableMessages();
        logger.info("Received operation result from {" + source + "}");
        if (res.isSuccessful()) {
            logger.info("[" + source + "] Operation completed successfully");
            msgs.forEach(m -> {
                logger.fine("[" + source + "] - " + m);
            });
        } else if (res.isCancelled()) {
            logger.info("[" + source + "] Operation was canceled:");
            msgs.forEach(m -> {
                logger.info("[" + source + "] - " + m);
            });
        } else {
            logger.warning("Operation failed with status {" + res.status().toString() + "}:");
            msgs.forEach(m -> {
                logger.warning("[" + source + "] - " + m);
            });
        }

    }
}
