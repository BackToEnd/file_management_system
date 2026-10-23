package com.backtoend.file_management_system.service;

import com.backtoend.file_management_system.Context;
import com.backtoend.file_management_system.controller.FileExplorerController;
import com.backtoend.file_management_system.controller.ModalController;
import com.backtoend.file_management_system.database.DatabaseCheck;
import com.backtoend.file_management_system.database.DatabaseFetch;
import com.backtoend.file_management_system.database.DatabaseInsert;
import com.backtoend.file_management_system.enums.ItemStatus;
import com.backtoend.file_management_system.enums.ParentExistence;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.ItemRow;
import com.backtoend.file_management_system.model.ParentNode;
import com.backtoend.file_management_system.util.FeedbackMsg;
import com.backtoend.file_management_system.util.OperationResult;
import com.backtoend.file_management_system.enums.OperationStatus;
import javafx.stage.Window;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Scan-Import service class that maintains the directory parsing and importing workflow
 */
public class ScanImport {
    private final Context context;
    private static final AppLogger logger = AppLogger.getLogger(ScanImport.class);

    public ScanImport(Context context) {
        this.context = context;
    }

    /**
     * Begins the import process; Opens a directory chooser, processes user selection,
     * displays an overview menu, allows the user to choose whether to import or cancel,
     * and parses content into the DB
     * @param window Where any external screens this method creates should be displayed
     * @return Operation status and status messages
     */
    public OperationResult<Void> initiateScanImport(Window window) {
        logger.info("Initiating directory scan and import process");
        ArrayList<FeedbackMsg> feedback = new ArrayList<>();

        // Open directory chooser and receive operation result w/ user selection
        OperationResult<String> directory = this.openDirChooser(window);
        logger.result(this.getClass().getName(), directory);
        feedback.addAll(directory.msg());
        if (!directory.isSuccessful() || directory.value() == null) {
            // A canceled directory selection does not mean that the selection failed
            return new OperationResult<>(directory.status(), null, feedback);
        }
        feedback.addAll(directory.msg());

        // Checking if the chosen path already exists as a node
        String path = directory.value();
        int nodeId = -1;
        try {
            ParentExistence result = DatabaseCheck.doesParentExist(path, context.db());
            if (result == ParentExistence.IS_SCAN_LOCATION) {
                feedback.add(
                        new FeedbackMsg(
                                this.getClass().getName(),
                                "import.init.path.exists.scan",
                                new Object[] {}
                        )
                );
                return new OperationResult<>(OperationStatus.FAILED, null, feedback);
            }
            if (result == ParentExistence.IS_NODE) {
                nodeId = DatabaseFetch.getNodeId(path, context.db().connect());
            }
        } catch (SQLException e) {
            logger.error("Critical error encountered: ", e);
            feedback.add(
                    new FeedbackMsg(
                            this.getClass().getName(),
                            "import.init.path.exists.sqlexception",
                            new Object[] {}
                    )
            );
            return new OperationResult<>(OperationStatus.FAILED, null, feedback);
        }

        // Parsing directory
        ArrayList<ItemRow> items;
        try {
            items = DirectoryHandler.parseDir(path);
        } catch (IOException e) {
            logger.error("Critical error encountered: ", e);
            feedback.add(
                    new FeedbackMsg(
                            this.getClass().getName(),
                        "import.init.parse.failure", new Object[] {}
                    )
            );
            return new OperationResult<>(OperationStatus.FAILED, null, feedback);
        }

        // Display import overview
        OperationResult<Void> beginImport = showOverview(window, items);
        logger.result(this.getClass().getName(), beginImport);
        feedback.addAll(beginImport.msg());
        if (!beginImport.isSuccessful()) {
            return new OperationResult<>(beginImport.status(), null, feedback);
        }
        feedback.addAll(beginImport.msg());

        // Get ItemRow for parent path
        ItemRow parentRow;
        try {
            parentRow = DirectoryHandler.parseParent(path);
        } catch (IOException e) {
            logger.error("Critical error encountered: ", e);
            feedback.add(new FeedbackMsg(
                    this.getClass().getName(),
                    "import.init.parse.parent.failed", new Object[] {}
            ));
            return new OperationResult<>(OperationStatus.FAILED, null, feedback);
        }
        // TODO: Replace this constructor with a method to allow user input of all passed parameters
        ParentNode parentNode = new ParentNode(
                parentRow, 0, "MANUAL",
                "USER_SELECTION", 0, ItemStatus.ACTIVE
        );

        OperationResult<Void> insertIntoDb = DatabaseInsert.insertNewSet(
                parentNode, items, this.context.db()
        );
        logger.result(this.getClass().getName(), insertIntoDb);
        if (!insertIntoDb.isSuccessful()) {
            return new OperationResult<>(insertIntoDb.status(), null, feedback);
        }

        feedback.add(new FeedbackMsg(
                this.getClass().getName(),
            "modal.import.overview.success", new Object[] {}
        ));
        return new OperationResult<>(OperationStatus.SUCCESS, null, feedback);
    }

    // === Utility Methods === \\

    /**
     * Displays a directory chooser and allows the user to pick a directory
     * @param window Where the directory chooser should be opened
     * @return Operation status, user-selected directory (can be null), and status messages
     */
    private OperationResult<String> openDirChooser(Window window) {
        FileExplorerController fExCon = new FileExplorerController(this.context.dirHandler());
        return fExCon.openDirectoryChooser(window);
    }

    /**
     * Displays an import overview screen and allows the user to choose whether to proceed with the import or cancel the process
     * @param window Where the overview should be displayed
     * @param items List of scanned items to be displayed
     * @return Operation status and status messages
     */
    private OperationResult<Void> showOverview(Window window, ArrayList<ItemRow> items) {
        try {
            boolean startImport = ModalController.showImportPreview(window, items);
            ArrayList<FeedbackMsg> feedback = new ArrayList<>();
            if (startImport) {
                feedback.add(new FeedbackMsg(
                        this.getClass().getName(),
                        "modal.import.overview.accepted", new Object[] {}
                ));
                return new OperationResult<>(OperationStatus.SUCCESS, null, feedback);
            }
            feedback.add(new FeedbackMsg(
                    this.getClass().getName(),
                    "modal.import.overview.denied", new Object[] {}
            ));
            return new OperationResult<>(OperationStatus.CANCELLED, null, feedback);
        } catch (IOException e) {
            logger.error("Critical error encountered: ", e);
            ArrayList<FeedbackMsg> feedback = new ArrayList<>();
            feedback.add(new FeedbackMsg(
                    this.getClass().getName(),
                    "modal.import.overview.failed", new Object[] {}
            ));
            return new OperationResult<>(OperationStatus.FAILED, null, feedback);
        }
    }
}
