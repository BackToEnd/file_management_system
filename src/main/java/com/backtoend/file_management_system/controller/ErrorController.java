package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.util.I18n;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;

/// Controller class implementing {@link IModalController}.\
/// The modal controller associated with {@link com.backtoend.file_management_system.error_handling.ErrorHandler}.
/// @author Rogue049
public class ErrorController implements IModalController {
    @FXML
    private Label errorHeader;
    @FXML
    private Label errorDesc;
    @FXML
    private Button buttonConfirm;
    @FXML
    private VBox listContainer;

    /// A String list of messages displayed on the modal
    private ListView<String> msgList;

    /// {@link I18n} instance
    private final I18n i = I18n.getInstance();

    @FXML
    private void initialize() {
        errorHeader.setText(i.get("modal.error.header.generic"));
        errorDesc.setText(i.get("modal.error.body.generic"));
        buttonConfirm.setText(i.get("modal.error.button"));
    }

    @FXML
    private void onConfirm() {
        Stage stage = (Stage) errorDesc.getScene().getWindow();
        stage.close();
    }

    // Getters, setters

    /// Configures the text shown on the header label.
    /// @param header an ordinary String
    public void setHeader(String header) {
        this.errorHeader.setText(header);
    }

    /// Configures the text shown in the body label.
    /// @param text an ordinary String, ideally containing some info as to
    ///             what caused an error. Should be informative to the user.
    public void setBody(String text) {
        this.errorDesc.setText(text);
    }

    /// Configures the text shown in the message list. This method should only
    /// be used if a message list is supposed to be displayed. If no list
    /// is provided, do not call this method.
    /// @param messages Non-null String list
    public void setMsgList(ArrayList<String> messages) {
        ObservableList<String> observable = FXCollections.observableArrayList(messages);
        msgList = new ListView<>(observable);
        listContainer.getChildren().add(msgList);
    }
}
