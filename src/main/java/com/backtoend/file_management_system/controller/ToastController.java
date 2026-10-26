package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.enums.OperationStatus;
import com.backtoend.file_management_system.enums.ToastType;
import com.backtoend.file_management_system.service.I18n;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.HashMap;

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
    public void setToastLayer(VBox layer) {
        this.layer = layer;
        layer.getChildren().add(toast);
    }

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
    public void showToast(ToastType type, String text) {
        this.toast.setText(i.get(text));
        this.toast.setStyle("-fx-background-color: " + this.colorCodes.get(type));
        this.toast.setVisible(true);
        this.setTimeout(5);
    }

    public void showGeneric(OperationStatus status) {
        switch (status) {
            case FAILED:
                this.showToast(ToastType.WARNING, "toast.generic.status.failed");
                return;
            case SUCCESS:
                this.showToast(ToastType.SUCCESS, "toast.generic.status.success");
                return;
            case CANCELLED:
                this.showToast(ToastType.INFO, "toast.generic.status.canceled");
                return;
            default:
                this.showToast(ToastType.WARNING, "toast.generic.status.invalid");
        }
    }

    public void showGeneric(OperationStatus status, String text) {
        switch (status) {
            case FAILED:
                this.showToast(ToastType.WARNING, text + ".failed");
                return;
            case SUCCESS:
                this.showToast(ToastType.SUCCESS, text + ".success");
                return;
            case CANCELLED:
                this.showToast(ToastType.INFO, text + ".canceled");
                return;
            default:
                this.showToast(ToastType.WARNING, "toast.generic.status.invalid");
        }
    }
    // endregion
}
