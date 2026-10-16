package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.Context;
import com.backtoend.file_management_system.MainApplication;
import com.backtoend.file_management_system.logging.AppLogger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.io.IOException;
import java.util.ArrayList;

public class ModalController {
    private static final AppLogger logger = AppLogger.getLogger(ModalController.class);

    /**
     * Generate and display an import preview window
     * @param owner javafx.stage.Window of the scene the screen should be displayed to
     * @param list ArrayList of Strings to display
     * @return whether the import was accepted or denied
     * @throws IOException
     */
    public static boolean showImportPreview(Window owner, ArrayList<String> list) throws IOException {
        logger.info("Attempting to load FXML file");
        FXMLLoader fxml = new FXMLLoader(
                MainApplication.class.getResource("overview.fxml")
        );
        Parent root = fxml.load();
        ImportController controller = fxml.getController();
        controller.setItems(list);

        Stage stage = new Stage();
        stage.setTitle("Review Import");
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setScene(new Scene(root));
        logger.debug("Scene preparation complete");

        stage.showAndWait();
        return controller.isAccepted();
    }
}
