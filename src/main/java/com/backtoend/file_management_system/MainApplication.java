package com.backtoend.file_management_system;

import com.backtoend.file_management_system.controller.MainController;
import com.backtoend.file_management_system.controller.OverviewController;
import com.backtoend.file_management_system.database.Database;
import com.backtoend.file_management_system.database.DatabaseInitializer;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.service.DirectoryHandler;
import com.backtoend.file_management_system.logging.LoggingConfig;
import com.backtoend.file_management_system.util.OperationResult;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Main application class and starting point of this application
 */
public class MainApplication extends Application {
    private final static AppLogger logger = AppLogger.getLogger(MainApplication.class);
    @Override
    public void start(Stage stage) throws IOException {
        LoggingConfig.configure();

        // Preparation
        Database db = new Database("filesystem.db");
        DirectoryHandler dir = new DirectoryHandler();
        Context context = new Context(dir, db);

        // Initialize DB
        DatabaseInitializer dbInit = new DatabaseInitializer(context);
        OperationResult<Void> initStatus = dbInit.initiateDB();
        // TODO: Actually handle situations where DB-initialization fails lol
        logger.result(this.getClass().getName(), initStatus);

        // Showing initial stage
        FXMLLoader fxml = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        fxml.setControllerFactory(type -> {
            if (type == MainController.class) {
                return new MainController(context);
            } else if(type == OverviewController.class) {
                return new OverviewController(context);
            }
            throw new IllegalArgumentException(
                    "Unknown controller: " + type.getName()
            );
        });
        Scene scene = new Scene(fxml.load(), 700, 400);
        stage.setTitle("File Management System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
