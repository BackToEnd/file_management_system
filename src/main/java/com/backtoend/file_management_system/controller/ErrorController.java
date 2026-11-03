package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.service.I18n;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.ArrayList;

/// Controller class used to display critical errors to the user
/// @author Rogue049
public class ErrorController {
    /// The first line of the error message, ideally a short description of what went wrong
    @FXML
    private Label errorHeader;
    /// The description of the error, formatted as a list of individual progress update messages
    @FXML
    private Label errorDesc;
    /// Button to close the error modal
    @FXML
    private Button buttonConfirm;

    /// A list of messages, which will be displayed in the error description
    private ArrayList<String> messages;
    /// I18n instance to handle resource bundle keys
    private final I18n i = I18n.getInstance();

    /// Default initialization of this controller at first creation - binds text properties
    /// of header, body, and button
    @FXML
    private void initialize() {
        errorHeader.textProperty().bind(i.text("modal.error.header.generic"));
        errorDesc.textProperty().bind(i.text("modal.error.desc.generic"));
        buttonConfirm.textProperty().bind(i.text("modal.error.button"));
    }

    /// Closes the error modal
    @FXML
    private void onConfirm() {
        Stage stage = (Stage) errorDesc.getScene().getWindow();
        stage.close();
    }

    /// Configures header and body of the error modal. Both attributes may be `null`
    ///
    /// @param header Translation key for the error header
    /// @param messages List of formatted error messages to display, not translation keys
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
