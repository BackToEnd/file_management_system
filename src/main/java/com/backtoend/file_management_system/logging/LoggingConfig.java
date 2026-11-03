package com.backtoend.file_management_system.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/// Logging config initializer that defines logging levels and handlers
public final class LoggingConfig {
    /// The default logger namespace
    private static final String LOGGER_NAME = "com.backtoend.file_management_system";

    /// Configures the `java.util.logging.Logger` used application-wide.\
    /// By default, file logging is set to `Level.ALL`, console logging to `Level.FINE`.
    /// Applies {@link CompactFormatter} to both logging handlers. Logs will be written into `./logs/application.log`
    /// @throws IOException If it is unable to check whether the logging directory exists, or cannot create the logging directory
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
