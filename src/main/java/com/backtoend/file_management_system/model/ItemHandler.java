package com.backtoend.file_management_system.model;

import com.backtoend.file_management_system.logging.AppLogger;

import java.util.ArrayList;
import java.util.List;

public class ItemHandler {
    private final List<Item> itemList;
    private final String path;
    private static final AppLogger logger = AppLogger.getLogger(ItemHandler.class);

    // Constructor
    public ItemHandler() {
        logger.debug("Instance created");
        this.itemList = new ArrayList<>();
        this.path = null;
    }
    public ItemHandler(String path) {
        this.path = path;
        this.itemList = new ArrayList<>();
    }

    // Getters, Setters
    public List<Item> getItemList() {
        return this.itemList;
    }
    public String getPath() {
        return this.path;
    }

    /**
     * Add a new item to this ItemHandler
     * @param path The complete file path, NOT the item name
     * @param name Item name, NOT the file path
     * @param isDir Whether the item is a directory
     */
    public void addItem(String path, String name, boolean isDir) {
        logger.debug("Adding item to list: " + path + ", " + name + ", " + isDir);
        this.itemList.add(
                new Item(path, isDir, name)
        );
    }

    // === Utility Methods === \\

    /**
     * Returns the names of all items
     * @return ArrayList of all item names (String)
     */
    public ArrayList<String> getAllItemNames() {
        ArrayList<String> names = new ArrayList<>();
        for (Item item : this.itemList) {
            names.add(item.getDisplayName());
        }
        return names;
    }
}
