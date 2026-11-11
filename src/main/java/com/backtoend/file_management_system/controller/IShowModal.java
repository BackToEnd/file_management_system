package com.backtoend.file_management_system.controller;

import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public interface IShowModal {
    String getTitle();
    Window getOwner();
    Modality getModality();
    Scene getRoot();

    default Stage setStage() {
        Stage stage = new Stage();
        stage.setTitle(getTitle());
        stage.initOwner(getOwner());
        stage.initModality(getModality());
        stage.setScene(getRoot());
        return stage;
    }
}
