package com.backtoend.file_management_system.logging;

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
    /// Helper function for `getCallerFrame``, which would otherwise have too many conditions to be convenient.\
    /// Filters out whether the passed frame belongs to an internal process that should be ignored as a source.
    /// @param frame The frame to be checked
    /// @return `true` if the frame belongs to any internal class (javafx, java, jdk, sun),
    /// or a relevant logging or record class ({@link AppLogger}, {@link OperationResult}, {@link FeedbackMsg}, {@link ErrorResult}),
    /// otherwise returns `false`.
    private static boolean isInternalFrame(StackWalker.StackFrame frame) {
        String className = frame.getClassName();
        String methodName = frame.getMethodName();

        return className.equals(AppLogger.class.getName()
//                || className.equals(FeedbackMsg.class.getName())
//                || className.equals(OperationResult.class.getName())
//                || className.equals(ErrorResult.class.getName()
                );
    }


    // === All logging methods === \\

    // region Logging info messages
    /// Logs a regular info message
    /// @param msg String message to be logged
    /// @param sourceMethod Name of the method that called this one; relevant for callback-
    ///                     and lambda-safe logging
    /// @param sourceClass Name of the class of the method that called this one; relevant for thread-safe logging
    public void info(String msg, String sourceMethod, String sourceClass) {
        logger.logp(
                Level.INFO,
                sourceClass,
                sourceMethod,
                msg
        );
    }
    /// Logs a regular info message
    /// @param msg String message to be logged
    /// @param sourceMethod Name of the method that called this one; relevant for callback-
    ///                     and lambda-safe logging
    public void info(String msg, String sourceMethod) {
        SourceLocation caller = getCaller();
        info(msg, sourceMethod, caller.getEffectiveClassName());
    }
    /// Logs a regular info message
    /// @param msg String message to be logged
    public void info(String msg) {
        SourceLocation caller = getCaller();
        info(msg, caller.sourceMethod(), caller.getEffectiveClassName());
    }
    // endregion

    // region Logging warning messages
    /// Logs a warning message
    /// @param msg String message to be logged
    /// @param sourceMethod Name of the method that called this one; relevant for callback-
    ///                     and lambda-safe logging
    /// @param sourceClass Name of the class of the method that called this one; relevant for thread-safe logging
    public void warning(String msg, String sourceMethod, String sourceClass) {
        logger.logp(
                Level.WARNING,
                sourceClass,
                sourceMethod,
                msg
        );
    }
    /// Logs a warning message
    /// @param msg String message to be logged
    /// @param sourceMethod Name of the method that called this one; relevant for callback-
    ///                     and lamda-safe logging
    public void warning(String msg, String sourceMethod) {
        SourceLocation caller = getCaller();
        warning(msg, sourceMethod, caller.getEffectiveClassName());
    }
    /// Logs a warning message
    /// @param msg String message to be logged
    public void warning(String msg) {
        SourceLocation caller = getCaller();
        warning(msg, caller.sourceMethod(), caller.getEffectiveClassName());
    }
    // endregion

    // region Logging error messages
    /// Logs an error or exception
    /// @param msg String message to be logged
    /// @param sourceMethod Name of the method that called this one; relevant for callback-
    ///                     and lambda-safe logging
    /// @param sourceClass Name of the class of the method that called this one; relevant for thread-safe logging
    /// @param error Throwable error
    public void error(String msg, String sourceMethod, String sourceClass, Throwable error) {
        logger.logp(
                Level.SEVERE,
                sourceClass,
                sourceMethod,
                msg,
                error
        );
    }
    /// Logs an error or exception
    /// @param msg String message to be logged
    /// @param sourceMethod Name of the method that called this one; relevant for callback-
    ///                     and lambda-safe logging
    /// @param sourceClass Name of the class of the method that called this one; relevant for thread-safe logging
    public void error(String msg, String sourceMethod, String sourceClass) {
        error(msg, sourceMethod, sourceClass, null);
    }/// Logs an error or exception
    /// @param msg String message to be logged
    /// @param sourceMethod Name of the method that called this one; relevant for callback-
    ///                     and lambda-safe logging
    /// @param error Throwable error
    public void error(String msg, String sourceMethod, Throwable error) {
        SourceLocation caller = getCaller();
        error(msg, sourceMethod, caller.getEffectiveClassName(), error);
    }
    /// Logs an error or exception
    /// @param msg String message to be logged
    /// @param sourceMethod Name of the method that called this one; relevant for callback-
    ///                     and lambda-safe logging
    public void error(String msg, String sourceMethod) {
        SourceLocation caller = getCaller();
        error(msg, sourceMethod, caller.getEffectiveClassName(), null);
    }
    /// Logs an error or exception
    /// @param msg String message to be logged
    /// @param error Throwable error
    public void error(String msg, Throwable error) {
        SourceLocation caller = getCaller();
        error(msg, caller.sourceMethod(), caller.getEffectiveClassName(), error);
    }
    /// Logs an error or exception
    /// @param msg String message to be logged
    public void error(String msg) {
        SourceLocation caller = getCaller();
        error(msg, caller.sourceMethod(), caller.getEffectiveClassName(), null);
    }
    // endregion

    // region Logging config messages
    /// Logs a config message
    /// @param msg String message to be logged
    /// @param sourceMethod Name of the method that called this one; relevant for callback-
    ///                     and lambda-safe logging
    /// @param sourceClass Name of the class of the method that called this one; relevant for thread-safe logging
    public void config(String msg, String sourceMethod, String sourceClass) {
        logger.logp(
                Level.CONFIG,
                sourceClass,
                sourceMethod,
                msg
        );
    }
    /// Logs a config message
    /// @param msg String message to be logged
    /// @param sourceMethod Name of the method that called this one; relevant for callback-
    ///                     and lambda-safe logging
    public void config(String msg, String sourceMethod) {
        SourceLocation caller = getCaller();
        config(msg, sourceMethod, caller.getEffectiveClassName());
    }
    /// Logs a config message
    /// @param msg String message to be logged
    public void config(String msg) {
        SourceLocation caller = getCaller();
        config(msg, caller.sourceMethod(), caller.getEffectiveClassName());
    }
    // endregion

    // region Logging debug messages
    /// Logs a debug message with an error
    /// @param msg String message to be logged
    /// @param sourceMethod Name of the method that called this one; relevant for callback-
    ///                     and lambda-safe logging
    /// @param sourceClass Name of the class of the method that called this one; relevant for thread-safe logging
    /// @param error Throwable error
    public void debug(String msg, String sourceMethod, String sourceClass, Throwable error) {
        logger.logp(
                Level.FINE,
                sourceClass,
                sourceMethod,
                msg,
                error
        );
    }
    /// Logs a debug message
    /// @param msg String message to be logged
    /// @param sourceMethod Name of the method that called this one; relevant for callback-
    ///                     and lambda-safe logging
    /// @param sourceClass Name of the class of the method that called this one; relevant for thread-safe logging
    public void debug(String msg, String sourceMethod, String sourceClass) {
        debug(msg, sourceMethod, sourceClass, null);
    }
    /// Logs a debug message with an error
    /// @param msg String message to be logged
    /// @param sourceMethod Name of the method that called this one; relevant for callback-
    ///                     and lambda-safe logging
    /// @param error Throwable error
    public void debug(String msg, String sourceMethod, Throwable error) {
        SourceLocation caller = getCaller();
        debug(msg, sourceMethod, caller.getEffectiveClassName(), error);
    }
    /// Logs a debug message
    /// @param msg String message to be logged
    /// @param sourceMethod Name of the method that called this one; relevant for callback-
    ///                     and lambda-safe logging
    public void debug(String msg, String sourceMethod) {
        SourceLocation caller = getCaller();
        debug(msg, sourceMethod, caller.getEffectiveClassName(), null);
    }
    /// Logs a debug message with an error
    /// @param msg String message to be logged
    /// @param error Throwable error
    public void debug(String msg, Throwable error) {
        SourceLocation caller = getCaller();
        debug(msg, caller.sourceMethod(), caller.getEffectiveClassName(), error);
    }
    /// Logs a debug message
    /// @param msg String message to be logged
    public void debug(String msg) {
        SourceLocation caller = getCaller();
        debug(msg, caller.sourceMethod(), caller.getEffectiveClassName(), null);
    }
    // endregion

    // region Logging trace messages
    /// Logs a trace message
    /// @param msg String message to be logged
    /// @param sourceMethod Name of the method that called this one; relevant for callback-
    ///                     and lambda-safe logging
    /// @param sourceClass Name of the class of the method that called this one; relevant for thread-safe logging
    public void trace(String msg, String sourceMethod, String sourceClass) {
        logger.logp(
                Level.FINEST,
                sourceClass,
                sourceMethod,
                msg
        );
    }
    /// Logs a trace message
    /// @param msg String message to be logged
    /// @param sourceMethod Name of the method that called this one; relevant for callback-
    ///                     and lambda-safe logging
    public void trace(String msg, String sourceMethod) {
        SourceLocation caller = getCaller();
        trace(msg, sourceMethod, caller.getEffectiveClassName());
    }
    /// Logs a trace message
    /// @param msg String message to be logged
    public void trace(String msg) {
        SourceLocation caller = getCaller();
        trace(msg, caller.sourceMethod(), caller.getEffectiveClassName());
    }
    // endregion

    // region Logging operation results
    /// Logs the content of an operation result
    /// @param result Operation result to be logged
    /// @param sourceMethod Name of the method that called this one; relevant for callback-
    ///                     and lambda-safe logging
    /// @param sourceClass Name of the class of the method that called this one; relevant for thread-safe logging
//    public void result(OperationResult<?> result, String sourceMethod, String sourceClass) {
//        // Gathering result source information
//        String resultProviderClass = result.source().getEffectiveClassName();
//        String resultProviderMethod = result.source().sourceMethod();
//
//        // Preparing log levels and announcement message
//        String announceResult;
//        TriConsumer<String, String, String> logResult;
//        TriConsumer<String, String, String> logMsg;
//        switch (result.status()) {
//            case SUCCESS:
//                logResult = this::info;
//                logMsg = this::debug;
//                announceResult = " - Concluded successfully";
//                break;
//            case CANCELED:
//                logResult = this::info;
//                logMsg = this::debug;
//                announceResult = " - Canceled by user";
//                break;
//            case WARNING:
//                logResult = this::warning;
//                logMsg = this::info;
//                announceResult = " - Unable to perform operation";
//                break;
//            case FAILED:
//                logResult = this::error;
//                logMsg = this::error;
//                announceResult = " - Critical error encountered; unable to proceed";
//                break;
//            default:
//                logResult = this::warning;
//                logMsg = this::info;
//                announceResult = " - Unknown operation status: {" + result.status().toString() + "}";
//        }
//
//        // Logging messages
//        logResult.accept(
//                MessageFormat.format(
//                        "Received operation result from [{0} > {1}] {2}",
//                        new Object[] { resultProviderClass, resultProviderMethod, announceResult}
//                ),
//                sourceMethod, sourceClass
//        );
//        ArrayList<String> feedback = result.getLoggableMessages();
//        feedback.forEach(msg -> {
//            logMsg.accept(
//                    msg,
//                    sourceMethod,
//                    sourceClass
//            );
//        });
//    }
//    /// Logs the content of an operation result
//    /// @param result Operation result to be logged
//    public void result(OperationResult<?> result) {
//        SourceLocation caller = getCaller();
//        result(result, caller.sourceMethod(), caller.getEffectiveClassName());
//    }
    // endregion
}
