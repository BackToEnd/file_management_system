package com.backtoend.file_management_system;

import com.backtoend.file_management_system.controller.MainController;
import com.backtoend.file_management_system.controller.ToastController;
import com.backtoend.file_management_system.database.Database;
import com.backtoend.file_management_system.database.DatabaseInitializer;
import com.backtoend.file_management_system.enums.DbStatus;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.service.DirectoryHandler;
import com.backtoend.file_management_system.logging.LoggingConfig;
import com.backtoend.file_management_system.util.OperationResult;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Main application class and starting point of this application
 */
public class MainApplication extends Application {
    private final static AppLogger logger = AppLogger.getLogger(MainApplication.class);
    private static final Database database = new Database("filesystem.db");
    private static final DirectoryHandler directoryHandler = new DirectoryHandler();
    private static final ToastController toastController = new ToastController();

    @Override
    public void start(Stage stage) throws IOException {
        LoggingConfig.configure();

        // Showing initial stage
        logger.debug("Attempting to load main view FXML file");
        FXMLLoader fxml = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        logger.trace("FXMLLoader created");
        Scene scene = new Scene(fxml.load(), 700, 400);


        // Installing scene on a StackPane
        Parent root = scene.getRoot();
        StackPane overlayRoot = new StackPane();
        VBox toastLayer = new VBox(16);
        toastLayer.setAlignment(Pos.BOTTOM_RIGHT);
        toastLayer.setPadding(new Insets(16, 20, 70, 16));
        toastLayer.setPickOnBounds(false);
        overlayRoot.getChildren().addAll(
                root, toastLayer
        );
        StackPane.setAlignment(toastLayer, Pos.BOTTOM_RIGHT);
        scene.setRoot(overlayRoot);

        // Configuring toast and modal controllers
        toastController.setToastLayer(toastLayer);

        stage.setTitle("File Management System");
        stage.setScene(scene);
        stage.show();

        // DB initialization on background thread
        Task<OperationResult<Void>> initializeDb = dbInitializationTask(fxml);
        Thread thread = new Thread(initializeDb);
        thread.setDaemon(true);
        thread.start();
    }

    // Getters
    public static Database getDatabaseInstance() { return database; }
    public static DirectoryHandler getDirectoryHandlerInstance() { return directoryHandler; }
    public static ToastController getToastControllerInstance() { return toastController; }

    // TODO: Implement proper error handling
    /**
     * Creates a Task responsible for initiating the DB, receives and logs a result upon completion,
     * and loads the overview tree on success
     * @param fxml FXMLLoader of MainController
     * @return Task
     */
    private Task<OperationResult<Void>> dbInitializationTask(FXMLLoader fxml) {
        DatabaseInitializer dbInit = new DatabaseInitializer();
        Task<OperationResult<Void>> initializeDb = new Task<>() {
            @Override
            protected OperationResult<Void> call() throws SQLException {
                return database.withConnection(dbInit::initiateDB);
            }
        };
        initializeDb.setOnSucceeded(event -> {
            OperationResult<Void> result = initializeDb.getValue();
            logger.result(result, "dbinitializationTask", this.getClass().getName());
            if (result.isSuccessful()) {
                MainController ctrl = fxml.getController();
                ctrl.enableOverview();
            } else {
                logger.warning("DB initialization could not be completed", "dbinitializationTask", this.getClass().getName());
            }
        });
        initializeDb.setOnFailed(event -> {
            logger.error(
                    "Database could not be initialized:",
                    "dbInitializationTask",
                    this.getClass().getName(),
                    initializeDb.getException()
            );
        });
        return initializeDb;
    }

    public static void main(String[] args) {
        launch();
    }
}
