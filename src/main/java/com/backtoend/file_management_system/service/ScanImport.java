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
import com.backtoend.file_management_system.exceptions.OperationException;
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
    private ItemRow parentItemRow;
    private String scannedPath;

    /// Initiates the scan-import process by performing the following steps:\
    /// 1. Opens a directory chooser and allows the user to select a directory
    /// 2. Scans the selected directory and displays an import overview
    /// 3. Imports all scanned items into the DB
    /// @param window Where directory chooser and import overview are supposed to be displayed
    /// @param callback What to do with the newly inserted parent item;
    ///                 is used to allow {@link com.backtoend.file_management_system.controller.MainController}
    ///                 to update the overview tree after the completion of
    ///                 asynchronous operations
    public void scanImportProcess(Window window, ScanImportCallback callback) {
        // General set-up
        Database db = MainApplication.getDatabaseInstance();
        ArrayList<FeedbackMsg> feedback = new ArrayList<>();
        this.parentItemRow = null;
        this.parentNodeId = -1;
        this.scannedPath = null;

        try {
            // Checking if there is already a scan in progress
            if (this.scanInProgress) {
                feedback.add(FeedbackMsg.create(
                        "import.init.scan.ongoing",
                        new Object[] {}
                ));
                throw new OperationException(
                        "Unable to begin scan-import; scan-import is already running!"
                );
            }

            this.scanInProgress = true;
            logger.info("Initiating directory scan");

            // Checking if the DB is available
            if (db.getStatus() != DbStatus.READY) {
                feedback.add(FeedbackMsg.create(
                        "import.init.db.uninitialized",
                        new Object[] {}
                ));
                throw new OperationException(
                        "Unable to proceed with scan-import; database is unavailable"
                );
            }

            // Open directory chooser
            OperationResult<String> dirChooser = this.openDirChooser(window);
            logger.result(dirChooser);
            feedback.addAll(dirChooser.msg());

            if (!dirChooser.isSuccessful() || dirChooser.value() == null) {
                throw new OperationException(
                        "Directory chooser returned unsuccessfully"
                );
            }

            this.scannedPath = dirChooser.value();

            // Checking if the chosen path already exists in the DB
            this.parentNodeId = -1;
            try {
                ParentExistence doesNodeExist = db.withConnection(
                        conn -> DatabaseCheck.doesParentExist(this.scannedPath, conn)
                );
                if (doesNodeExist == ParentExistence.IS_SCAN_LOCATION) {
                    feedback.add(FeedbackMsg.create(
                            "import.init.path.exists.scan",
                            new Object[] {}
                    ));
                    throw new OperationException(
                            "Unable to proceed with scan-import; selected path is already a known scan location"
                    );
                } else if (doesNodeExist == ParentExistence.IS_NODE) {
                    this.parentNodeId = db.withConnection(
                            conn -> DatabaseFetch.getNodeId(this.scannedPath, conn)
                    );
                    parentItemRow = db.withConnection(
                            conn -> DatabaseFetch.getInfo(this.parentNodeId, conn)
                    );
                }
            } catch (SQLException e) {
                feedback.add(FeedbackMsg.create(
                        "import.init.path.exists.sqlexception",
                        new Object[] {}
                ));
                throw new OperationException(
                        "Encountered SQLException while checking status of selected directory",
                        e
                );
            }

            // Parsing the selected directory
            Task<ArrayList<ItemRow>> parseDirTask = new Task<>() {
                @Override
                protected ArrayList<ItemRow> call() throws IOException {
                    return DirectoryHandler.parseDir(scannedPath);
                }
            };
            parseDirTask.setOnFailed(event -> {
                logger.error(
                        "File operation failed:",
                        "handleScanImport",
                        this.getClass().getName(),
                        parseDirTask.getException()
                );
                feedback.add(FeedbackMsg.create(
                        "import.init.parse.failure",
                        new Object[] {}
                ));
                this.scanInProgress = false;
                // TODO: Error-handling
            });
            parseDirTask.setOnSucceeded(event -> {
                logger.info(
                        "Directory content obtained",
                        "scanImportProccess",
                        this.getClass().getName()
                );
                ArrayList<ItemRow> items = parseDirTask.getValue();

                // Displaying import overview
                OperationResult<Void> showOverview = showOverview(window, items);
                logger.result(showOverview, "handleScanImport", this.getClass().getName());
                feedback.addAll(showOverview.msg());

                if (!showOverview.isSuccessful()) {
                    logger.warning(
                            "Import overview failed",
                            "handleScanImport",
                            this.getClass().getName()
                    );
                    this.scanInProgress = false;
                    // TODO: Error-handling
                } else {
                    handleDbInsert(feedback, items, callback);
                }
            });

            logger.info("Initiating asynchronous directory reading");
            Thread thread = new Thread(parseDirTask);
            thread.setDaemon(true);
            thread.start();

        } catch (OperationException e) {
            // TODO: Proper error handling
            logger.error(
                    "Scan-Import process could not complete:", e
            );
            this.scanInProgress = false;
        }
    }

    // === Utility Methods === \\

    /// Final step in the scan-import process that deals with the bulk DB insert
    /// of all scanned items
    /// @param feedback List of all feedback messages accumulated until now
    /// @param items List of scanned items from the selected directory
    /// @param callback What to do after the insert is completed successfully
    private void handleDbInsert(ArrayList<FeedbackMsg> feedback, ArrayList<ItemRow> items, ScanImportCallback callback) {
        logger.info("Initiating bulk Db insert");
        Database db = MainApplication.getDatabaseInstance();
        if (this.parentItemRow == null) {
            logger.debug("Attempting to create new parent item");
            try {
                this.parentItemRow = getParentRow();
            } catch (IOException e) {
                logger.error("File operation failed:", e);
                feedback.add(FeedbackMsg.create(
                        "import.init.parse.parent.failed",
                        new Object[]{}
                ));
                this.scanInProgress = false;
                // TODO: Error-handling
                return;
            }
        }
        ScanLocation scanLocation = getScanLocation(this.parentItemRow);
        Task<OperationResult<ScanLocation>> bulkDbInsert = new Task<>() {
            @Override
            protected OperationResult<ScanLocation> call() throws SQLException {
                return db.withTransaction(conn ->
                        DatabaseInsert.insertOrUpdateSet(
                                scanLocation, items, conn
                        ));
            }
        };
        bulkDbInsert.setOnFailed(event -> {
            logger.error(
                    "Database bulk-insert failed:",
                    "handleDbInsert",
                    this.getClass().getName(),
                    bulkDbInsert.getException()
                    );
            this.scanInProgress = false;
            // TODO: Error-handling
        });
        bulkDbInsert.setOnSucceeded(event -> {
            logger.info(
                    "DB insert was successful",
                    "handleDbInsert",
                    this.getClass().getName()
            );
            feedback.addAll(bulkDbInsert.getValue().msg());
            callback.onSuccess(bulkDbInsert.getValue().value());
            this.scanInProgress = false;
        });
        logger.info("Attempting asynchronous bulk DB insert");
        Thread thread = new Thread(bulkDbInsert);
        thread.setDaemon(true);
        thread.start();
    }

    /// Fetches information about the chosen path
    /// @return An `ItemRow` of the selected path
    /// @throws IOException If the path cannot be accessed
    private ItemRow getParentRow() throws IOException {
        return DirectoryHandler.parseParent(this.scannedPath);
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
