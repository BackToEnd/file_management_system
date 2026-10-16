package com.backtoend.file_management_system.logging;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppLogger {

    private final java.util.logging.Logger logger;

    private AppLogger(Class<?> owner) {
        this.logger = java.util.logging.Logger.getLogger(owner.getName());
    }

    public static AppLogger getLogger(Class<?> owner) {
        return new AppLogger(owner);
    }

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
}
