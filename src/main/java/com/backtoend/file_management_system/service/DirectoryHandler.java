package com.backtoend.file_management_system.service;

import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.ScannedItem;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Locale;

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
    public static String normalizePath(File path) {
        return path.toPath()
                .toAbsolutePath()
                .normalize()
                .toString();
    }
    public static String toComparison(File path) {
        return normalizePath(path).toLowerCase(Locale.ROOT);
    }
    public static String escapePath(String path) {
        return path
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
    public ArrayList<ScannedItem> parseDirectory(String path) throws IOException {
        logger.info("Parsing directory: " + path);
        ArrayList<ScannedItem> items = new ArrayList<>();
        Path absolutePath = Path.of(path);
        try (DirectoryStream<Path> children = Files.newDirectoryStream(absolutePath)) {
            for (Path item : children) {
                File f = item.toFile();
                String filePath = normalizePath(f);
                String fileType = (isDir(filePath)) ? "Folder" : "File";
                long lastModified = f.lastModified();
                long size = f.length();
                items.add(new ScannedItem(
                        f.getAbsoluteFile().getName(), filePath, fileType, lastModified, size, false
                ));
            }
        }
        return items;
    }

    public ScannedItem parseParent(String path) {
        logger.info("Getting info on parent path: " + path);
        File f = new File(path);
        String fileType = (isDir(path)) ? "Folder" : "File";
        long lastModified = f.lastModified();
        long size = f.length();
        String[] content = f.list();
        String normalizedPath = normalizePath(f);
        String name = normalizedPath.substring(normalizedPath.lastIndexOf('\\') + 1);
        boolean isParent = (content != null && content.length > 0);
        return new ScannedItem(
                name, normalizedPath, fileType, lastModified, size, isParent
        );
    }

    // === Private Helper Methods === \\
    private boolean isDir(String path) {
        File f = new File(path);
        return f.isDirectory();
    }
}
