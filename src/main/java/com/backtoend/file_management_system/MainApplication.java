package com.backtoend.file_management_system;

import com.backtoend.file_management_system.controller.MainController;
import com.backtoend.file_management_system.controller.ModalController;
import com.backtoend.file_management_system.controller.OverviewController;
import com.backtoend.file_management_system.controller.ToastController;
import com.backtoend.file_management_system.database.Database;
import com.backtoend.file_management_system.database.DatabaseInitializer;
import com.backtoend.file_management_system.enums.ToastType;
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

/**
 * Main application class and starting point of this application
 */
public class MainApplication extends Application {
    private final static AppLogger logger = AppLogger.getLogger(MainApplication.class);
    private static Context context;
    @Override
    public void start(Stage stage) throws IOException {
        LoggingConfig.configure();

        // Preparation
        Database db = new Database("filesystem.db");
        DirectoryHandler dir = new DirectoryHandler();
        ToastController toast = new ToastController();
        ModalController modal = new ModalController();
        Context context = new Context(dir, db, toast, modal);
        MainApplication.context = context;

        // Showing initial stage
        FXMLLoader fxml = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
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
        toast.setToastLayer(toastLayer);
        modal.setRoot(overlayRoot);

        stage.setTitle("File Management System");
        stage.setScene(scene);
        stage.show();

        // DB initialization on background thread
        Task<OperationResult<Void>> initializeDb = getOperationResultTask(context, fxml);
        Thread thread = new Thread(initializeDb);
        thread.setDaemon(true);
        thread.start();
    }

    // TODO: Implement proper error handling
    /**
     * Creates a Task responsible for initiating the DB, receives and logs a result upon completion,
     * and loads the overview tree on success.
     * @param context AppContext instance
     * @param fxml FXMLLoader of MainController
     * @return Task
     */
    private Task<OperationResult<Void>> getOperationResultTask(Context context, FXMLLoader fxml) {
        DatabaseInitializer dbInit = new DatabaseInitializer(context);
        Task<OperationResult<Void>> initializeDb = new Task<>() {
            @Override
            protected OperationResult<Void> call() {
                return dbInit.initiateDB();
            }
        };
        initializeDb.setOnSucceeded(event -> {
            OperationResult<Void> result = initializeDb.getValue();
            logger.result(this.getClass().getName(), result);
            if (result.isSuccessful()) {
                MainController ctrl = fxml.getController();
                ctrl.enableOverview();
            } else {
                logger.warning("DB initialization failed horribly");
            }
        });
        return initializeDb;
    }

    /**
     * Allows any component to get the AppContext singleton from anywhere
     * @return This session's AppContext instance
     */
    public static Context getContext() {
        return MainApplication.context;
    }

    public static void main(String[] args) {
        launch();
    }
}
