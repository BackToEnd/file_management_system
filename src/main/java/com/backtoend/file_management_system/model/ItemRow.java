package com.backtoend.file_management_system.model;

import com.backtoend.file_management_system.util.ItemStatus;

import java.io.File;

public class ItemRow {
    private final int id;
    private final String name;
    private final String path;
    private final String displayName;
    private final String fileType;
    private final String lastModified;
    private final double size;
    private final ItemStatus status;
    private final int parent;

    // Constructor
    public ItemRow(int id, String name, String path, String fileType) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.fileType = fileType;
        this.displayName = name;
        this.lastModified = "Unknown";
        this.size = 0.0;
        this.status = ItemStatus.ACTIVE;
        this.parent = -1;
    }
    public ItemRow(int id, String name, String path, String fileType, String displayName) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.fileType = fileType;
        this.displayName = displayName;
        this.lastModified = "Unknown";
        this.size = 0.0;
        this.status = ItemStatus.ACTIVE;
        this.parent = -1;
    }
    public ItemRow(int id, String name, String path, String fileType, String displayName, String lastModified, double size, ItemStatus status) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.fileType = fileType;
        this.displayName = displayName;
        this.lastModified = lastModified;
        this.size = size;
        this.status = status;
        this.parent = -1;
    }
    public ItemRow(int id, String name, String path, String fileType, String displayName, String lastModified, double size, ItemStatus status, int parent) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.fileType = fileType;
        this.displayName = displayName;
        this.lastModified = lastModified;
        this.size = size;
        this.status = status;
        this.parent = parent;
    }

    // Getters, Setters
    public int getId() {
        return this.id;
    }
    public String getName() {
        return this.name;
    }
    public String getPath() {
        return this.path;
    }
    public String getDisplayName() {
        return this.displayName;
    }
    public String getFileType() {
        return this.fileType;
    }
    public String getLastModified() {
        return this.lastModified;
    }
    public double getSize() {
        return this.size;
    }
    public ItemStatus getStatus() {
        return this.status;
    }
    public int getParent() {
        return this.parent;
    }
}
