package com.backtoend.file_management_system;

import com.backtoend.file_management_system.service.DirectoryHandler;
import com.backtoend.file_management_system.database.Database;

public record Context(DirectoryHandler dirHandler, Database db) {
}
