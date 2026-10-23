package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.MainApplication;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.ItemRow;
import com.backtoend.file_management_system.service.I18n;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Controller responsible for displaying any external screens outside MainController,
 * as well as processing any user actions on those screens
 */
public class ModalController {
    private static final AppLogger logger = AppLogger.getLogger(ModalController.class);

    /**
     * Display an overview of imported items and allow the user to proceed with the import or cancel<br>
     * Loads the FXML file "import-overview.fxml" together with the corresponding ImportController
     * @param owner The window on which the modal should be displayed
     * @param list A list of ItemRows to be displayed on the overview
     * @return Whether the user wants to proceed with the import or discard scanned items
     * @throws IOException If the FXML file cannot be loaded properly
     */
    public static boolean showImportPreview(Window owner, ArrayList<ItemRow> list) throws IOException {
        logger.info("Attempting to load FXML file");
        I18n i = I18n.getInstance();

        // Loading FXML file
        FXMLLoader fxml = new FXMLLoader(
                MainApplication.class.getResource("import-overview.fxml")
        );
        Parent root = fxml.load();
        ImportController controller = fxml.getController();
        controller.setItems(list);

        // Preparing stage
        Stage stage = new Stage();
        stage.setTitle(i.get("modal.import.overview.title"));
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setScene(new Scene(root));
        logger.debug("Scene preparation complete");

        // Show stage and await user input
        stage.showAndWait();
        return controller.isAccepted();
    }
}
