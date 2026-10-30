package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.service.I18n;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.ArrayList;

/**
 * Controller class responsible for displaying certain errors to the user
 */
public class ErrorController {
    @FXML
    private Label errorHeader;
    @FXML
    private Label errorDesc;
    @FXML
    private Button buttonConfirm;

    private ArrayList<String> messages;
    private final I18n i = I18n.getInstance();

    @FXML
    private void initialize() {
        errorHeader.textProperty().bind(i.text("modal.error.header.generic"));
        errorDesc.textProperty().bind(i.text("modal.error.desc.generic"));
        buttonConfirm.textProperty().bind(i.text("modal.error.button"));
    }

    @FXML
    private void onConfirm() {
        Stage stage = (Stage) errorDesc.getScene().getWindow();
        stage.close();
    }

    /**
     * Configures the entire error display, including header and body<br>
     * Both `header` and `messages` may be `null` to leave default values.
     * @param header Translation key for the error header
     * @param messages List of formatted error messages to display, not translation keys
     */
    public void setup(String header, ArrayList<String> messages) {
        if (header != null) {
            this.errorHeader.textProperty().bind(i.text(header));
        }
        if (messages != null) {
            String desc = String.join("\n", messages);
            this.errorDesc.setText(desc);
        }
    }
}
