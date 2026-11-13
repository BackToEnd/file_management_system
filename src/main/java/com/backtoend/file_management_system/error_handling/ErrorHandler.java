package com.backtoend.file_management_system.error_handling;

import com.backtoend.file_management_system.controller.ErrorController;
import com.backtoend.file_management_system.controller.IShowModal;
import com.backtoend.file_management_system.controller.MainController;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.util.I18n;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Objects;

public class ErrorHandler implements IShowModal {
    private final static AppLogger logger = AppLogger.getLogger(ErrorHandler.class);
    private final String modalTitle = "modal.error.title";
    private final Modality modality = Modality.WINDOW_MODAL;
    private final String fxmlFile = "error-view.fxml";
    private final I18n i = I18n.getInstance();
    private Window owner;
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
    // endregion

    // Displaying an error
    public void showError(String header, String body, Window owner, ArrayList<String> list) {
        logger.warning("Critical error received, attempting to load error view FXML file");
        if (!this.isAvailable) {
            logger.warning("Unable to display two errors at the same time");
            return;
        }
        try {
            this.owner = owner;
            this.isAvailable = false;
            FXMLLoader fxml = loadFxml();
            logger.debug("FXML file loaded");
            Parent root = getRoot(fxml);
            ErrorController controller = fxml.getController();
            if (controller == null) {
                logger.error("Unable to fetch controller for some reason");
                return;
            }
            if (header != null) {
                controller.setHeaderKey(header);
            }
            if (body != null) {
                controller.setBodyString(body);
            }
            if (list != null) {
                controller.setMsgList(list);
            }
            Stage stage = setStage(fxml, root);
            stage.showAndWait();
            logger.warning("Error displayed and acknowledged. Terminating application.");
        } catch (IOException e) {
            logger.error("Unable to display error modal. Terminating application.", e);
        } finally {
            MainController.terminate();
        }
    }

    public void showError(String header, String body, Window owner) {
        showError(header, body, owner, null);
    }

    // Displaying a problematic operation result
    public void displayOperationResult(String header, String body, Window owner, OperationResult<?> result) {
        if (!this.isAvailable) {
            logger.warning("Unable to display two errors at the same time");
            return;
        }

        try {
            this.owner = owner;
            this.isAvailable = false;
            FXMLLoader fxml = loadFxml();
            logger.debug("FXML file loaded");
            Parent root = getRoot(fxml);
            ErrorController controller = fxml.getController();
            if (controller == null) {
                logger.error("Unable to fetch controller for some reason");
                return;
            }
            controller.setHeaderKey(Objects.requireNonNullElse(
                    header, "modal.error.header.failed"
            ));
            controller.setBodyString(Objects.requireNonNullElse(
                    body, MessageFormat.format(
                            i.get("modal.error.body.failed"),
                            new Object[] { result.status().toString() }
                    )
            ));
            if (result.msgs() != null) {
                controller.setMsgList(
                        result.getPrintableMsgs()
                );
            }

            Stage stage = setStage(fxml, root);
            stage.showAndWait();
            logger.warning("Error displayed and acknowledged. Terminating application.");
        } catch (IOException e) {
            logger.error("Unable to display error modal. Terminating application.", e);
        } finally {
            MainController.terminate();
        }
    }
    public void displayOperationResult(Window owner, OperationResult<?> result) {
        displayOperationResult(null, null, owner, result);
    }
}
