package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.MainApplication;
import com.backtoend.file_management_system.database.Database;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.util.I18n;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

public class OverviewController {
    private static final AppLogger logger = AppLogger.getLogger(OverviewController.class);
    @FXML
    private TreeTableView<String> treeView;
    @FXML
    private Label placeholder;
    @FXML
    private TreeTableColumn<String, String> nameCol;
    @FXML
    private TreeTableColumn<String, String> typeCol;
    @FXML
    private TreeTableColumn<String, String> pathCol;
    private final Database db = MainApplication.getDbInstance();
    private final I18n i = I18n.getInstance();

    @FXML
    private void initialize() {
        placeholder.textProperty().bind(i.bind("overview.table.placeholder"));
        nameCol.textProperty().bind(i.bind("overview.table.header.name"));
        typeCol.textProperty().bind(i.bind("overview.table.header.type"));
        pathCol.textProperty().bind(i.bind("overview.table.header.path"));
    }
}
