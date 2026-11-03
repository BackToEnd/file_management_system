package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.MainApplication;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.service.I18n;
import com.backtoend.file_management_system.service.ScanImport;
import com.backtoend.file_management_system.util.OperationResult;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.fxml.FXML;
import javafx.stage.Window;

    /// Main controller class handling the main app interface
    /// @author Rogue049
public class MainController {
    /// `OverviewController` instance defined within this controller's FXML file
    @FXML
    private OverviewController overviewController;
    /// Button that triggers the scan/import process
    @FXML
    private Button browseBtn;
    /// Button that causes the application to shut down
    @FXML
    private Button killBtn;

    private static final AppLogger logger = AppLogger.getLogger(MainController.class);
    /// Locally available `ScanImport` instance
    private final ScanImport scanImport;

    // Constructor
        public MainController() {
            scanImport = new ScanImport();
        }
    @FXML
    private void initialize() {
        logger.info("Initiating main application interface");
        I18n i = I18n.getInstance();
        logger.trace("Creating ScanImport instance");
        logger.trace("Attempting to configure buttons");
        browseBtn.textProperty().bind(i.text("main.button.browse"));
        killBtn.textProperty().bind(i.text("main.button.terminate"));
    }

    /// Initiates the scan/import process
    /// @param event The event created by the pressed import button
    @FXML
    private void onChooseDirectory(ActionEvent event) {
        logger.info("Initiating ScanImport");
        Window window = (Window) ((Button) event.getSource()).getScene().getWindow();
        this.scanImport.handleScanImport(window,itemId -> overviewController.addNewParent(itemId)
        );
    }

    /// Shuts down the application
    @FXML
    private void onExit() {
        logger.info("Application shutting down");
        Platform.exit();
    }

    // === Utility Methods === \\
    /// Generates the overview tree; is called by `MainApplication` after DB initialization finishes
    ///  successfully and should **not** be called before the initialization finishes
    public void enableOverview() {
        OperationResult<Void> result = this.overviewController.buildTree();
        logger.result(result);
        MainApplication.getToastControllerInstance().showGeneric(
                result.status(), "toast.main.overview.build"
        );
    }
}
