package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.util.I18n;
import javafx.fxml.FXMLLoader;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;

public class ErrorHandler implements IShowModal {
    private final static AppLogger logger = AppLogger.getLogger(ErrorHandler.class);
    private final String modalTitle = "modal.error.title";
    private final Modality modality = Modality.WINDOW_MODAL;
    private final String fxmlFile = "error-view.fxml";
    private final I18n i = I18n.getInstance();
    private ErrorController controller;
    private Window owner;
    private FXMLLoader fxml;
    private boolean isAvailable;

    public ErrorHandler() {
        this.isAvailable = true;
    }

    // region Implementing interface
    @Override
    public String getTitle() {
        return i.get(this.modalTitle);
    }
    @Override
    public Window getOwner() {
        return this.owner;
    }
    @Override
    public Modality getModality() {
        return this.modality;
    }
    @Override
    public String getFxmlFile() {
        return this.fxmlFile;
    }
    @Override
    public FXMLLoader getFxmlLoader() {
        return this.fxml;
    }
    // endregion

    // Displaying an error
    public void showError(String header, String body) {
        logger.info("Attempting to load error view FXML file");
        if (!this.isAvailable) {
            logger.warning("Unable to display two errors at the same time");
            return;
        }
        try {
            this.isAvailable = false;
            this.fxml = loadFxml();
            this.controller = this.fxml.getController();
            if (header != null) {
                this.controller.bindHeader(header);
            }
            if (body != null) {
                this.controller.setBody(body);
            }
            this.owner = this.controller.getWindow();
            Stage stage = setStage();
            stage.showAndWait();
            logger.warning("Error displayed and acknowledged. Terminating application.");
        } catch (IOException e) {
            logger.error("Unable to display error modal. Terminating application.", e);
        } finally {
            MainController.terminate();
        }
    }
}
