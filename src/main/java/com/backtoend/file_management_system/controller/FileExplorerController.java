package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.util.FeedbackMsg;
import com.backtoend.file_management_system.util.OperationResult;
import com.backtoend.file_management_system.enums.OperationStatus;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.ArrayList;
import com.backtoend.file_management_system.service.DirectoryHandler;
import javafx.stage.Window;

/// Controller class handling any kinds of directory and file choosers
/// @author Rogue049
public class FileExplorerController {
    /// `DirectoryHandler` instance utilized by this controller
    private final DirectoryHandler dirHandler;
    private static final AppLogger logger = AppLogger.getLogger(DirectoryHandler.class);

    // Constructor
    public FileExplorerController(DirectoryHandler dirHandler) {
        this.dirHandler = dirHandler;
    }

//    /**
//     * Opens a directory chooser and allows the user to choose a path
//     * @param window The window on which the directory chooser should be displayed
//     * @return Operation status and selected path
//     */
    ///
    /// Opens a new DirectoryChooser, allowing the user to pick a directory
    /// @param window The window on which the DirectoryChooser should be displayed
    /// @return Operation status and selected path; status will be `CANCELLED` if the user
    /// closed the window without selecting a path
    public OperationResult<String> openDirectoryChooser(Window window) {
        ArrayList<FeedbackMsg> list = new ArrayList<>();
        logger.info("Initiating directory chooser");

        // Setting up new DirectoryChooser
        Stage stage = (Stage) window;
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select a Directory");

        // Set initial directory for DirectoryChooser
        File defaultDir = new File(dirHandler.getLastDir());
        logger.debug("Identified last directory: [ " + defaultDir + "]");
        dirChooser.setInitialDirectory(defaultDir);

        // Show DirectoryChooser
        File selection = dirChooser.showDialog(stage);

        // Check whether the user selected a path or canceled
        if (selection != null) {
            dirHandler.setLastDir(selection.getPath());
            list.add(FeedbackMsg.create(
                    "explorer.directory.result.obtained",
                    new Object[] { selection.getAbsolutePath() }
            ));
            return OperationResult.create(OperationStatus.SUCCESS, selection.getAbsolutePath(), list);
        } else {
            list.add(FeedbackMsg.create(
                            "explorer.directory.result.canceled",
                            new Object[] {})
            );
            return OperationResult.create(OperationStatus.CANCELED, null, list);
        }
    }
}
