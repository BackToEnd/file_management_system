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

        // Initialize DB
        DatabaseInitializer dbInit = new DatabaseInitializer(context);
        OperationResult<Void> initStatus = dbInit.initiateDB();
        // TODO: Actually handle situations where DB-initialization fails lol
        logger.result(this.getClass().getName(), initStatus);

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

        toast.showGeneric(initStatus.status());
    }

    public static Context getContext() {
        return MainApplication.context;
    }

    public static void main(String[] args) {
        launch();
    }
}
