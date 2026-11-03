package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.ItemRow;
import com.backtoend.file_management_system.service.I18n;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.ArrayList;

///
/// Controller class used to display an import preview of a selected directory
/// @author Rogue049
public class ImportController {
    /// TableView containing all scanned items
    @FXML
    private TableView<ItemRow> tableView;
    /// Label displayed above the TableView
    @FXML
    private Label tableLabel;
    /// First TableColumn; contains the file name
    @FXML
    private TableColumn<ItemRow, String> nameCol;
    /// Second TableColumn; contains the file type
    @FXML
    private TableColumn<ItemRow, String> typeCol;
    /// Button that will initiate the import process and DB insert
    @FXML
    private Button buttonImport;
    /// Button that will cancel the operation and discard all scanned items
    @FXML
    private Button buttonCancel;
    /// Stores whether the user initiated or canceled the import process
    private boolean accepted = false;
    private static final AppLogger logger = AppLogger.getLogger(ImportController.class);

    /// Default initialization of this controller at first creation - creates text bindings for label,
    /// column headers, and buttons
    @FXML
    private void initialize() {
        I18n i = I18n.getInstance();
        tableLabel.textProperty().bind(i.text("modal.import.overview.table.label"));
        nameCol.textProperty().bind(i.text("modal.import.overview.table.columns.name"));
        typeCol.textProperty().bind(i.text("modal.import.overview.table.columns.type"));
        buttonImport.textProperty().bind(i.text("modal.import.overview.buttons.import"));
        buttonCancel.textProperty().bind(i.text("modal.import.overview.buttons.cancel"));
    }

    // Getters, Setters
    /// Returns whether the import was accepted or rejected by the user
    public boolean isAccepted() {
        return this.accepted;
    }

    // === Utility Methods === \\

    /// Sets the content of the TableView
    /// @param list A list of items to be displayed
    public void setItems(ArrayList<ItemRow> list) {
        ObservableList<ItemRow> data = FXCollections.observableArrayList(list);

        // Using lamba-based cell value factories because record classes do not use standard getters like .getName() and .getFileType()
        // but uses .name() and .fileType() instead
        nameCol.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(
                        cellData.getValue().name()
                )
        );
        typeCol.setCellValueFactory(
                cellData -> new ReadOnlyStringWrapper(
                        cellData.getValue().fileType()
                )
        );
        tableView.setItems(data);
    }

    /// Closes the overview window
    private void close() {
        Stage stage = (Stage) tableView.getScene().getWindow();
        stage.close();
    }

    /// Called by the import button; sets the value of `accepted` to `true` and closes the modal
    @FXML
    protected void handleImport() {
        logger.info("Selection accepted, proceeding to DB entries");
        this.accepted = true;
        this.close();
    }
    /// Called by the cancel button; closes the modal without changing the value of `accepted`
    @FXML
    protected void cancelImport() {
        logger.info("Selection canceled");
        this.close();
    }
}
