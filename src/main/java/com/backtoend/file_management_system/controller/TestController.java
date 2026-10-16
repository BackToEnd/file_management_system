package com.backtoend.file_management_system.controller;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.backtoend.file_management_system.service.DirectoryHandler;
import com.backtoend.file_management_system.database.DatabaseInitializer;

public class TestController extends Application {
    private final BorderPane parent = new BorderPane();

    @Override
    public void start(Stage stage) throws Exception {
        this.setupStage(stage);
    }

    @Override
    public void init() throws Exception {
        super.init();
        this.buildUI();
    }

    private void buildUI() {
        Label header = new Label("Header");
        Label leftSidebar = new Label("Left Sidebar");
        Label content = new Label("Main Content");
        Label rightSidebar = new Label("Right Sidebar");
        Label footer = new Label("Footer");

        this.parent.setTop(header);
        this.parent.setLeft(leftSidebar);
        this.parent.setCenter(content);
        this.parent.setRight(rightSidebar);
        this.parent.setBottom(footer);
    }

    private void setupStage(Stage stage) {
        Scene scene = new Scene(this.parent, 640, 480);
        stage.setTitle("Test");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
}
