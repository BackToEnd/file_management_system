package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.ItemRow;
import com.backtoend.file_management_system.service.I18n;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.ArrayList;

/**
 * Displaying an overview of scanned items in an extra window,
 * allowing the user to accept and begin import, or cancel and discard scanned items
 */
public class ImportController {
    @FXML
    private TableView<ItemRow> tableView;
    @FXML
    private Label tableLabel;
    @FXML
    private TableColumn<ItemRow, String> nameCol;
    @FXML
    private TableColumn<ItemRow, String> typeCol;
    @FXML
    private Button buttonImport;
    @FXML
    private Button buttonCancel;
    private boolean accepted = false;
    private static final AppLogger logger = AppLogger.getLogger(ImportController.class);

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
    public boolean isAccepted() {
        return this.accepted;
    }

    // === Utility Methods === \\

    /**
     * Specifies a list of items to display on the overview window<br>
     * @param list A list of items to be displayed
     */
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

    /**
     * Closes the overview window
     */
    private void close() {
        Stage stage = (Stage) tableView.getScene().getWindow();
        stage.close();
    }

    @FXML
    protected void handleImport() {
        logger.info("Selection accepted, proceeding to DB entries");
        this.accepted = true;
        this.close();
    }
    @FXML
    protected void cancelImport() {
        logger.info("Selection canceled");
        this.close();
    }
}
