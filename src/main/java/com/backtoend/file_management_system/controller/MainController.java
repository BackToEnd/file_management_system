package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.util.I18n;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Window;

/// Main controller class. Handles user interactions with main application elements,
/// initiates relevant processes, maintains all other base UI controllers.
/// @author Rogue049
public class MainController {
    private static final AppLogger logger = AppLogger.getLogger(MainController.class);
    private final I18n i = I18n.getInstance();

    // region FXML elements
    /// {@link OverviewController} instance
    @FXML
    private OverviewController overviewController;
    /// Pressing this button initiates a scan-import process
    @FXML
    private Button browseBtn;
    /// Pressing this button terminates the application
    @FXML
    private Button killBtn;
    // endregion

    /// Constructor. Creates a new `MainController` instance and configures all required attributes.
    public MainController() {
        // Initialization things
    }

    /// Initialization of this controller. Creates string bindings for
    /// all main UI elements.
    @FXML
    private void initialize() {
        logger.info("Initiating main application interface");
        browseBtn.textProperty().bind(i.bind("main.buttons.browse"));
        killBtn.textProperty().bind(i.bind("main.buttons.kill"));
    }

    // TODO: Link scan-import process in Javadoc comment
    /// Initiate the scan-import process.
    /// @param event event created by the import button being pressed
    @FXML
    private void onChooseDirectory(ActionEvent event) {
        logger.info("Initiating scan-import sequence");
        Window window = ((Button) event.getSource()).getScene().getWindow();
        // TODO: Hook up scan-import workflow
    }

    // TODO: Make sure this method works with any ongoing background processes
    /// Shut down the application
    @FXML
    private void onExit() {
        logger.info("Application shutting down");
        Platform.exit();
    }

    /// Begins generating the overview tree after the DB initialization
    /// completes. Should **not** be called before the initialization finishes.
    public void enableOverview() {
        // TODO: Call a method that begins building the overview tree
    }
}
