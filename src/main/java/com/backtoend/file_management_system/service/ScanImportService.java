package com.backtoend.file_management_system.service;

import com.backtoend.file_management_system.MainApplication;
import com.backtoend.file_management_system.controller.FileExplorerController;
import com.backtoend.file_management_system.database.Database;
import com.backtoend.file_management_system.database.DatabaseSelect;
import com.backtoend.file_management_system.error_handling.FeedbackMsg;
import com.backtoend.file_management_system.error_handling.OperationResult;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.*;
import com.backtoend.file_management_system.util.I18n;
import javafx.concurrent.Task;
import javafx.stage.Window;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class ScanImportService {
    private static final AppLogger logger = AppLogger.getLogger(ScanImportService.class);
    private final I18n i = I18n.getInstance();
    private final Database db = MainApplication.getDbInstance();
    private final DirectoryHandler dirHandler = MainApplication.getDirHandler();

    private volatile AvailabilityStatus status = AvailabilityStatus.AVAILABLE;
    private boolean acceptingImports = true;
    private IItemInformation parentItem;
    private ImportMode mode;
    private ArrayList<FeedbackMsg> feedback;
    private Consumer<Integer> callback;
    private CompletableFuture<Void> activeImport;

    enum ImportMode {
        NEW,
        TRANSFORM,
        RESCAN
    }
    enum AvailabilityStatus {
        AVAILABLE,
        BUSY,
        SHUTTING_DOWN
    }

    public CompletableFuture<Void> shutdown() {
        this.acceptingImports = false;
        if (this.activeImport == null) {
            return CompletableFuture.completedFuture(null);
        }
        return this.activeImport;
    }

    public void handleScanImport(Window window, Consumer<Integer> callback) {
        // Checking if a scan is currently in progress
        if (!acceptingImports && this.status != AvailabilityStatus.AVAILABLE) {
            String logMsg = (this.status == AvailabilityStatus.BUSY) ?
                    "import.scan.ongoing.error.log" :
                    "import.scan.ongoing.offline.log";
            String errorKey = (this.status == AvailabilityStatus.BUSY) ?
                    "import.scan.ongoing.error" : "import.scan.ongoing.offline";
            logger.warning(logMsg);
            handleError(null, errorKey, window, false);
            return;
        }
        this.status = AvailabilityStatus.BUSY;
        this.activeImport = new CompletableFuture<>();
        this.feedback.add(FeedbackMsg.create(
                "import.scan.ongoing.success",
                null
        ));

        // Checking if the database is ready
        if (!this.db.ready()) {
            logger.warning("Unable to begin scan-import before the database has been initialized");
            handleError(null, "import.db.initialization.error", window, false);
            this.onCompletion();
            return;
        }
        this.feedback.add(FeedbackMsg.create(
                "import.db.initialization.success",
                null
        ));

        logger.info("Initiating scan-import process");
        this.parentItem = null;
        this.mode = null;
        this.feedback = new ArrayList<>();
        this.callback = callback;

        String directory = this.openDirectoryChooser(window);
        if (directory == null) {
            logger.warning("Received invalid directory from the user");
            handleError(this.feedback, "import.directory.error", window, false);
            this.onCompletion();
            return;
        }
        this.feedback.add(FeedbackMsg.create(
                "import.directory.success",
                new Object[] { directory }
        ));

        // Fetching directory content and info
        getDirectoryInfo(window, directory);
    }

    // === Private Helper Methods === \\
    private String openDirectoryChooser(Window window) {
        FileExplorerController fExCon = new FileExplorerController(dirHandler);
        return fExCon.openDirectoryChooser(window);
    }

    private void getDirectoryInfo(Window window, String path) {
        Task<ArrayList<ScannedItem>> directoryContent = new Task<>() {
            @Override
            protected ArrayList<ScannedItem> call() throws SQLException {
                int parentId = -1;
                try {
                    parentId = db.read(conn -> DatabaseSelect.getItemId(path, conn)).get();
                } catch (ExecutionException e) {
                    logger.error(
                            "Operation could not be executed:",
                            "getDirectoryInfo",
                            this.getClass().getName(),
                            e.getCause()
                    );
                } catch (InterruptedException e) {
                    logger.error(
                            "Thread is being interrupted:",
                            "getDirectoryInfo",
                            this.getClass().getName(),
                            e
                    );
                    shutdown();
                    getDirectoryInfo(window, path);
                }
                ScannedItem parentDetails = (ScannedItem) dirHandler.parseParent(path);
                if (parentId == -1) {
                    mode = ImportMode.NEW;
                    parentItem = parentDetails;
                } else {
                    ItemRow dbParent = null;
                    boolean isScanlocation = false;
                    int effectiveParentId = parentId;
                    try {
                        dbParent = (ItemRow) db.read(conn -> DatabaseSelect.getInfo(effectiveParentId, conn)).get();
                    } catch (ExecutionException e) {
                        logger.error(
                                "Operation could not be executed:",
                                "getDirectoryInfo",
                                this.getClass().getName(),
                                e.getCause()
                        );
                        handleError(feedback, "import.directory.parse.error", window, false);
                        onCompletion();
                    } catch (InterruptedException e) {
                        logger.error(
                                "Thread is being interrupted:",
                                "getDirectoryInfo",
                                this.getClass().getName(),
                                e
                        );
                        shutdown();
                        getDirectoryInfo(window, path);
                    }
                    try {
                        isScanlocation = db.read(conn -> DatabaseSelect.isScanLocation(effectiveParentId, conn)).get();
                    } catch (ExecutionException e) {
                        logger.error(
                                "Operation could not be executed:",
                                "getDirectoryInfo",
                                this.getClass().getName(),
                                e.getCause()
                        );
                        handleError(feedback, "import.directory.parse.error", window, false);
                        onCompletion();
                    } catch (InterruptedException e) {
                        logger.error(
                                "Thread is being interrupted:",
                                "getDirectoryInfo",
                                this.getClass().getName(),
                                e
                        );
                        shutdown();
                        getDirectoryInfo(window, path);
                    }
                    if (!isScanlocation) {
                        mode = ImportMode.TRANSFORM;
                        parentItem = dbParent;
                    } else {
                        mode = ImportMode.RESCAN;
                        try {
                            ItemRow effectiveParent = dbParent;
                            parentItem = (ScanLocation) db.read(conn -> DatabaseSelect.getScanLocationOf(effectiveParent, conn)).get();
                        } catch (ExecutionException e) {
                            logger.error(
                                    "Operation could not be executed:",
                                    "getDirectoryInfo",
                                    this.getClass().getName(),
                                    e.getCause()
                            );
                            handleError(feedback, "import.directory.parse.error", window, false);
                            onCompletion();
                        } catch (InterruptedException e) {
                            logger.error(
                                    "Thread is being interrupted:",
                                    "getDirectoryInfo",
                                    this.getClass().getName(),
                                    e
                            );
                            shutdown();
                            getDirectoryInfo(window, path);
                        }
                    }
                    try {
                        return dirHandler.parseDirectory(path);
                    } catch (IOException e) {

                    }
                }
            }
        }
    }

    private void onCompletion() {
        if (!acceptingImports) {
            this.status = AvailabilityStatus.SHUTTING_DOWN;
        } else {
            this.status = AvailabilityStatus.AVAILABLE;
        }
        this.activeImport.complete(null);
        this.activeImport = null;
    }

    // Error-handling
    private void handleError(ArrayList<FeedbackMsg> msgs, String header, String body, Window window, boolean isFatal) {
        OperationResult<Void> result = OperationResult.failed(null, msgs);
        String headerText = i.get(header);
        String bodyText = i.get(body);
        MainApplication.getErrorHandler().showOperationResult(headerText, bodyText, window, result, isFatal);
    }
    private void handleError(ArrayList<FeedbackMsg> msgs, String key, Window window, boolean isFatal) {
        String header = key + ".header";
        String body = key + ".body";
        handleError(msgs, header, body, window, isFatal);
    }
}
