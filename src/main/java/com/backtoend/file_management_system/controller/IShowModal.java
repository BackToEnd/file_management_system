package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.MainApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;

/// Interface implemented by all modal handlers. Contains
/// methods relevant to all handlers.
/// @author Rogue049
public interface IShowModal {
    /// Gets the modal window title
    /// @return modal window title
    String getTitle();

    /// Gets the {@link Window} on which the modal should be displayed
    /// @return owner window
    Window getOwner();

    /// Gets the {@link Modality} of the modal
    /// @return modality
    Modality getModality();

    /// Gets the name of the FXML file of the modal
    /// @return name of the FXML file
    String getFxmlFile();

    /// Gets the {@link FXMLLoader} of the modal controller
    /// @return FXMLLoader
    FXMLLoader getFxmlLoader();

    /// Default implementation. Attempts to load an FXML file.
    /// @return FXMLLoader of the file
    /// @throws IOException if the FXML file cannot be loaded
    default FXMLLoader loadFxml() throws IOException {
        return new FXMLLoader(
                MainApplication.class.getResource(getFxmlFile())
        );
    }

    /// Default implementation. Loads the file within the FXMLLoader.
    /// @return root node of the loaded FXML file
    /// @throws IOException if the FXML file cannot be loaded
    default Parent getRoot() throws IOException {
        return getFxmlLoader().load();
    }

    /// Default implementation. Creates a {@link Stage} based on variables provided through
    /// {@link #getTitle()}, {@link #getOwner()}, and {@link #getModality()}, and sets the stage's {@link Scene}
    /// to the provided root parent.
    /// @param root root parent of the modal
    /// @return fully configured stage
    default Stage setStage(Parent root) {
        Stage stage = new Stage();
        stage.setTitle(getTitle());
        stage.initOwner(getOwner());
        stage.initModality(getModality());
        stage.setScene(new Scene(root));
        return stage;
    }

    /// Default implementation. Uses the FXMLLoader provided through {@link #getFxmlLoader()} to return
    /// the loader's controller.
    /// @return any modal controller implementing {@link IModalController}, obtained through
    /// {@link FXMLLoader#getController()}
    default IModalController getController() {
        return getFxmlLoader().getController();
    }
}
