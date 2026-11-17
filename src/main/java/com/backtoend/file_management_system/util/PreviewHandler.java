package com.backtoend.file_management_system.util;

import com.backtoend.file_management_system.controller.IShowModal;
import com.backtoend.file_management_system.controller.ImportController;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.ScannedItem;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.ArrayList;

public class PreviewHandler implements IShowModal {
    private final static AppLogger logger = AppLogger.getLogger(PreviewHandler.class);
    private final I18n i = I18n.getInstance();

    private String modalTitle = "modal.import.preview.title.default";
    private final Modality modality = Modality.WINDOW_MODAL;
    private final String fxmlFile = "import-overview.fxml";
    private Window owner;
    private boolean isAvailable;
    private FXMLLoader fxml;
    private Parent root;
    private ImportController controller;

    public PreviewHandler() { this.isAvailable = true; }

    // region Implementing interface
    @Override
    public String getTitle() { return i.get(this.modalTitle); }
    @Override
    public Window getOwner() { return this.owner; }
    @Override
    public Modality getModality() { return this.modality; }
    @Override
    public String getFxmlFile() { return this.fxmlFile; }
    @Override
    public FXMLLoader getFxmlLoader() { return this.fxml; }
    // endregion

    public Boolean showPreview(String title, String label, ArrayList<ScannedItem> list, Window owner) {
        if (!this.isAvailable) {
            logger.warning("Unable to display two previews at the same time");
            return null;
        }
        try {
            this.owner = owner;
            this.isAvailable = false;
            if (title != null) { this.modalTitle = title; }
            this.fxml = loadFxml();
            this.root = getRoot();
            this.controller = (ImportController) getController();
            if (label != null) { this.controller.bindLabel(label); }
            Stage stage = setStage(this.root);
            stage.showAndWait();
            return this.controller.isAccepted();
        } catch (IOException e) {
            logger.error("Unable to read FXML file:", e);
            return null;
        }
    }
}
