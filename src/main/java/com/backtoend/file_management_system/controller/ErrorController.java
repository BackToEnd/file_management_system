package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.util.I18n;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ErrorController {
    @FXML
    private Label errorHeader;
    @FXML
    private Label errorDesc;
    @FXML
    private Button buttonConfirm;

    private final I18n i = I18n.getInstance();

    @FXML
    private void initialize() {
        errorHeader.textProperty().bind(i.bind("modal.error.header.generic"));
        errorDesc.textProperty().bind(i.bind("modal.error.body.generic"));
        buttonConfirm.textProperty().bind(i.bind("modal.error.button"));
    }

    @FXML
    private void onConfirm() {
        Stage stage = (Stage) errorDesc.getScene().getWindow();
        stage.close();
    }

    // Getters, setters
    public void bindHeader(String header) {
        errorHeader.textProperty().unbind();
        errorHeader.textProperty().bind(i.bind(header));
    }
    public void setBody(String text) {
        this.errorDesc.setText(text);
    }
    public Window getWindow() {
        return this.errorHeader.getScene().getWindow();
    }
}
