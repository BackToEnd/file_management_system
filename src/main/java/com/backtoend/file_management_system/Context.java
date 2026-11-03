package com.backtoend.file_management_system;

import com.backtoend.file_management_system.controller.ModalController;
import com.backtoend.file_management_system.controller.ToastController;
import com.backtoend.file_management_system.service.DirectoryHandler;
import com.backtoend.file_management_system.database.Database;

/// Record class keeping track of all globally available singletons
/// @param dirHandler {@link DirectoryHandler} instance
/// @param db {@link Database} instance
/// @param toast {@link ToastController} instance
/// @param modal {@link ModalController} instance
/// @deprecated Currently investigating a proper way to replace this thing with something more convenient
@Deprecated(forRemoval = true)
public record Context(
        DirectoryHandler dirHandler,
        Database db,
        ToastController toast,
        ModalController modal
) {
}
