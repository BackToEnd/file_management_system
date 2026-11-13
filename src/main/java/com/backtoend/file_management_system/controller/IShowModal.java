package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.MainApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;

public interface IShowModal {
    String getTitle();
    Window getOwner();
    Modality getModality();
    String getFxmlFile();

    default FXMLLoader loadFxml() throws IOException {
        return new FXMLLoader(
                MainApplication.class.getResource(getFxmlFile())
        );
    }

    default Parent getRoot(FXMLLoader fxml) throws IOException {
        return fxml.load();
    }


    default Stage setStage(FXMLLoader fxml, Parent root) throws IOException {
        Stage stage = new Stage();
        stage.setTitle(getTitle());
        stage.initOwner(getOwner());
        stage.initModality(getModality());
        stage.setScene(new Scene(root));
        return stage;
    }
}
