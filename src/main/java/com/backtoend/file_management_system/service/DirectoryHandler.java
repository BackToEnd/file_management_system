package com.backtoend.file_management_system.service;

import java.io.File;

public class DirectoryHandler {
    private final String home;
    private String lastDir;

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

    // Checking whether a file path exists
    public boolean checkPath(String path) {
        File f = new File(path);
        return f.exists();
    }

    // Checking whether a given path is a directory or a file
    public boolean isDir(String path) {
        File f = new File(path);
        return f.isDirectory();
    }
}
