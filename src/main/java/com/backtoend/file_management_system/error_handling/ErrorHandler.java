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

// TODO: Fix duplicate code
public class ErrorHandler implements IShowModal {
    private final static AppLogger logger = AppLogger.getLogger(ErrorHandler.class);
    private final String modalTitle = "modal.error.title";
    private final Modality modality = Modality.WINDOW_MODAL;
    private final String fxmlFile = "error-view.fxml";
    private final I18n i = I18n.getInstance();
    private Window owner;
    private boolean isAvailable;
    private FXMLLoader fxml;
    private Parent root;
    private ErrorController controller;

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
    public FXMLLoader getFxmlLoader() { return this.fxml; }
    // endregion

    // Displaying an error
    public void showError(String header, String body, Window owner, ArrayList<String> list, boolean isFatal) {
        logger.warning("Critical error received, attempting to load error view FXML file");
        if (!this.isAvailable) {
            logger.warning("Unable to display two errors at the same time");
            return;
        }
        try {
            configureAll(owner, header, body, list);
            logger.warning("Error displayed and acknowledged. Terminating application.");
        } catch (IOException e) {
            logger.error("Unable to display error modal. Terminating application.", e);
        } finally {
            if (isFatal) {
                MainController.terminate();
            }
        }
    }
    public void showError(String header, String body, Window owner, ArrayList<String> list) {
        showError(header, body, owner, list, true);
    }
    public void showError(String header, String body, Window owner, boolean isFatal) {
        showError(header,body, owner, null, isFatal);
    }
    public void showError(String header, String body, Window owner) {
        showError(header, body, owner, null, true);
    }

    // Displaying a problematic operation result
    public void showOperationResult(String header, String body, Window owner, OperationResult<?> result, boolean isFatal) {
        if (!this.isAvailable) {
            logger.warning("Unable to display two errors at the same time");
            return;
        }
        try {
            configureAll(
                    owner,
                    Objects.requireNonNullElse(header, i.get("modal.error.header.failed")),
                    Objects.requireNonNullElse(body, MessageFormat.format(
                            i.get("modal.error.body.failed"), new Object[] { result.status().toString() }
                    )),
                    result.getPrintableMsgs()
            );
            logger.warning("Error displayed and acknowledged. Terminating application.");
        } catch (IOException e) {
            logger.error("Unable to display error modal. Terminating application.", e);
        } finally {
            if (isFatal) {
                MainController.terminate();
            }
        }
    }
    public void showOperationResult(String header, String body, Window owner, OperationResult<?> result) {
        showOperationResult(header, body, owner, result, true);
    }
    public void showOperationResult(Window owner, OperationResult<?> result, boolean isFatal) {
        showOperationResult(null, null, owner, result, isFatal);
    }
    public void showOperationResult(Window owner, OperationResult<?> result) {
        showOperationResult(null, null, owner, result, true);
    }

    // === Private Helper Methods === \\
    private void configureAll(Window owner, String header, String body, ArrayList<String> msgs)
            throws IOException {
        this.owner = owner;
        this.isAvailable = false;
        this.fxml = loadFxml();
        this.root = getRoot();
        this.controller = (ErrorController) getController();
        if (header != null) { this.controller.setHeaderString(header); }
        if (body != null) { this.controller.setBodyString(body); }
        if (msgs != null) { this.controller.setMsgList(msgs); }
        Stage stage = setStage(this.root);
        stage.showAndWait();
    }
}
