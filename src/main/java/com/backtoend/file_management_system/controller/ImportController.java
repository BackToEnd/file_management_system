package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.IItemInformation;
import com.backtoend.file_management_system.model.ScanLocation;
import com.backtoend.file_management_system.model.ScannedItem;
import com.backtoend.file_management_system.service.ScanImportService;
import com.backtoend.file_management_system.util.I18n;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;


public class ImportController implements IModalController {
    private static final AppLogger logger = AppLogger.getLogger(ImportController.class);
    private final I18n i = I18n.getInstance();
    private final ScanLocation DEFAULT_SETTINGS = new ScanLocation(
            null, 1, IItemInformation.UpdateMode.MANUAL,
            IItemInformation.ScanSource.USER_INPUT, 1, IItemInformation.ItemStatus.ACTIVE
    );
    @FXML
    private Label tableLabel;
    @FXML
    private Button buttonImport;
    @FXML
    private Button buttonCancel;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab previewTab;
    @FXML
    private Tab settingsTab;

    private boolean accepted = false;
    private ScanLocation settings;

    @FXML
    private void initialize() {
        tableLabel.textProperty().bind(i.bind("modal.import.preview.label"));
        buttonImport.textProperty().bind(i.bind("modal.import.preview.button.import"));
        buttonCancel.textProperty().bind(i.bind("modal.import.preview.button.cancel"));
        previewTab.textProperty().bind(i.bind("modal.import.preview.tabs.preview"));
        settingsTab.textProperty().bind(i.bind("modal.import.preview.tabs.settings"));
    }

    public boolean isAccepted() {
        return this.accepted;
    }

    public void setList(ArrayList<ScannedItem> list) {
        TableView<ScannedItem> table = new TableView<>();
        TableColumn<ScannedItem, String> nameCol = new TableColumn<>();
        nameCol.textProperty().bind(i.bind("modal.import.preview.table.header.name"));
        nameCol.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().name()));
        TableColumn<ScannedItem, String> typeCol = new TableColumn<>();
        typeCol.textProperty().bind(i.bind("modal.import.preview.table.header.type"));
        typeCol.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().fileType()));
        table.getColumns().addAll(nameCol, typeCol);
        table.setItems(FXCollections.observableArrayList(list));
        previewTab.setContent(table);
        Label testLabel = new Label();
        testLabel.setText("This is a placeholder! Something more useful to come soon :)");
        settingsTab.setContent(testLabel);
    }

    private void close() {
        Stage stage = (Stage) tabPane.getScene().getWindow();
        stage.close();
    }

    @FXML
    protected void handleImport() {
        this.accepted = true;
        close();
    }

    @FXML
    protected void cancelImport() {
        close();
    }

    public void bindLabel(String key) {
        this.tableLabel.textProperty().unbind();
        this.tableLabel.textProperty().bind(i.bind(key));
    }

    public ScanLocation getSettings() {
        logger.warning("Scan location information requested, but scan location settings are not yet implemented!");
        return DEFAULT_SETTINGS;
    }
}