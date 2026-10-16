package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.logging.AppLogger;
import javafx.stage.DirectoryChooser;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.File;
import com.backtoend.file_management_system.service.DirectoryHandler;
import javafx.stage.Window;

public class FileExplorerController {
    private final DirectoryHandler dirHandler;
    private static final AppLogger logger = AppLogger.getLogger(DirectoryHandler.class);
    // Constructor
    public FileExplorerController(DirectoryHandler dirHandler) {
        this.dirHandler = dirHandler;
        logger.debug("Instance created");
    }

    /**
     * Opens a DirectoryChooser and returns the selected directory
     * @param window Where the DirectoryChooser should be displayed
     * @return The selected directory
     */
    public String openDirectoryChooser(Window window) {
        logger.info("Initiating directory chooser");
        Stage stage = (Stage) window;
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select a Directory");

        // Set initial directory for DirectoryChooser
        File defaultDir = new File(dirHandler.getLastDir());
        logger.debug("Identified last directory: [ " + defaultDir + "]");
        dirChooser.setInitialDirectory(defaultDir);

        // Show DirectoryChooser
        File selection = dirChooser.showDialog(stage);
        if (selection != null) {
            logger.info("User selection obtained: [ " + selection + "]");

            return selection.getAbsolutePath();
        } else {
            logger.info("Selection canceled");
            return null;
        }
    }
}
