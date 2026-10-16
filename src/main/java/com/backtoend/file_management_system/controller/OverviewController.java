package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.logging.AppLogger;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.backtoend.file_management_system.model.Item;
import com.backtoend.file_management_system.model.ItemHandler;

public class OverviewController {
    private static final AppLogger logger = AppLogger.getLogger(OverviewController.class);

    public static VBox showOverview(ItemHandler items) {
        ObservableList<Item> data = FXCollections.observableArrayList(
                items.getItemList()
        );
        TableView<Item> view = new TableView<>();
        TableColumn<Item, String> pathCol = new TableColumn<>("Path");
        TableColumn<Item, String> nameCol = new TableColumn<>("Display Name");
        TableColumn<Item, Boolean> dirCol = new TableColumn<>("Is Directory");
        pathCol.setCellValueFactory(new PropertyValueFactory<>("path"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        dirCol.setCellValueFactory(new PropertyValueFactory<>("isDir"));
        view.getColumns().addAll(pathCol, nameCol, dirCol);
        view.setItems(data);

        return new VBox(view);
    }
}
