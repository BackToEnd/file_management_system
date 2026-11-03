package com.backtoend.file_management_system.service;

import com.backtoend.file_management_system.MainApplication;
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
import com.backtoend.file_management_system.model.ScanLocation;
import com.backtoend.file_management_system.util.FeedbackMsg;
import com.backtoend.file_management_system.util.OperationResult;
import com.backtoend.file_management_system.enums.OperationStatus;
import javafx.concurrent.Task;
import javafx.stage.Window;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

/// Service class handling the scan-import workflow
/// @author Rogue049
public class ScanImport {
    private static final AppLogger logger = AppLogger.getLogger(ScanImport.class);
    /// Keeps track of whether a scan is currently in process;
    /// required to prevent two scans being active at the same time
    private boolean scanInProgress = false;
    /// Locally remembers the ID of a parent directory; easier than
    /// passing it into different methods as an argument
    private int parentNodeId;

    /// Performs the entire scan-import process:\
    /// 1. Opens a directory chooser to allow the user to pick a directory. Properly accounts for
    /// the directory chooser being closed instead of choosing a directory.
    /// 2. Browses the content of the directory, properly accounting for insufficient permission to do so.
    /// 3. Displays an import overview that allows the user to either proceed with the
    /// import, or cancel the import and discard all scanned items.
    /// 4. Bulk-inserts all scanned items and their parent path into the database if
    /// the user proceeds with the import using a transaction, which is rolled back in case any SQLException
    /// is thrown in the process.
    /// @param window The window on which directory chooser and import overview should be displayed
    /// @param callback An operation to execute after this method has run; this should be a method
    ///                 updating the overview tree. The callback is executed on the main application thread.
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
        Database db = MainApplication.getDatabaseInstance();

        // Check if the DB is ready
        if (db.getStatus() != DbStatus.READY) {
            logger.warning("Unable to begin scan-import; DB is not yet initialized");
            feedback.add(FeedbackMsg.create(
                    "import.init.db.uninitialized",
                    new Object[] {}
            ));
            // TODO: Implement error-handling
            this.scanInProgress = false;
            return;
        }

        // Open directory chooser
        OperationResult<String> dirChooser = this.openDirChooser(window);
        logger.result(dirChooser);
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
                feedback.add(FeedbackMsg.create(
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
            feedback.add(FeedbackMsg.create(
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
            feedback.add(FeedbackMsg.create(
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
            logger.result(showOverview);
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
                feedback.add(FeedbackMsg.create(
                        "import.init.parse.parent.failed",
                        new Object[] {}
                ));
                // TODO: Implement error-handling
                this.scanInProgress = false;
                return;
            }
            ScanLocation scanLocation = getScanLocation(parentRow);

            // Yes, I am creating a task inside a task. I'm sorry.
            Task<OperationResult<Integer>> bulkDbInsert = new Task<>() {
                @Override
                protected OperationResult<Integer> call() throws SQLException {
                    return db.withTransaction(conn ->
                            DatabaseInsert.insertNewSet(
                                    scanLocation, items, conn
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

    /// Fetches information about the chosen path
    /// @param path User-selected path
    /// @return An `ItemRow` of the selected path
    /// @throws IOException If the path cannot be accessed
    private ItemRow getParentRow(String path) throws IOException {
        ItemRow parent = DirectoryHandler.parseParent(path);
        if (this.parentNodeId != -1) {
            parent = parent.setNewId(this.parentNodeId);
        }
        return parent;
    }

    // TODO: Allow the user to define these values
    /// Creates a {@link ScanLocation} of the chosen path
    /// @param parent Previously created {@link ItemRow} of the user-selected path
    /// @return `ScanLocation` instance
    private ScanLocation getScanLocation(ItemRow parent) {
        return new ScanLocation(
                parent, 0, "MANUAL", "USER_SELECTION", 0, ItemStatus.ACTIVE
        );
    }

    /// Opens a new {@link javafx.stage.DirectoryChooser} to allow the user to pick a directory
    /// @param window Where the `DirectoryChooser` should open
    /// @return An {@link OperationResult} containing status, messages, and the selected path (nullable).
    /// Status will be `SUCCESS` if the user selected a directory, `FAILED` if anything in this process went
    /// wrong, and `CANCELED` if the user closed the window without selecting a directory.
    private OperationResult<String> openDirChooser(Window window) {
        FileExplorerController fExCon = new FileExplorerController(MainApplication.getDirectoryHandlerInstance());
        return fExCon.openDirectoryChooser(window);
    }

    // TODO: This thing does not account for recursive scanning yet
    /// Displays the import preview to the user, containing all detected items contained within the parent folder.
    /// @param window Where the preview modal should be displayed
    /// @param items A list of scanned child items to be displayed
    /// @return An {@link OperationResult} containing status and messages. Status will be `SUCCESS` if
    /// the user allowed the import and nothing else went wrong, `CANCELED` if the user rejected the import,
    /// and `FAILED` if any problem was encountered.
    private OperationResult<Void> showOverview(Window window, ArrayList<ItemRow> items) {
        try {
            boolean startImport = ModalController.showImportPreview(window, items);
            ArrayList<FeedbackMsg> feedback = new ArrayList<>();
            if (startImport) {
                feedback.add(FeedbackMsg.create(
                        "modal.import.overview.accepted",
                        new Object[] {}
                ));
                return OperationResult.create(OperationStatus.SUCCESS, null, feedback);
            }
            feedback.add(FeedbackMsg.create(
                    "modal.import.overview.denied",
                    new Object[] {}
            ));
            return OperationResult.create(OperationStatus.CANCELED, null, feedback);
        } catch (IOException e) {
            logger.error("Critical error encountered: ", e);
            ArrayList<FeedbackMsg> feedback = new ArrayList<>();
            feedback.add(FeedbackMsg.create(
                    "modal.import.overview.failed",
                    new Object[] {}
            ));
            return OperationResult.create(OperationStatus.FAILED, null, feedback);
        }
    }
}
