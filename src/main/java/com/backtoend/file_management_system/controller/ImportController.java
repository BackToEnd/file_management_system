package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.logging.AppLogger;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.ArrayList;

public class ImportController {
    @FXML
    private ListView<String> listView;
    private boolean accepted = false;
    private static final AppLogger logger = AppLogger.getLogger(ImportController.class);

    public ImportController() {
        logger.debug("Instance created");
    }

    // Getters, Setters
    public boolean isAccepted() {
        return this.accepted;
    }

    // === Utility Methods === \\
    public void setItems(ArrayList<String> list) {
        this.listView.setItems(FXCollections.observableArrayList(list));
    }
    private void close() {
        Stage stage = (Stage) listView.getScene().getWindow();
        stage.close();
    }

    @FXML
    protected void handleImport() {
        logger.info("Selection accepted, proceeding to DB entries");
        this.accepted = true;
        this.close();
    }
    @FXML
    protected void cancelImport() {
        logger.info("Selection canceled");
        this.close();
    }
}
