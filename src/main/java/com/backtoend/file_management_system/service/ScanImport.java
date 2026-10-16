package com.backtoend.file_management_system.service;

import com.backtoend.file_management_system.Context;
import com.backtoend.file_management_system.controller.FileExplorerController;
import com.backtoend.file_management_system.controller.ModalController;
import com.backtoend.file_management_system.database.DatabaseInsert;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.ItemHandler;
import com.backtoend.file_management_system.util.ProcessFeedback;
import javafx.stage.Window;

import java.io.IOException;

public class ScanImport {
    private final Context context;
    private static final AppLogger logger = AppLogger.getLogger(ScanImport.class);

    public ScanImport(Context context) {
        this.context = context;
    }

    // TODO: ensure that ANY user-selected path is readable so this method CANNOT error!

    /**
     * Processes directory selection, parsing, and DB inserts
     * @param window Where the DirectoryChooser and overview should be opened
     * @return ProcessFeedback with success status and message
     */
    public ProcessFeedback initiateScanImport(Window window) {
        logger.info("Initiating directory scan and import process");
        String path = this.openDirChooser(window);
        if (path != null) {
            logger.info("Valid path obtained; initiating directory scan");
            ItemHandler children = DirectoryHandler.parseDir(path);
            if (children != null && !children.getItemList().isEmpty()) {
                logger.info("Scan successful; attempting to show overview");
                if (this.showOverview(window, children)) {
                    logger.info("Import accepted; beginning DB insert");
                    if (DatabaseInsert.insertItems(children, this.context.db())) {
                        return new ProcessFeedback(true, "Operation successful");
                    }
                    return new ProcessFeedback(false, "Encountered a problem while writing into database");
                }
            }
        }
        return new ProcessFeedback(false, "Unable to complete operation; see logs for more information");
    }

    // === Utility Methods === \\

    /**
     * Open DirectoryChooser
     * @param window Where the DirectoryChooser should be opened
     * @return The user's selected directory
     */
    private String openDirChooser(Window window) {
        FileExplorerController fExCon = new FileExplorerController(this.context.dirHandler());
        return fExCon.openDirectoryChooser(window);
    }

    /**
     * Displays an overview window of scanned items
     * @param window Where the window should be opened
     * @param items Scanned items
     * @return Whether the import was accepted or canceled
     */
    private boolean showOverview(Window window, ItemHandler items) {
        try {
            return ModalController.showImportPreview(window, items.getAllItemNames());
        } catch (IOException e) {
            logger.error("Unable to display overview:", e);
            return false;
        }
    }
}
