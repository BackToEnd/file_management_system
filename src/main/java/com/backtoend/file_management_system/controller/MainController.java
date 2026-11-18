package com.backtoend.file_management_system.controller;

import com.backtoend.file_management_system.MainApplication;
import com.backtoend.file_management_system.error_handling.ErrorHandler;
import com.backtoend.file_management_system.error_handling.OperationResult;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.service.ScanImportService;
import com.backtoend.file_management_system.util.I18n;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Window;

import java.io.IOException;
import java.sql.SQLException;
import java.text.MessageFormat;

/// Main controller class. Handles user interactions with main application elements,
/// initiates relevant processes, maintains all other base UI controllers.
/// @author Rogue049
public class MainController {
    /// Logger instance
    private static final AppLogger logger = AppLogger.getLogger(MainController.class);
    /// How many times the shutdown may be re-attempted before considering the shutdown
    /// failed
    private static final int PREVENT_SOFTLOCK = 20;
    /// `ErrorHandler` singleton instance
    private final ErrorHandler errors = MainApplication.getErrorHandler();
    /// `I18n` instance
    private final I18n i = I18n.getInstance();
    /// `ScanImportService` instance
    private final ScanImportService scanImport;

    // region FXML elements
    /// {@link OverviewController} instance
    @FXML
    private OverviewController overviewController;
    /// Pressing this button initiates a scan-import process
    @FXML
    private Button browseBtn;
    /// Pressing this button terminates the application
    @FXML
    private Button killBtn;
    @FXML
    private Button errorBtn;
    // endregion

    /// Constructor. Creates a new `MainController` instance and configures all required attributes.
    public MainController() {
        this.scanImport = new ScanImportService();
        // Initialization things
        try {
            MainApplication.getDbInstance()
                    .initialize(this::handleOperationResult);
        } catch (SQLException e) {
            logger.error("Encountered a problem during initialization:", e);
        } catch (IOException e) {
            logger.error("Unable to create or access DB directory:", e);
        }
    }

    /// Initialization of this controller. Creates string bindings for
    /// all main UI elements.
    @FXML
    private void initialize() {
        logger.info("Initiating main application interface");
        browseBtn.textProperty().bind(i.bind("main.buttons.browse"));
        killBtn.textProperty().bind(i.bind("main.buttons.kill"));
        errorBtn.textProperty().bind(i.bind("main.buttons.error"));
    }

    // TODO: Link scan-import process in Javadoc comment
    /// Initiate the scan-import process.
    /// @param event event created by the import button being pressed
    @FXML
    private void onChooseDirectory(ActionEvent event) {
        logger.info("Initiating scan-import sequence");
        Window window = ((Button) event.getSource()).getScene().getWindow();
        scanImport.handleScanImport(window, this::updateAfterImport);
    }

    private void updateAfterImport(Integer id) {
        logger.info("Updating overview tree with ID: " + id);
    }
    // TODO: Make sure this method works with any ongoing background processes
    /// Shut down the application
    @FXML
    private void onExit() {
        MainController.terminate();
    }

    @FXML
    private void showTestError() {
        try {
            throw new RuntimeException("Test exception thrown");
        } catch (RuntimeException e) {
            try {
                throw new RuntimeException("Caught text exception", e);
            } catch (RuntimeException ex) {
                try {
                    throw new RuntimeException("Added suppressed exception");
                } catch (RuntimeException exception) {
                    ex.addSuppressed(exception);
                    logger.error("Test exception thrown:", ex);
                    errors.showError(null, null, errorBtn.getScene().getWindow());
                }
            }
        }
    }

    /// Begins generating the overview tree after the DB initialization
    /// completes. Should **not** be called before the initialization finishes.
    public void enableOverview() {
        // TODO: Call a method that begins building the overview tree
    }

    // FIXME: Shut down executor services
    public static void terminate() {
        MainApplication.getDbInstance().shutdown();
        Task<Boolean> shutDown = MainController.getShutDownTask();
        Thread thread = new Thread(shutDown);
        thread.setDaemon(true);
        thread.start();
    }

    /// Deals with an `OperationResult` obtained from a process.\
    /// If the operation status is `FAILED`, the error is treated as fatal and the application is terminated.
    /// @param result `OperationResult` to be handled
    private void handleOperationResult(OperationResult<?> result) {
        logger.result(
                result,
                "handleOperationResult",
                this.getClass().getName()
        );
        if (result.isFailed()) {
            errors.showOperationResult(
                    killBtn.getScene().getWindow(), result
            );
        }
    }

    /// Creates a `Task` to asynchronously shut down executor services. This process will be repeated until
    /// the application can either shut down successfully (all executor services have terminated), or
    /// {@value #PREVENT_SOFTLOCK} attempts have been made.
    /// @return the created task; the task returns `true` if all executor services have shut down, and `false`
    /// if the shut-down was not possible.
    private static Task<Boolean> getShutDownTask() {
        logger.info("Shut-down initiated; shutting down executor services");
        Task<Boolean> shutDown = new Task<>() {
            @Override
            protected Boolean call() {
                logger.debug(
                        "Awaiting all executor service shutdowns on background thread"
                );
                boolean canShutDown = false;
                int preventSoftlock = 0;
                while (!canShutDown && preventSoftlock < PREVENT_SOFTLOCK) {
                    logger.trace(
                            "Shut-down attempt: " + preventSoftlock
                    );
                    canShutDown = MainApplication.getDbInstance().awaitTermination();
                    if (preventSoftlock == (PREVENT_SOFTLOCK / 2)) {
                        logger.warning(MessageFormat.format(
                                "Shut-down has been attempted {0} times; half as many times as is allowed",
                                new Object[] { preventSoftlock }
                                )
                        );
                    }
                    preventSoftlock++;
                }
                return canShutDown;
            }
        };
        // TODO: What amount of error-handling can be done here?
        shutDown.setOnCancelled(event -> {
            logger.warning(
                    "Shut-down has been canceled",
                    "getShutDownTask (canceled)",
                    MainController.class.getName()
            );
        });
        shutDown.setOnFailed(event -> {
            logger.warning(
                    "Shut-down has failed",
                    "getShutDownTask (failed)",
                    MainController.class.getName()
            );
        });
        shutDown.setOnSucceeded(event -> {
            if (shutDown.getValue()) {
                logger.info(
                        "Successfully shut down all executor services; application shutting down",
                        "getShutDownTask (success)",
                        MainController.class.getName()
                );
                Platform.exit();
            } else {
                logger.warning(
                        "Unable to shut down executor services; something might be stuck",
                        "getShutDownTask (success)",
                        MainController.class.getName()
                );
            }
        });
        return shutDown;
    }
}
