package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.Context;
import com.backtoend.file_management_system.database.DatabaseFetch;
import com.backtoend.file_management_system.enums.OperationStatus;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.ItemRow;
import com.backtoend.file_management_system.model.ItemTreeNode;
import com.backtoend.file_management_system.service.I18n;
import com.backtoend.file_management_system.util.FeedbackMsg;
import com.backtoend.file_management_system.util.OperationResult;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Controller responsible for displaying any kind of DB output on the main app interface<br>
 * Is rendered by MainController upon app start
 */
public class OverviewController {
    private static final AppLogger logger = AppLogger.getLogger(OverviewController.class);
    @FXML
    private TreeTableView treeView;
    @FXML
    private Label placeholder;
    @FXML
    private TreeTableColumn nameCol;
    @FXML
    private TreeTableColumn typeCol;
    @FXML
    private TreeTableColumn pathCol;
    private final Context context;

    @FXML
    private void initialize() {
        I18n i = I18n.getInstance();
        placeholder.textProperty().bind(
                i.text("overview.table.rows.placeholder")
        );
        nameCol.textProperty().bind(
                i.text("overview.table.columns.name")
        );
        typeCol.textProperty().bind(
                i.text("overview.table.columns.type")
        );
        pathCol.textProperty().bind(
                i.text("overview.table.columns.path")
        );
    }

    public OverviewController(Context context) {
        this.context = context;
    }

    // === Utility Methods === \\

    private OperationResult<Void> buildTree() {
        logger.info("Attempting to generate tree table view");
        ArrayList<FeedbackMsg> feedback = new ArrayList<>();

        // Attempting to fetch child items
        logger.debug("Attempting to fetch parent nodes from DB");
        OperationResult<ArrayList<ItemRow>> fetchParentResult = this.getParentItems();
        feedback.addAll(fetchParentResult.msg());
        if (!fetchParentResult.isSuccessful()) {
            feedback.add(new FeedbackMsg(
                    this.getClass().getName(),
                    "overview.build.failed.generic",
                    new Object[] {}
            ));
            return new OperationResult<>(OperationStatus.FAILED, null, feedback);
        }
    }

    private OperationResult<ArrayList<ItemRow>> getParentItems() {
        ArrayList<FeedbackMsg> feedback = new ArrayList<>();
        ArrayList<ItemRow> parents;
        try {
            parents = this.context.db()
                    .withConnection(DatabaseFetch::getAllParentItems);
        } catch (SQLException e) {
            logger.error("Critical error encountered: ", e);
            feedback.add(new FeedbackMsg(
                    this.getClass().getName(),
                    "overview.fetch.parent.all.failed",
                    new Object[] {}
            ));
            return new OperationResult<>(OperationStatus.FAILED, null, feedback);
        }
        feedback.add(new FeedbackMsg(
                this.getClass().getName(),
                "overview.fetch.parent.all.success",
                new Object[] {}
        ));
        return new OperationResult<>(OperationStatus.SUCCESS, parents, feedback);
    }

    private OperationResult<Void> loadChilren(ItemTreeNode parent) {
        
    }
}
