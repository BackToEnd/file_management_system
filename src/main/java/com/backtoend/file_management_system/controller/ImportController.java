package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.IItemInformation;
import com.backtoend.file_management_system.model.ScanLocation;
import com.backtoend.file_management_system.model.ScannedItem;
import com.backtoend.file_management_system.util.I18n;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.ArrayList;

/// Controller class implementing {@link IModalController}.\
/// The modal controller associated with {@link com.backtoend.file_management_system.util.PreviewHandler}.
/// @author Rogue049
public class ImportController implements IModalController {
    /// Logger instance
    private static final AppLogger logger = AppLogger.getLogger(ImportController.class);
    /// {@link I18n} instance
    private final I18n i = I18n.getInstance();
    /// Default settings for a newly obtained {@link ScanLocation}.
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

    /// Stores whether the user wants to proceed with the import (`true`)
    /// or abort it (`false`).
    private boolean accepted = false;
    /// Stores information on the {@link ScanLocation} created for a scanned
    /// directory.
    private ScanLocation settings;

    @FXML
    private void initialize() {
        tableLabel.setText(i.get("modal.import.preview.label"));
        buttonImport.setText(i.get("modal.import.preview.button.import"));
        buttonCancel.setText(i.get("modal.import.preview.button.cancel"));
        previewTab.setText(i.get("modal.import.preview.tabs.preview"));
        settingsTab.setText(i.get("modal.import.preview.tabs.settings"));
    }

    /// Should be called after the import preview is shown and user input was
    /// received
    /// @return `true` if the user wants to proceed with the import, `false`
    /// otherwise.
    public boolean isAccepted() {
        return this.accepted;
    }

    /// Should be called after the import preview is shown and the user input was received.
    /// @return the selected {@link ScanLocation} settings
    public ScanLocation getSettings() {
        logger.warning("Scan location information requested, but scan location settings are not yet implemented!");
        return DEFAULT_SETTINGS;
    }

    /// Configures the list of scanned items displayed on the modal.
    /// If the list is `null`, a placeholder label will be
    /// shown instead.
    /// @param list list of scanned items
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
        if (list == null || list.isEmpty()) {
            table.setPlaceholder(
                    new Label(i.get("modal.import.preview.table.placeholder"))
            );
            return;
        }
        table.setItems(FXCollections.observableArrayList(list));
        previewTab.setContent(table);
    }

    /// Configures the {@link ScanLocation} settings to adopt. Call this method even if no scan location
    /// exists, passing `null` as an argument, otherwise the settings tab will be empty.
    /// @param settings scan location settings
    public void setSettings(ScanLocation settings) {
        if (settings == null) {
            // TODO: user default settings
        } else {
            // TODO: use provided settings
        }
        Label testLabel = new Label();
        testLabel.setText("This is a placeholder! Something more useful to come soon :)");
        settingsTab.setContent(testLabel);
    }

    /// Shortcut to configure both displayed items and {@link ScanLocation} settings through one method.
    /// Both parameters can be `null`.
    /// @param list list of items to display
    /// @param settings scan location settings
    public void setAll(ArrayList<ScannedItem> list, ScanLocation settings) {
        setList(list);
        setSettings(settings);
    }

    /// Changes the text displayed in the preview label.\
    /// Note that `text` is treated as a String text, not a translation key.
    /// @param text new label text
    public void setLabel(String text) {
        this.tableLabel.setText(text);
    }

    // === Private Helper Methods === \\
    /// Closes the stage
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

}