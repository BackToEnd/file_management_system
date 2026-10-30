package com.backtoend.file_management_system.service;

import com.backtoend.file_management_system.Context;
import com.backtoend.file_management_system.controller.FileExplorerController;
import com.backtoend.file_management_system.controller.ModalController;
import com.backtoend.file_management_system.database.Database;
import com.backtoend.file_management_system.database.DatabaseCheck;
import com.backtoend.file_management_system.database.DatabaseFetch;
import com.backtoend.file_management_system.database.DatabaseInsert;
import com.backtoend.file_management_system.enums.DbStatus;
import com.backtoend.file_management_system.enums.ItemStatus;
import com.backtoend.file_management_system.enums.ParentExistence;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.ItemRow;
import com.backtoend.file_management_system.model.ParentNode;
import com.backtoend.file_management_system.util.FeedbackMsg;
import com.backtoend.file_management_system.util.OperationResult;
import com.backtoend.file_management_system.enums.OperationStatus;
import javafx.concurrent.Task;
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
    private boolean scanInProgress = false;
    private int parentNodeId;

    public ScanImport(Context context) {
        this.context = context;
    }
    public void handleScanImport(Window window, ScanImportCallback callback) {
        // Checking if there is already a scan in progress
        if (this.scanInProgress) {
            logger.warning(
                    "Unable to begin scan-import; "
                    + "There is already a scan-import in progress"
            );
            // TODO: Implement error-handling
            return;
        }
        this.scanInProgress = true;

        logger.info("Initiating directory scan and import process");
        ArrayList<FeedbackMsg> feedback = new ArrayList<>();
        Database db = this.context.db();

        // Check if the DB is ready
        if (db.getStatus() != DbStatus.READY) {
            logger.warning("Unable to begin scan-import; DB is not yet initialized");
            feedback.add(new FeedbackMsg(
                    this.getClass().getName(),
                    "import.init.db.uninitialized",
                    new Object[] {}
            ));
            // TODO: Implement error-handling
            this.scanInProgress = false;
            return;
        }

        // Open directory chooser
        OperationResult<String> dirChooser = this.openDirChooser(window);
        logger.result(this.getClass().getName(), dirChooser);
        feedback.addAll(dirChooser.msg());
        if (!dirChooser.isSuccessful() || dirChooser.value() == null) {
            // TODO: Implement error-handling
            logger.warning("Directory chooser failed");
            this.scanInProgress = false;
            return;
        }
        String scanPath = dirChooser.value();

        // Checking if the chosen path already exists as a node
        this.parentNodeId = -1;
        try {
            ParentExistence doesNodeExist = db.withConnection(
                    conn -> DatabaseCheck.doesParentExist(scanPath, conn)
            );
            if (doesNodeExist == ParentExistence.IS_SCAN_LOCATION) {
                feedback.add(new FeedbackMsg(
                        this.getClass().getName(),
                        "import.init.path.exists.scan",
                        new Object[] {}
                ));
                // TODO: Implement error-handling
                logger.warning("Re-scanning existing directories is not yet supported :<");
                this.scanInProgress = false;
                return;
            } else if (doesNodeExist == ParentExistence.IS_NODE) {
                this.parentNodeId = db.withConnection(
                        conn -> DatabaseFetch.getNodeId(scanPath, conn)
                );
            }
        } catch (SQLException e) {
            logger.error("Database error encountered:", e);
            feedback.add(new FeedbackMsg(
                    this.getClass().getName(),
                    "import.init.path.exists.sqlexception",
                    new Object[] {}
            ));
            // TODO: Implement error-handling
            this.scanInProgress = false;
            return;
        }

        // Parsing the selected directory
        Task<ArrayList<ItemRow>> parseDirTask = new Task<>() {
            @Override
            protected ArrayList<ItemRow> call() throws IOException {
                return DirectoryHandler.parseDir(scanPath);
            }
        };
        parseDirTask.setOnFailed(event -> {
            logger.error("File operation failed:", parseDirTask.getException());
            feedback.add(new FeedbackMsg(
                    this.getClass().getName(),
                    "import.init.parse.failure",
                    new Object[] {}
            ));
            // TODO: Implement error-handling
            this.scanInProgress = false;
        });
        parseDirTask.setOnSucceeded(event -> {
            ArrayList<ItemRow> items = parseDirTask.getValue();

            // Displaying import overview
            OperationResult<Void> showOverview = showOverview(window, items);
            logger.result(this.getClass().getName(), showOverview);
            feedback.addAll(showOverview.msg());

            if (!showOverview.isSuccessful()) {
                logger.warning("Import overview failed");
                // TODO: Implement error-handling
                this.scanInProgress = false;
                return;
            }

            // Preparing DB insert
            ItemRow parentRow;
            try {
                parentRow = getParentRow(scanPath);
            } catch (IOException e) {
                logger.error("File operation failed:", e);
                feedback.add(new FeedbackMsg(
                        this.getClass().getName(),
                        "import.init.parse.parent.failed",
                        new Object[] {}
                ));
                // TODO: Implement error-handling
                this.scanInProgress = false;
                return;
            }
            ParentNode parentNode = getScanLocation(parentRow);

            // Yes, I am creating a task inside a task. I'm sorry.
            Task<OperationResult<Integer>> bulkDbInsert = new Task<>() {
                @Override
                protected OperationResult<Integer> call() throws SQLException {
                    return db.withTransaction(conn ->
                            DatabaseInsert.insertNewSet(
                                    parentNode, items, conn
                            ));
                }
            };
            bulkDbInsert.setOnFailed(newEvent -> {
                logger.error("Database operation failed:", bulkDbInsert.getException());
                feedback.addAll(bulkDbInsert.getValue().msg());
                // TODO: Implement error-handling
                this.scanInProgress = false;
            });
            bulkDbInsert.setOnSucceeded(newEvent -> {
                feedback.addAll(bulkDbInsert.getValue().msg());
                callback.onSuccess(bulkDbInsert.getValue().value());
                this.scanInProgress = false;
            });
            Thread insertThread = new Thread(bulkDbInsert);
            insertThread.setDaemon(true);
            insertThread.start();
        });
        Thread scanDirectory = new Thread(parseDirTask);
        scanDirectory.setDaemon(true);
        scanDirectory.start();
    }

    // === Utility Methods === \\

    /**
     * Creates an ItemRow instance for the selected directory
     * @param path Scanned directory
     * @return ItemRow instance with info about the directory item
     * @throws IOException If any file operation cannot be executed
     */
    private ItemRow getParentRow(String path) throws IOException {
        ItemRow parent = DirectoryHandler.parseParent(path);
        if (this.parentNodeId != -1) {
            parent = parent.setNewId(this.parentNodeId);
        }
        return parent;
    }

    // TODO: Allow the user to define these values
    /**
     * Creates a ParentNode instance for the given parent node
     * @param parent Scan location ItemRow
     * @return The created parent node
     */
    private ParentNode getScanLocation(ItemRow parent) {
        return new ParentNode(
                parent, 0, "MANUAL", "USER_SELECTION", 0, ItemStatus.ACTIVE
        );
    }
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
