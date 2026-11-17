package com.backtoend.file_management_system.service;

import com.backtoend.file_management_system.MainApplication;
import com.backtoend.file_management_system.controller.FileExplorerController;
import com.backtoend.file_management_system.database.Database;
import com.backtoend.file_management_system.database.DatabaseSelect;
import com.backtoend.file_management_system.error_handling.FeedbackMsg;
import com.backtoend.file_management_system.error_handling.OperationResult;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.logging.SourceLocation;
import com.backtoend.file_management_system.model.*;
import com.backtoend.file_management_system.util.I18n;
import com.backtoend.file_management_system.util.PreviewHandler;
import javafx.concurrent.Task;
import javafx.stage.Window;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class ScanImportService {
    private static final AppLogger logger = AppLogger.getLogger(ScanImportService.class);
    private static final int UNTIL_TIMEOUT = 5;
    private final I18n i = I18n.getInstance();
    private final Database db = MainApplication.getDbInstance();
    private final DirectoryHandler dirHandler = MainApplication.getDirHandler();
    private final CompletableFuture<Void> shutdownCompleted = new CompletableFuture<>();

    private volatile AvailabilityStatus status = AvailabilityStatus.AVAILABLE;
    private volatile int untilTimeout = 0;
    private boolean acceptingImports = true;
    private IItemInformation parentItem;
    private ImportMode mode;
    private ArrayList<FeedbackMsg> feedback;
    private Consumer<Integer> callback;
    private CompletableFuture<Void> activeImport;
    private Window window;

    enum ImportMode {
        NEW,
        TRANSFORM,
        RESCAN
    }
    enum AvailabilityStatus {
        AVAILABLE,
        BUSY,
        SHUTTING_DOWN,
        OFFLINE
    }

    public CompletableFuture<Void> shutdown() {
        this.acceptingImports = false;
        status = AvailabilityStatus.SHUTTING_DOWN;
        if (this.activeImport == null) {
            return CompletableFuture.completedFuture(null);
        }
        return this.activeImport;
    }

    public void handleScanImport(Window window, Consumer<Integer> callback) {
        // Checking if a scan is currently in progress
        if (!acceptingImports && this.status != AvailabilityStatus.AVAILABLE) {
            String logMsg = (this.status == AvailabilityStatus.BUSY) ?
                    "Unable to begin new scan-import while one is already being processed" :
                    "Unable to begin new scan-import; this service is shutting down";
            String errorKey = (this.status == AvailabilityStatus.BUSY) ?
                    "import.scan.ongoing.error" : "import.scan.ongoing.offline";
            logger.warning(logMsg);
            handleError(null, errorKey, false);
            return;
        }
        this.status = AvailabilityStatus.BUSY;
        this.activeImport = new CompletableFuture<>();
        this.untilTimeout = 0;
        this.window = window;
        this.feedback.add(FeedbackMsg.create(
                "import.scan.ongoing.success"
        ));

        // Checking if the database is ready
        if (!this.db.ready()) {
            logger.warning("Unable to begin scan-import before the database has been initialized");
            handleError(null, "import.db.initialization.error", false);
            this.onCompletion();
            return;
        }
        this.feedback.add(FeedbackMsg.create(
                "import.db.initialization.success"
        ));

        logger.info("Initiating scan-import process");
        this.parentItem = null;
        this.mode = null;
        this.feedback = new ArrayList<>();
        this.callback = callback;

        String directory = this.openDirectoryChooser();
        if (directory == null) {
            logger.warning("Received invalid directory from the user");
            this.onCompletion();
            return;
        }
        this.feedback.add(FeedbackMsg.create(
                "import.directory.success", directory
        ));

        // Fetching directory content and info
        getDirectoryInfo(directory);
    }

    // === Private Helper Methods === \\
    private String openDirectoryChooser() {
        FileExplorerController fExCon = new FileExplorerController(dirHandler);
        return fExCon.openDirectoryChooser(window);
    }

    private void getDirectoryInfo(String path) {
        logger.info("Starting directory parsing");
        feedback.add(FeedbackMsg.create(
                "import.directory.parse.initiated"
        ));
        Task<ArrayList<ScannedItem>> directoryContent = new Task<>() {
            @Override
            protected ArrayList<ScannedItem> call() throws IOException {
                // Fetching info on the parent
                parentItem = dirHandler.parseParent(path);
                logger.info(parentItem.display());
                mode = ImportMode.NEW;
                feedback.add(FeedbackMsg.create(
                        "import.directory.parse.parent.success",
                        "call",
                        "Task {directoryContent}"
                ));

                // Attempting to fetch path info from database
                try {
                    ItemRow dbParent = db.read(conn -> DatabaseSelect.getInfo((ScannedItem) parentItem, conn)).get();
                    // If this method returns null, the path does not exist in the DB yet
                    if (dbParent != null) {
                        feedback.add(FeedbackMsg.create(
                                "import.directory.parse.parent.success",
                                "call",
                                "Task {directoryContent}"
                        ));
                        logger.info(dbParent.display());
                        mode = ImportMode.TRANSFORM;
                        parentItem = dbParent;

                        // Attempting to fetch scan location
                        ScanLocation scanLocation = db.read(conn -> DatabaseSelect.getScanLocationOf(dbParent, conn)).get();
                        if (scanLocation != null) {
                            feedback.add(FeedbackMsg.create(
                                    "import.directory.parse.parent.location.success",
                                    "call",
                                    "Task {directoryContent}"
                            ));
                            logger.info(scanLocation.display());
                            mode = ImportMode.RESCAN;
                            parentItem = scanLocation;
                        }
                    }
                } catch (InterruptedException e) {
                    logger.error(
                            "Thread interrupted while processing DB read",
                            "call",
                            "Task {directoryContent}",
                            e
                    );
                    feedback.add(FeedbackMsg.create(
                            "import.interrupted.parse.parent",
                            "call",
                            "Task {directoryContent}"
                    ));
                    if (status != AvailabilityStatus.SHUTTING_DOWN) {
                        feedback.add(FeedbackMsg.create(
                                "import.shutdown.initiated",
                                "call",
                                "Task {directoryContent}"
                        ));
                        shutdown();
                    }
                    handleError(feedback, "import.interrupted", false);
                    onCompletion();
                    return null;
                } catch (ExecutionException e) {
                    logger.error(
                            "Encountered an exception while fetching information on the selected path",
                            "call",
                            "Task {directoryContent}",
                            e.getCause()
                    );
                    handleError(feedback, "import.directory.parse.error", false);
                    onCompletion();
                    return null;
                }
                feedback.add(FeedbackMsg.create(
                        "import.directory.parse.parent.all.success",
                        "call",
                        "Task {directoryContent}"
                ));

                // Attempting to parse directory content
                ArrayList<ScannedItem> children = dirHandler.parseDirectory(path);
                feedback.add(FeedbackMsg.create(
                        "import.directory.parse.children.success",
                        "call",
                        "Task {directoryContent}",
                        children.size()
                ));
                return children;
            }
        };
        directoryContent.setOnCancelled(event -> {
            feedback.add(FeedbackMsg.create(
                    "import.directory.canceled",
                    "setOnCanceled", "Task {directoryContent}"
                    ));
            logger.result(OperationResult.canceled(
                    null, feedback, new SourceLocation("Task {directoryContent}", "setOnCancelled")
            ));
            onCompletion();
        });
        directoryContent.setOnFailed(event -> {
            logger.error(
                    "Encountered an error while parsing directory:",
                    "setOnFailed",
                    "Task {directoryContent}",
                    directoryContent.getException()
            );
            handleError(feedback, "import.directory.failed", false);
            onCompletion();
        });
        directoryContent.setOnSucceeded(event -> {
            feedback.add(FeedbackMsg.create(
                    "import.directory.success",
                    "setOnSucceeded",
                    "Task {directoryContent}"
            ));
            showOverview(directoryContent.getValue());
        });
        Thread thread = new Thread(directoryContent);
        thread.setDaemon(true);
        thread.start();
    }
    private void showOverview(ArrayList<ScannedItem> list) {
        feedback.add(FeedbackMsg.create("import.preview.initiated"));
        String suffix = mode.toString().toLowerCase();
        String title = "modal.import.preview.title." + suffix;
        String label = "modal.import.preview.label." + suffix;
        PreviewHandler preview = new PreviewHandler();
        Boolean importStatus = preview.showPreview(title, label, list, window);
        if (importStatus == null) {
            handleError(feedback, "import.preview.error", false);
            onCompletion();
            return;
        }
        if (!importStatus) {
            logger.info("User rejected import");
            onCompletion();
            return;
        }
        // Proceeding with the bulk-import
        dbBulkWrite(list);
    }

    private void dbBulkWrite(ArrayList<ScannedItem> list) {
        // TODO
    }

    private void onCompletion() {
        this.activeImport.complete(null);
        this.activeImport = null;
        if (this.status == AvailabilityStatus.BUSY) {
            this.status = AvailabilityStatus.AVAILABLE;
        } else if (this.status == AvailabilityStatus.SHUTTING_DOWN) {
            this.status = AvailabilityStatus.OFFLINE;
            this.shutdownCompleted.complete(null);
        }
    }

    // Error-handling
    private void handleError(ArrayList<FeedbackMsg> msgs, String header, String body, boolean isFatal) {
        logger.warning("An error was encountered and is now being handled. Printing all collected messages:");
        logger.feedback(msgs, logger::error);
        OperationResult<Void> result = OperationResult.failed(null, msgs);
        String headerText = i.get(header);
        String bodyText = i.get(body);
        MainApplication.getErrorHandler().showOperationResult(headerText, bodyText, window, result, isFatal);
    }
    private void handleError(ArrayList<FeedbackMsg> msgs, String key, boolean isFatal) {
        String header = key + ".header";
        String body = key + ".body";
        handleError(msgs, header, body, isFatal);
    }
}
