package com.backtoend.file_management_system;

import com.backtoend.file_management_system.service.DirectoryHandler;
import com.backtoend.file_management_system.database.Database;

/**
 * Record class keeping track of class instances that should persist and be universally accessible by all other classes
 * while the application is running
 * @param dirHandler DirectoryHandler instance
 * @param db Database instance
 */
public record Context(DirectoryHandler dirHandler, Database db) {
}
