package com.backtoend.file_management_system.logging;

import com.backtoend.file_management_system.util.OperationResult;

import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Custom app logger class with custom logging handlers and levels
 */
public class AppLogger {

    private final java.util.logging.Logger logger;

    private AppLogger(Class<?> owner) {
        this.logger = java.util.logging.Logger.getLogger(owner.getName());
    }

    public static AppLogger getLogger(Class<?> owner) {
        return new AppLogger(owner);
    }

    // === All logging methods === \\
    public void info(String msg) {
        logger.info(msg);
    }
    public void warning(String msg) {
        logger.warning(msg);
    }
    public void severe(String msg) {
        logger.warning(msg);
    }
    public void config(String msg) {
        logger.config(msg);
    }
    public void debug(String msg) {
        logger.fine(msg);
    }
    public void trace(String msg) {
        logger.finest(msg);
    }
    public void error(String msg, Throwable error) {
        logger.log(Level.SEVERE, msg, error);
    }
    public void debug(String msg, Throwable error) {
        logger.log(Level.FINE, msg, error);
    }
    public void result(String source, OperationResult<?> res) {
        ArrayList<String> msgs = res.logAllMsg();
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
