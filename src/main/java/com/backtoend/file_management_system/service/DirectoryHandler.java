package com.backtoend.file_management_system.service;

import java.io.File;
import java.nio.file.Path;

import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.ItemHandler;

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
     * Checks whether the given path exists<br>
     * Does not check whether the path is a valid file path
     * @param path The path to be checked
     * @return whether the path exists
     */
    public static boolean checkPath(String path) {
        logger.debug("Checking if path '" + path + "' exists");
        File f = new File(path);
        return f.exists();
    }

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
     * Parses the content of the specified directory<br>
     * Returns null if the given path either does not exist or is not a directory
     * @param path The path to be parsed
     * @return ItemHandler with all parsed items, or null
     */
    public static ItemHandler parseDir(String path) {
        logger.info("Parsing directory: " + path);
        if (checkPath(path) && isDir(path)) {
            File f = new File(path);
            logger.info(path + " exists and is directory");
            String[] children = f.list();
            ItemHandler items = new ItemHandler(path);
            for (String item : children) {
                String completePath = Path.of(path, item).toString();
                items.addItem(
                        completePath,
                        item,
                        DirectoryHandler.isDir(completePath)
                );
            }
            return items;
        } else {
            logger.info(path + " is either nonexistent or not a directory");
            return null;
        }
    }
}
