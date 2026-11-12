package com.backtoend.file_management_system;

import com.backtoend.file_management_system.controller.ErrorHandler;
import com.backtoend.file_management_system.database.Database;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.logging.LoggingConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main application class and starting point of this application
 */
public class MainApplication extends Application {
    private final static AppLogger logger = AppLogger.getLogger(MainApplication.class);
    private static final Database db = new Database("filesystem.db");
    private static final ErrorHandler errorHandler = new ErrorHandler();

    @Override
    public void start(Stage stage) throws IOException {
        LoggingConfig.configure();

        // Loading main application
        logger.debug("Attempting to load main view FXML file");
        FXMLLoader fxml = new FXMLLoader(
                MainApplication.class.getResource("main-view.fxml")
        );
        Scene scene = new Scene(fxml.load(), 700, 400);

        // Installing the scene on a stack pane; relevant for toasts
        Parent root = scene.getRoot();
        StackPane overlay = new StackPane();
        VBox toastLayer = new VBox(16);
        toastLayer.setAlignment(Pos.BOTTOM_RIGHT);
        toastLayer.setPadding(new Insets(16, 20, 70, 16));
        toastLayer.setPickOnBounds(false);
        overlay.getChildren().addAll(
                root,toastLayer
        );
        StackPane.setAlignment(toastLayer, Pos.BOTTOM_RIGHT);
        scene.setRoot(overlay);

        // Configuring stage
        stage.setTitle("File Management System");
        stage.setScene(scene);
        stage.show();
    }

    // Getters, Setters
    public static Database getDbInstance() {
        return db;
    }
    public static ErrorHandler getErrorHandler() {
        return errorHandler;
    }
    
    public static void main(String[] args) {
        launch();
    }
}
