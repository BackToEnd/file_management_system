package com.backtoend.file_management_system.logging;

import com.backtoend.file_management_system.util.ErrorResult;
import com.backtoend.file_management_system.util.FeedbackMsg;
import com.backtoend.file_management_system.util.OperationResult;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.logging.Level;

/// App-specific logger system, based on `java.util.logging.Logger`\
/// Currently supports both console and file logging (one file at a time), utilizes
/// the custom formatter {@link CompactFormatter}, and has several different log levels
/// @author Rogue049
public class AppLogger {

    private final java.util.logging.Logger logger;
    private static final StackWalker STACK_WALKER = StackWalker.getInstance();

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

    // General Utility Methods

    /// Fetches the approximate class and method name that called this method
    /// @return {@link java.lang.StackWalker.StackFrame} that called this method, excluding any `AppLogger` methods
    private static StackWalker.StackFrame getCallerFrame() {
        return STACK_WALKER.walk(frames ->
                frames.filter(frame ->!isInternalFrame(frame))
                        .findFirst().orElseThrow()
        );
    }

    /// Fetches class and method name of the caller
    /// @return {@link SourceLocation} of the caller
    public static SourceLocation getCaller() {
        StackWalker.StackFrame caller = getCallerFrame();
        return new SourceLocation(
                caller.getClassName(),
                caller.getMethodName()
        );
    }

    // TODO: Find a fix for lambda expressions related to async processes showing up weird
    /// Helper function for {@link getCallerFrame}, which would otherwise have too many conditions to be convenient.\
    /// Filters out whether the passed frame belongs to an internal process that should be ignored as a source.
    /// @param frame The frame to be checked
    /// @return `true` if the frame belongs to any internal class (javafx, java, jdk, sun),
    /// or a relevant logging or record class ({@link AppLogger}, {@link OperationResult}, {@link FeedbackMsg}, {@link ErrorResult}),
    /// otherwise returns `false`.
    private static boolean isInternalFrame(StackWalker.StackFrame frame) {
        String className = frame.getClassName();
        String methodName = frame.getMethodName();

        return className.equals(AppLogger.class.getName())
//                || methodName.startsWith("lambda$")
                || className.equals(FeedbackMsg.class.getName())
                || className.equals(OperationResult.class.getName())
                || className.equals(ErrorResult.class.getName());
    }
    // === All logging methods === \\

    /// Logs a regular log message
    /// @param msg String message to be logged
    public void info(String msg) {
        StackWalker.StackFrame caller = getCallerFrame();
        logger.logp(
                Level.INFO,
                caller.getClassName(),
                caller.getMethodName(),
                msg
        );
    }

    /// Logs a warning message
    /// @param msg String message to be logged
    public void warning(String msg) {
        StackWalker.StackFrame caller = getCallerFrame();
        logger.logp(
                Level.WARNING,
                caller.getClassName(),
                caller.getMethodName(),
                msg
        );
    }

    /// Logs a severe message
    /// @param msg String message to be logged
    public void severe(String msg) {
        StackWalker.StackFrame caller = getCallerFrame();
        logger.logp(
                Level.SEVERE,
                caller.getClassName(),
                caller.getMethodName(),
                msg
        );
    }

    /// Logs a config message
    /// @param msg String message to be logged
    public void config(String msg) {
        StackWalker.StackFrame caller = getCallerFrame();
        logger.logp(
                Level.CONFIG,
                caller.getClassName(),
                caller.getMethodName(),
                msg
        );
    }

    /// Logs a debug message (not visible on the console by default)
    /// @param msg String message to be logged
    public void debug(String msg) {
        StackWalker.StackFrame caller = getCallerFrame();
        logger.logp(
                Level.FINE,
                caller.getClassName(),
                caller.getMethodName(),
                msg
        );
    }

    /// Logs a message on the lowest-priority logging level (`FINEST`)
    /// @param msg String message to be logged
    public void trace(String msg) {
        StackWalker.StackFrame caller = getCallerFrame();
        logger.logp(
                Level.FINEST,
                caller.getClassName(),
                caller.getMethodName(),
                msg
        );
    }

    /// Logs an error message, as well as a throwable error
    /// @param msg String message to be logged
    /// @param error Throwable responsible for this log message
    public void error(String msg, Throwable error) {
        StackWalker.StackFrame caller = getCallerFrame();
        logger.logp(
                Level.SEVERE,
                caller.getClassName(),
                caller.getMethodName(),
                msg,
                error
        );
    }

    /// Logs a debug message and throwable error on second-lowest logging level (`FINE`)
    /// @param msg String message to be logged
    /// @param error Throwable to be logged
    public void debug(String msg, Throwable error) {
        StackWalker.StackFrame caller = getCallerFrame();
        logger.logp(
                Level.FINE,
                caller.getClassName(),
                caller.getMethodName(),
                msg,
                error
        );
    }

    /// Logs an operation result. Logging level depends on operation status:
    /// - A `SUCCESSFUL` operation is logged at second-lowest level (`FINE`)
    /// - A `CANCELED` operation is logged at info level
    /// - Any other operation status (`FAILED`, `WARNING`) is logged at warning level
    /// @param res The received operation result
    public void result(OperationResult<?> res) {
        // Preparing details about the caller
        StackWalker.StackFrame caller = getCallerFrame();
        String className;
        String methodName = caller.getMethodName();
        if (caller.getClassName().contains(".")) {
            className = caller.getClassName()
                    .substring(caller.getClassName()
                            .lastIndexOf('.') + 1
                    );
        } else {
            className = caller.getClassName();
        }
        logger.info("Received operation result from [" + res.source().getEffectiveClassName() + " > " + res.source().sourceMethod() + "]");

        // Preparing log level and announcement message
        Level resultLevel;
        Level msgLevel;
        String announceResult;
        String resultProviderClass = res.source().getEffectiveClassName();
        String resultProviderMethod = res.source().sourceMethod();
        switch (res.status()) {
            case SUCCESS:
                resultLevel = Level.INFO;
                msgLevel = Level.FINE;
                announceResult = " - Concluded successfully";
                break;
            case CANCELED:
                resultLevel = Level.INFO;
                msgLevel = Level.FINE;
                announceResult = " - Canceled by user";
                break;
            case WARNING:
                resultLevel = Level.WARNING;
                msgLevel = Level.INFO;
                announceResult = " - Unable to perform operation";
                break;
            case FAILED:
                resultLevel = Level.SEVERE;
                msgLevel = Level.SEVERE;
                announceResult = " - Critical error encountered; unable to proceed";
                break;
            default:
                resultLevel = Level.WARNING;
                msgLevel = Level.INFO;
                announceResult = " - Unknown operation status";
        }

        // Logging announcement
        logger.logp(
                resultLevel,
                className,
                methodName,
                MessageFormat.format(
                        "Result: [{0} / {1}]", new Object[] {
                                resultProviderClass, resultProviderMethod
                        }
                ) + announceResult
        );

        // Logging contained messages
        ArrayList<String> msg = res.getLoggableMessages();
        msg.forEach(m -> {
            logger.logp(
                    msgLevel,
                    className,
                    methodName,
                    m
            );
        });
    }
}
