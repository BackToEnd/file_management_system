package com.backtoend.file_management_system.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.ItemRow;
import com.backtoend.file_management_system.enums.ItemStatus;

/// Helper service handling directories and file operations
/// @author Rogue049
public class DirectoryHandler {
    /// The user's home directory, identified through system properties
    private final String home;

    // TODO: Integrate this value into long-term settings so the user can resume exactly where they left off,
    //  even between app restarts
    /// The last directory the user selected
    private String lastDir;
    private static final AppLogger logger = AppLogger.getLogger(DirectoryHandler.class);

    // Constructor
    public DirectoryHandler() {
        this.home = System.getProperty("user.home");
        this.lastDir = this.home;
    }

    // Getters, Setters
    public String getHome() {
        return this.home;
    }
    public String getLastDir() {
        return this.lastDir;
    }
    public void setLastDir(String newDir) {
        this.lastDir = newDir;
    }


    // === Utility Methods === \\

    /// Checks whether the given path points to a directory or a file
    /// @param path The directory path to be checked
    /// @return `true` if the path points to a directory, otherwise `false`
    public static boolean isDir(String path) {
        logger.debug("Checking if path '" + path + "' is a directory");
        File f = new File(path);
        return f.isDirectory();
    }

    // TODO: Make async
    /// Parses the content of the given directory and creates a new `ItemRow` instance for each
    /// child of the path
    /// @param path Directory path to be parsed
    /// @return A list of `ItemRow` instances for all scanned child items
    /// @throws IOException If any file operation cannot be completed
    public static ArrayList<ItemRow> parseDir(String path) throws IOException {
        logger.info("Parsing directory: " + path);
        ArrayList<ItemRow> items = new ArrayList<>();
        Path absolutePath = Path.of(path);
        try (DirectoryStream<Path> children = Files.newDirectoryStream(absolutePath)) {
            for (Path item : children) {
                File f = item.toFile();
                String filePath = f.getAbsolutePath();
                String fileType = (isDir(filePath)) ? "Folder" : "File";
                long lastModified = f.lastModified();
                long size = f.length();
                items.add(new ItemRow(
                        -1, f.getAbsoluteFile().getName(), filePath,
                        f.getAbsoluteFile().getName(), fileType, lastModified,
                        size, ItemStatus.ACTIVE, null, false
                ));
            }
        }
        return items;
    }

    /// Creates an `ItemRow` instance for the given parent path
    /// @param path Path of a parsed directory
    /// @return `ItemRow` instance of the parent path
    public static ItemRow parseParent(String path)  {
        logger.info("Getting ItemRow for path: " + path);
        File f = new File(path);
        String name = f.getAbsoluteFile().getName();
        String fileType = (isDir(path)) ? "Folder" : "File";
        long lastModified = f.lastModified();
        long size = f.length();
        return new ItemRow(
                -1, name, path, name, fileType, lastModified, size, ItemStatus.ACTIVE,
                null, true
        );
    }
}
