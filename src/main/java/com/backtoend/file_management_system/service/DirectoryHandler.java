package com.backtoend.file_management_system.service;

import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.ScannedItem;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class DirectoryHandler {
    private static final AppLogger logger = AppLogger.getLogger(DirectoryHandler.class);
    private String lastDir;

    public DirectoryHandler() {
        this.lastDir = System.getProperty("user.home");
    }

    // Getters, Setters
    public String getLastDir() {
        return this.lastDir;
    }
    public void setLastDir(String lastDir) {
        this.lastDir = lastDir;
    }

    // === Utility Methods === \\
    public ArrayList<ScannedItem> parseDirectory(String path) throws IOException {
        logger.info("Parsing directory: " + path);
        ArrayList<ScannedItem> items = new ArrayList<>();
        Path absolutePath = Path.of(path);
        try (DirectoryStream<Path> children = Files.newDirectoryStream(absolutePath)) {
            for (Path item : children) {
                File f = item.toFile();
                String filePath = f.getAbsolutePath();
                String fileType = (isDir(filePath)) ? "Folder" : "File";
                long lastModified = f.lastModified();
                long size = f.length();
                items.add(new ScannedItem(
                        f.getAbsoluteFile().getName(), filePath, fileType, lastModified, size
                ));
            }
        }
        return items;
    }

    public ScannedItem parseParent(String path) {
        logger.info("Getting info on parent path: " + path);
        File f = new File(path);
        String name = f.getAbsoluteFile().getName();
        String fileType = (isDir(path)) ? "Folder" : "File";
        long lastModified = f.lastModified();
        long size = f.length();
        return new ScannedItem(
                name, path, fileType, lastModified, size
        );
    }

    // === Private Helper Methods === \\
    private boolean isDir(String path) {
        File f = new File(path);
        return f.isDirectory();
    }
}
