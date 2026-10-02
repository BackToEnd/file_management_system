package com.backtoend.file_management_system.controller;

import javafx.stage.DirectoryChooser;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.File;
import com.backtoend.file_management_system.service.DirectoryHandler;

public class FileExplorerController {
    private final DirectoryHandler dirHandler;

    // Constructor
    public FileExplorerController(DirectoryHandler dirHandler) {
        this.dirHandler = dirHandler;
    }

    // Display DirectoryChooser dialog
    public String openDirectoryChooser(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select a Directory");

        // Set initial directory for DirectoryChooser
        File defaultDir = new File(dirHandler.getLastDir());

        // Show DirectoryChooser and return it
        File selection = dirChooser.showDialog(stage);
        if (selection != null) {
            return selection.getAbsolutePath();
        } else {
            return null;
        }
    }
}
