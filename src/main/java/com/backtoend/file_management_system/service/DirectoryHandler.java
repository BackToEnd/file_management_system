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

/**
 * Class responsible for anything related to directory parsing
 */
public class DirectoryHandler {
    private final String home;
    private String lastDir;
    private static final AppLogger logger = AppLogger.getLogger(DirectoryHandler.class);

    // Constructor
    public DirectoryHandler() {
        logger.debug("Instance created");
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
    /**
     * Checks whether the given path is a directory<br>
     * Does not check whether the path exists or is a valid file path
     * @param path The path to be checked
     * @return whether the path is a directory
     */
    public static boolean isDir(String path) {
        logger.debug("Checking if path '" + path + "' is a directory");
        File f = new File(path);
        return f.isDirectory();
    }

    /**
     * Parses the given directory's child items into a list of ItemRows
     * @param path Directory path to be scanned
     * @return A list of all scanned child items
     * @throws IOException If the file operation cannot be executed
     */
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

    /**
     * Scans the information about the specified parent path
     * @param path Path of the parent directory
     * @return ItemRow for the parent directory
     * @throws IOException If the file operation cannot be executed
     */
    public static ItemRow parseParent(String path) throws IOException {
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
