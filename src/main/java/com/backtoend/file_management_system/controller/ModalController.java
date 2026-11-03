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

/// Controller class displaying modal windows above the main application window
/// @author Rogue049
public class ModalController {
    private static final AppLogger logger = AppLogger.getLogger(ModalController.class);
    /// Displays the preview of a directory selected by the user during the scan/import process.
    /// Allows the user to either proceed with the import, or reject and discard all scanned items.
    ///
    /// Loads the "import-overview.fxml" file and the corresponding `ImportController`.
    /// @param owner The window on which the import preview should be displayed
    /// @param list An `ItemRow` list of scanned items
    /// @return `true` if the user wants to proceed with the import, `false` if the user rejects the import
    /// @throws IOException If the FXML file cannot be loaded
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
