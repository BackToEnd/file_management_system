package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.MainApplication;
import com.backtoend.file_management_system.database.Database;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.util.I18n;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

/// Controller class contained within {@link MainController}.\
/// Responsible for generating and maintaining the tree overview, as well as
/// handling all user interactions with the tree.
public class OverviewController {
    /// Logger instance
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
    /// Database singleton instance
    private final Database db = MainApplication.getDbInstance();
    /// I18n instance
    private final I18n i = I18n.getInstance();

    @FXML
    private void initialize() {
        placeholder.textProperty().bind(i.bind("overview.table.placeholder"));
        nameCol.textProperty().bind(i.bind("overview.table.header.name"));
        typeCol.textProperty().bind(i.bind("overview.table.header.type"));
        pathCol.textProperty().bind(i.bind("overview.table.header.path"));
    }
}
