package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.helpers.ExceptionToString;
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

public class ErrorController {
    @FXML
    private Label errorHeader;
    @FXML
    private Label errorDesc;
    @FXML
    private Button buttonConfirm;
    @FXML
    private VBox listContainer;

    private ListView<String> msgList;

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
    public void setHeaderKey(String headerKey) {
        this.errorHeader.setText(i.get(headerKey));
    }
    public void setHeaderString(String header) {
        this.errorHeader.setText(header);
    }
    public void setBodyKey(String bodyKey) {
        this.errorDesc.setText(i.get(bodyKey));
    }
    public void setBodyString(String text) {
        this.errorDesc.setText(text);
    }
    public void setMsgList(ArrayList<String> messages) {
        ObservableList<String> observable = FXCollections.observableArrayList(messages);
        msgList = new ListView<>(observable);
        listContainer.getChildren().add(msgList);
    }
}
