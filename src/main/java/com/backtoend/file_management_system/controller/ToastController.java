package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.enums.OperationStatus;
import com.backtoend.file_management_system.enums.ToastType;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.service.I18n;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.HashMap;

// TODO: Currently unable to display multiple toasts at once; can only display one single toast.
//  Toast is only visible for 5s no matter the severity.
//  Fix that to allow tracking multiple toasts, and adjust toast visibility time based on severity.

/// Controller class managing and displaying toasts
/// @author Rogue049
public class ToastController {
    private final static I18n i = I18n.getInstance();
    private HashMap<ToastType, String> colorCodes = new HashMap<>();
    private VBox layer;
    private Label toast;

    // Constructor
    public ToastController() {
        this.toast = new Label();
        this.toast.setPrefWidth(150);
        this.toast.setAlignment(Pos.CENTER);
        this.toast.setStyle(
                "-fx-padding: 8px 12px;"
                + "-fx-background-radius: 6px;"
        );
        this.toast.setTextFill(Color.WHITE);
        // Setting color codes
        this.colorCodes.put(ToastType.SUCCESS, "#085f63;");
        this.colorCodes.put(ToastType.INFO, "#49beb7;");
        this.colorCodes.put(ToastType.WARNING, "#facf5a;");
        this.colorCodes.put(ToastType.ERROR, "#ff5959;");
    }

    // Getters, Setters

    /// Sets the VBox layer on which toasts are supposed to be displayed
    /// @param layer Display layer
    public void setToastLayer(VBox layer) {
        this.layer = layer;
        layer.getChildren().add(toast);
    }

    /// Sets a timer after which the toast will disappear
    /// @param s How many seconds the toast should remain visible
    private void setTimeout(int s) {
        Timeline timeout = new Timeline(
                new KeyFrame(
                        Duration.seconds(s),
                        event -> this.toast.setVisible(false)
                )
        );
        timeout.play();
    }

    // region Displaying various notifications

    /// Displays a toast with the given type and text
    /// @param type Toast type
    /// @param text Translation key of the toast text
    public void showToast(ToastType type, String text) {
        this.toast.setText(i.get(text));
        this.toast.setStyle("-fx-background-color: " + this.colorCodes.get(type));
        this.toast.setVisible(true);
        this.setTimeout(5);
    }

    /// Displays a generic toast based on an operation status and provided text
    /// @param status Operation status
    /// @param text Translation key of the toast text. Based on type, the corresponding suffix is appended;
    ///             `.failed`, `.success`, `.canceled`, or `.warning`. Do not add the last '.' to the passed key.
    public void showGeneric(OperationStatus status, String text) {
        switch (status) {
            case FAILED:
                this.showToast(ToastType.ERROR, text + ".failed");
                return;
            case SUCCESS:
                this.showToast(ToastType.SUCCESS, text + ".success");
                return;
            case CANCELLED:
                this.showToast(ToastType.INFO, text + ".canceled");
                return;
            case WARNING:
                this.showToast(ToastType.WARNING, text + ".warning");
                return;
            default:
                this.showToast(ToastType.WARNING, "toast.generic.status.invalid");
        }
    }
    // endregion
}
