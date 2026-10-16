package com.backtoend.file_management_system.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class LoggingConfig {
    private static final String LOGGER_NAME = "com.backtoend.file_management_system";
    public static void configure() throws IOException {
        // Logging directory
        Path logDirectory = Path.of("logs");
        Files.createDirectories(logDirectory);
        Logger root = Logger.getLogger(LOGGER_NAME);

        // Logger hierarchy config
        root.setLevel(Level.ALL);
        root.setUseParentHandlers(false);
        for (var handler : root.getHandlers()) {
            root.removeHandler(handler);
            handler.close();
        }

        // Console output
        ConsoleHandler console = new ConsoleHandler();
        console.setLevel(Level.INFO);
        console.setFormatter(new CompactFormatter());

        // File output
        Path logPattern = logDirectory.resolve("application.log");
        FileHandler f = new FileHandler(
                logPattern.toString(),
                false
        );
        f.setLevel(Level.ALL);
        f.setFormatter(new CompactFormatter());

        // Adding all handlers
        root.addHandler(console);
        root.addHandler(f);
        root.info("Logger initialized");
        root.info("Log directory: " + logDirectory);
    }
}
