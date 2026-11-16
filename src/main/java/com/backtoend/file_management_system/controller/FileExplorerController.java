package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.error_handling.FeedbackMsg;
import com.backtoend.file_management_system.error_handling.OperationResult;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.service.DirectoryHandler;
import com.backtoend.file_management_system.util.I18n;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.util.ArrayList;

public class FileExplorerController {
    private static final AppLogger logger = AppLogger.getLogger(FileExplorerController.class);
    private final DirectoryHandler dirHandler;
    private I18n i = I18n.getInstance();

    public FileExplorerController(DirectoryHandler dirHandler) {
        this.dirHandler = dirHandler;
    }

    public String openDirectoryChooser(Window window) {
        logger.info("Initiating directory chooser");

        // Setting up directory chooser
        Stage stage = (Stage) window;
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle(i.get("modal.chooser.directory.title"));
        File defaultDir = new File(dirHandler.getLastDir());
        dirChooser.setInitialDirectory(defaultDir);

        // Show directory chooser
        File selection = dirChooser.showDialog(stage);
        return selection.getAbsolutePath();
    }
}
