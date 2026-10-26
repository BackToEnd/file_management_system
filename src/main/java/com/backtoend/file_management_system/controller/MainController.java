package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.Context;
import com.backtoend.file_management_system.MainApplication;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.ItemRow;
import com.backtoend.file_management_system.service.ScanImport;
import com.backtoend.file_management_system.util.OperationResult;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.fxml.FXML;
import javafx.stage.Window;

/**
 * Main controller class responsible for displaying any content on the main app window,
 * as well as connecting different other controllers and workflows initiated
 * by user action
 */
public class MainController {
    @FXML
    private Label pathLabel;
    @FXML
    private OverviewController overviewController;
    private final Context context;
    private static final AppLogger logger = AppLogger.getLogger(MainController.class);

    // Constructor
    public MainController() {
        this.context = MainApplication.getContext();
    }

    @FXML
    private void initialize() {
        OperationResult<Void> showOverview = this.overviewController.buildTree();
        logger.result(this.getClass().getName(), showOverview);
        this.context.toast().showGeneric(
                showOverview.status(), "toast.main.overview.build"
        );
    }

    @FXML
    private void onChooseDirectory(ActionEvent event) {
        logger.info("Initiating ScanImport");
        Window window = (Window) ((Button) event.getSource()).getScene().getWindow();
        ScanImport scanImport = new ScanImport(this.context);
        OperationResult<ItemRow> result = scanImport.initiateScanImport(window);
        logger.result(this.getClass().getName(), result);
        this.context.toast().showGeneric(
                result.status(), "toast.main.scan.import"
        );
        if (result.isSuccessful()) {
            this.overviewController.addNewParent(result.value());
        }
    }

    @FXML
    private void onExit() {
        logger.info("Application shutting down");
        Platform.exit();
    }

    // === Utility Methods === \\
}
