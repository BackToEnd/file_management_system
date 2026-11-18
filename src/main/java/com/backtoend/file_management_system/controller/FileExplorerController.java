package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.MainApplication;
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

/// Controller class. Its sole responsibility is opening file explorers
/// and processing their output.
/// @author Rogue049
public class FileExplorerController {
    /// Logger instance
    private static final AppLogger logger = AppLogger.getLogger(FileExplorerController.class);
    /// {@link DirectoryHandler} instance
    private final DirectoryHandler dirHandler;
    /// {@link I18n} instance
    private I18n i = I18n.getInstance();

    /// Constructor. Grabs {@link DirectoryHandler} singleton through
    /// {@link MainApplication#getDirHandler()}.
    public FileExplorerController() {
        this.dirHandler = MainApplication.getDirHandler();
    }

    /// Opens a {@link DirectoryChooser} and allows the user to select a
    /// directory.
    /// @param window where the directory chooser should be opened
    /// @return String value of the path selected by the user. `null` if the
    /// selection process was canceled.
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
        return DirectoryHandler.normalizePath(selection);
    }
}
