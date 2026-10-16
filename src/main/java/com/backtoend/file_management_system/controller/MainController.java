package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.Context;
import com.backtoend.file_management_system.database.DatabaseInsert;
import com.backtoend.file_management_system.model.ItemHandler;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.service.ScanImport;
import com.backtoend.file_management_system.util.ProcessFeedback;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.fxml.FXML;
import javafx.stage.Window;

import java.io.IOException;

public class MainController {
    @FXML
    private Label pathLabel;
    private final Context context;
    private static final AppLogger logger = AppLogger.getLogger(MainController.class);

    // Constructor
    public MainController(Context context) {
        this.context = context;
    }

    @FXML
    private void onChooseDirectory(ActionEvent event) {
        logger.info("Initiating ScanImport");
        Window window = (Window) ((Button) event.getSource()).getScene().getWindow();
        ScanImport scanImport = new ScanImport(this.context);
        ProcessFeedback result = scanImport.initiateScanImport(window);
        logger.info("Operation result: " + result.isSuccessful());
        logger.debug("Message: " + result.msg());
    }

    @FXML
    private void onExit() {
        logger.info("Application shutting down");
        Platform.exit();
    }

    // === Utility Methods === \\
}
