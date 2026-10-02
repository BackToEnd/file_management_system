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

public class MainController extends Application {
    private final BorderPane parent = new BorderPane();
    private final DirectoryHandler dirHandler = new DirectoryHandler();
    private final FileExplorerController explorer = new FileExplorerController(dirHandler);

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
        Button button = new Button("Browse...");
        Label label = new Label("No directory selected");
        button.setOnAction(event -> this.onBrowse(event, label));
        VBox container = new VBox(15, button, label);
        container.setAlignment(Pos.CENTER);
        this.parent.setCenter(container);
    }

    private void setupStage(Stage stage) {
        Scene scene = new Scene(this.parent, 640, 480);
        stage.setTitle("Directory Chooser Test");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    private void onBrowse(ActionEvent event, Label label) {
        String path = this.explorer.openDirectoryChooser(event);
        if (path != null) {
            label.setText("Directory: " + path);
        } else {
            label.setText("Directory selection canceled");
        }
    }
}
