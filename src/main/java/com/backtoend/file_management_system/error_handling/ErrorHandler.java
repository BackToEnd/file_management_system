package com.backtoend.file_management_system.error_handling;

import com.backtoend.file_management_system.MainApplication;
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
import java.util.ArrayList;

/// Modal handler class implementing {@link IShowModal}.\
/// Used to configure and display error messages. Only one
/// `ErrorController` should exist at runtime; this singleton
/// instance can be obtained using {@link MainApplication#getErrorHandler()}.\
/// For each error to be displayed, a new {@link ErrorController} is created,
/// its content (window title, error header, and error body) configured, and
/// then displayed. If an error marked as fatal is given to this handler,
/// after the user acknowledges, the application is terminated.\
/// When passing fatal errors, make sure to have either error header
/// or error body mention to the user that the application will terminate.
/// @author Rogue049
public class ErrorHandler implements IShowModal {
    /// Logger instance
    private final static AppLogger logger = AppLogger.getLogger(ErrorHandler.class);
    /// Window title of the error modal
    private final String modalTitle = "modal.error.title";
    /// Modality of the error modal; blocks interactions
    /// with the main application window until the modal is closed
    private final Modality modality = Modality.WINDOW_MODAL;
    /// Name of the FXML file associated with this handler
    private final String fxmlFile = "error-view.fxml";
    /// {@link I18n} instance
    private final I18n i = I18n.getInstance();
    /// Window on which the error modal should be displayed
    private Window owner;
    /// Whether this handler is currently available to display an error
    private boolean isAvailable;
    /// {@link FXMLLoader} for this handler
    private FXMLLoader fxml;
    /// Root {@link Parent}, primarily relevant for interface implementation
    private Parent root;
    /// {@link ErrorController} associated with this handler
    private ErrorController controller;

    /// Constructor. Creates a new `ErrorHandler` instance with
    /// {@link #isAvailable} set to `true`
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

    /// Default way to display an error modal to the user, blocking the main application
    /// window until the user acknowledges. When using this method, keep in mind:\
    /// - Always call on the FX thread! This method interacts with UI elements and should
    /// not be called from a background thread.
    /// - The parameters `header`, `body`, and `list` can be `null`. If `header`
    /// and/or `body` are `null`, their default values will be used. If `list` is null
    /// or omitted, no list container will be displayed on the modal.
    /// - The parameters `header` and `body` are treated as String values, not
    /// translation keys. You should translate keys before passing them.
    /// - The parameters `list` and `isFatal` can be omitted. If `isFatal` is omitted,
    /// its value will be assumed to be `true`. If you are passing a non-fatal error,
    /// please do not omit `isFatal`.
    /// - If a fatal error is passed, after the user acknowledges, the application will
    /// be terminated.
    /// @param header the error header. This is not the modal title, but a label
    ///               on the modal with a slightly bigger font.
    /// @param body the error body. While this one can be `null`, a body text should
    ///             always be passed, providing a description of the error.
    /// @param owner the window on which the modal should be displayed.
    /// @param list a list of error messages.
    /// @param isFatal whether the error is fatal and the application should terminate.
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
            this.isAvailable = true;
        }
    }
    /// Default way to display an error modal to the user, blocking the main application
    /// window until the user acknowledges. When using this method, keep in mind:\
    /// - Always call on the FX thread! This method interacts with UI elements and should
    /// not be called from a background thread.
    /// - The parameters `header`, `body`, and `list` can be `null`. If `header`
    /// and/or `body` are `null`, their default values will be used. If `list` is null
    /// or omitted, no list container will be displayed on the modal.
    /// - The parameters `header` and `body` are treated as String values, not
    /// translation keys. You should translate keys before passing them.
    /// - The parameters `list` and `isFatal` can be omitted. If `isFatal` is omitted,
    /// its value will be assumed to be `true`. If you are passing a non-fatal error,
    /// please do not omit `isFatal`.
    /// - If a fatal error is passed, after the user acknowledges, the application will
    /// be terminated.
    /// @param header the error header. This is not the modal title, but a label
    ///               on the modal with a slightly bigger font.
    /// @param body the error body. While this one can be `null`, a body text should
    ///             always be passed, providing a description of the error.
    /// @param owner the window on which the modal should be displayed.
    /// @param list a list of error messages.
    public void showError(String header, String body, Window owner, ArrayList<String> list) {
        showError(header, body, owner, list, true);
    }
    /// Default way to display an error modal to the user, blocking the main application
    /// window until the user acknowledges. When using this method, keep in mind:\
    /// - Always call on the FX thread! This method interacts with UI elements and should
    /// not be called from a background thread.
    /// - The parameters `header`, `body`, and `list` can be `null`. If `header`
    /// and/or `body` are `null`, their default values will be used. If `list` is null
    /// or omitted, no list container will be displayed on the modal.
    /// - The parameters `header` and `body` are treated as String values, not
    /// translation keys. You should translate keys before passing them.
    /// - The parameters `list` and `isFatal` can be omitted. If `isFatal` is omitted,
    /// its value will be assumed to be `true`. If you are passing a non-fatal error,
    /// please do not omit `isFatal`.
    /// - If a fatal error is passed, after the user acknowledges, the application will
    /// be terminated.
    /// @param header the error header. This is not the modal title, but a label
    ///               on the modal with a slightly bigger font.
    /// @param body the error body. While this one can be `null`, a body text should
    ///             always be passed, providing a description of the error.
    /// @param owner the window on which the modal should be displayed.
    /// @param isFatal whether the error is fatal and the application should terminate.
    public void showError(String header, String body, Window owner, boolean isFatal) {
        showError(header,body, owner, null, isFatal);
    }
    /// Default way to display an error modal to the user, blocking the main application
    /// window until the user acknowledges. When using this method, keep in mind:\
    /// - Always call on the FX thread! This method interacts with UI elements and should
    /// not be called from a background thread.
    /// - The parameters `header`, `body`, and `list` can be `null`. If `header`
    /// and/or `body` are `null`, their default values will be used. If `list` is null
    /// or omitted, no list container will be displayed on the modal.
    /// - The parameters `header` and `body` are treated as String values, not
    /// translation keys. You should translate keys before passing them.
    /// - The parameters `list` and `isFatal` can be omitted. If `isFatal` is omitted,
    /// its value will be assumed to be `true`. If you are passing a non-fatal error,
    /// please do not omit `isFatal`.
    /// - If a fatal error is passed, after the user acknowledges, the application will
    /// be terminated.
    /// @param header the error header. This is not the modal title, but a label
    ///               on the modal with a slightly bigger font.
    /// @param body the error body. While this one can be `null`, a body text should
    ///             always be passed, providing a description of the error.
    /// @param owner the window on which the modal should be displayed.
    public void showError(String header, String body, Window owner) {
        showError(header, body, owner, null, true);
    }

    // Displaying a problematic operation result

    /// Directly displays an operation result.
    /// - Always call on the FX thread! This method interacts with UI elements and should
    /// not be called from a background thread.
    /// - The parameters `header`, `body`, and `list` can be `null`. If `header`
    /// and/or `body` are `null`, their default values will be used. If `list` is null
    /// or omitted, no list container will be displayed on the modal.
    /// - The parameters `header` and `body` are treated as String values, not
    /// translation keys. You should translate keys before passing them.
    /// - The parameter`isFatal` can be omitted. If `isFatal` is omitted,
    /// its assumed value will be based on the operation result's status;
    /// if the status is `CANCELED`, `isFatal` will be assumed to be `false`. If
    /// the status is `FAILED`, `isFatal` will be assumed to be `true`. If you
    /// want to override this, please do not omit `isFatal`.
    /// - If a fatal error is passed, after the user acknowledges, the application will
    /// be terminated.
    /// @param header the error header. This is not the modal title, but a label
    ///               on the modal with a slightly bigger font.
    /// @param body the error body. While this one can be `null`, a body text should
    ///             always be passed, providing a description of the error.
    /// @param owner the window on which the modal should be displayed.
    /// @param result the operation result that should be displayed. If it contains
    ///               any feedback messages, those will be displayed as a
    ///               list of messages.
    /// @param isFatal whether the error is fatal and the application should terminate.
    public void showOperationResult(String header, String body, Window owner, OperationResult<?> result, boolean isFatal) {
        if (!this.isAvailable) {
            logger.warning("Unable to display two errors at the same time");
            return;
        }
        showError(
                header, body, owner, result.getPrintableMsgs(), isFatal
        );
    }
    /// Directly displays an operation result.
    /// - Always call on the FX thread! This method interacts with UI elements and should
    /// not be called from a background thread.
    /// - The parameters `header`, `body`, and `list` can be `null`. If `header`
    /// and/or `body` are `null`, their default values will be used. If `list` is null
    /// or omitted, no list container will be displayed on the modal.
    /// - The parameters `header` and `body` are treated as String values, not
    /// translation keys. You should translate keys before passing them.
    /// - The parameter`isFatal` can be omitted. If `isFatal` is omitted,
    /// its assumed value will be based on the operation result's status;
    /// if the status is `CANCELED`, `isFatal` will be assumed to be `false`. If
    /// the status is `FAILED`, `isFatal` will be assumed to be `true`. If you
    /// want to override this, please do not omit `isFatal`.
    /// - If a fatal error is passed, after the user acknowledges, the application will
    /// be terminated.
    /// @param header the error header. This is not the modal title, but a label
    ///               on the modal with a slightly bigger font.
    /// @param body the error body. While this one can be `null`, a body text should
    ///             always be passed, providing a description of the error.
    /// @param owner the window on which the modal should be displayed.
    /// @param result the operation result that should be displayed. If it contains
    ///               any feedback messages, those will be displayed as a
    ///               list of messages.
    public void showOperationResult(String header, String body, Window owner, OperationResult<?> result) {
        showOperationResult(header, body, owner, result, result.isFailed());
    }
    /// Directly displays an operation result.
    /// - Always call on the FX thread! This method interacts with UI elements and should
    /// not be called from a background thread.
    /// - The parameters `header`, `body`, and `list` can be `null`. If `header`
    /// and/or `body` are `null`, their default values will be used. If `list` is null
    /// or omitted, no list container will be displayed on the modal.
    /// - The parameters `header` and `body` are treated as String values, not
    /// translation keys. You should translate keys before passing them.
    /// - The parameter`isFatal` can be omitted. If `isFatal` is omitted,
    /// its assumed value will be based on the operation result's status;
    /// if the status is `CANCELED`, `isFatal` will be assumed to be `false`. If
    /// the status is `FAILED`, `isFatal` will be assumed to be `true`. If you
    /// want to override this, please do not omit `isFatal`.
    /// - If a fatal error is passed, after the user acknowledges, the application will
    /// be terminated.
    /// @param owner the window on which the modal should be displayed.
    /// @param result the operation result that should be displayed. If it contains
    ///               any feedback messages, those will be displayed as a
    ///               list of messages.
    /// @param isFatal whether the error is fatal and the application should terminate.
    public void showOperationResult(Window owner, OperationResult<?> result, boolean isFatal) {
        showOperationResult(null, null, owner, result, isFatal);
    }
    /// Directly displays an operation result.
    /// - Always call on the FX thread! This method interacts with UI elements and should
    /// not be called from a background thread.
    /// - The parameters `header`, `body`, and `list` can be `null`. If `header`
    /// and/or `body` are `null`, their default values will be used. If `list` is null
    /// or omitted, no list container will be displayed on the modal.
    /// - The parameters `header` and `body` are treated as String values, not
    /// translation keys. You should translate keys before passing them.
    /// - The parameter`isFatal` can be omitted. If `isFatal` is omitted,
    /// its assumed value will be based on the operation result's status;
    /// if the status is `CANCELED`, `isFatal` will be assumed to be `false`. If
    /// the status is `FAILED`, `isFatal` will be assumed to be `true`. If you
    /// want to override this, please do not omit `isFatal`.
    /// - If a fatal error is passed, after the user acknowledges, the application will
    /// be terminated.
    /// @param owner the window on which the modal should be displayed.
    /// @param result the operation result that should be displayed. If it contains
    ///               any feedback messages, those will be displayed as a
    ///               list of messages.
    public void showOperationResult(Window owner, OperationResult<?> result) {
        showOperationResult(null, null, owner, result, result.isFailed());
    }

    // === Private Helper Methods === \\

    /// Private helper method to avoid duplicate code used by all error displays.\
    /// Automatically processes `owner`, `header`, `body`, and `msgs`,
    /// loads the FXML file, obtains and configures the controller,
    /// and shows the stage.
    /// @param owner where the modal should be displayed
    /// @param header error header
    /// @param body error body
    /// @param msgs String message list
    /// @throws IOException if the FXML file cannot be accessed or loaded
    private void configureAll(Window owner, String header, String body, ArrayList<String> msgs)
            throws IOException {
        this.owner = owner;
        this.isAvailable = false;
        this.fxml = loadFxml();
        this.root = getRoot();
        this.controller = (ErrorController) getController();
        if (header != null) { this.controller.setHeader(header); }
        if (body != null) { this.controller.setBody(body); }
        if (msgs != null) { this.controller.setMsgList(msgs); }
        Stage stage = setStage(this.root);
        stage.showAndWait();
    }
}
