package com.backtoend.file_management_system.model;

public class Item {
    private String path;
    private String displayName;
    private boolean isDir;

    // Constructor
    public Item(String path, boolean isDir) {
        this.path = path;
        this.displayName = path;
        this.isDir = isDir;
    }
    public Item(String path, boolean isDir, String name) {
        this.path = path;
        this.displayName = name;
        this.isDir = isDir;
    }

    // Getters, Setters
    public String getPath() {
        return this.path;
    }
    public String getDisplayName() {
        return this.displayName;
    }
    public boolean getIsDir() {
        return this.isDir;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public void setDisplayName(String name) {
        this.displayName = name;
    }
    public void setIsDir(boolean isDir) {
        this.isDir = isDir;
    }
}
