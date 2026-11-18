package com.backtoend.file_management_system.database;

import com.backtoend.file_management_system.MainApplication;
import com.backtoend.file_management_system.error_handling.FeedbackMsg;
import com.backtoend.file_management_system.error_handling.OperationResult;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.model.ItemRow;
import com.backtoend.file_management_system.model.ScanLocation;
import com.backtoend.file_management_system.model.ScannedItem;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class DatabaseBulkInsert {
    private static final AppLogger logger = AppLogger.getLogger(DatabaseBulkInsert.class);

    private final ArrayList<FeedbackMsg> feedback;
    private final Database db;

    public DatabaseBulkInsert() {
        this.feedback = new ArrayList<>();
        this.db = MainApplication.getDbInstance();
    }

    public OperationResult<Integer> insertNewSet(
            ScannedItem parent, ScanLocation settings, ArrayList<ScannedItem> list
    ) {
        HashMap<String, Integer> descendants;
        try {
            descendants = db.read(conn -> DatabaseSelect.reverseEngineerParents(parent.path(), conn)).get();

        } catch (InterruptedException e) {
            feedback.add(FeedbackMsg.create("database.insert.bulk.descendants.interrupted"));
            Thread.currentThread().interrupt();
            return OperationResult.canceled(null, feedback, e);
        } catch (ExecutionException e) {
            feedback.add(FeedbackMsg.create("database.insert.bulk.descendants.exception"));
            return OperationResult.failed(null, feedback, e.getCause());
        }
        feedback.add(FeedbackMsg.create("database.insert.bulk.descendants.success", descendants.size()));

        try {
            int obtainedId = db.writeWithTransaction(conn -> {
                int parentId = DatabaseInsert.insertChildOf(null, parent, conn);
                feedback.add(FeedbackMsg.create(
                        "database.insert.bulk.parent.id.success",
                        "insertNewSet {db.writeWithTransaction}",
                        this.getClass().getName(),
                        parentId
                ));
                DatabaseInsert.createScanLocation(parentId, settings, conn);
                feedback.add(FeedbackMsg.create(
                        "database.insert.bulk.parent.scan.success",
                        "insertNewSet {db.writeWithTransaction}",
                        this.getClass().getName()
                ));
                DatabaseInsert.insertChildrenOf(parentId, list, descendants, conn);
                feedback.add(FeedbackMsg.create(
                        "database.insert.bulk.children.success",
                        "insertNewSet {db.writeWithTransaction}",
                        this.getClass().getName()
                ));
                return parentId;
            }).get();
            feedback.add(FeedbackMsg.create(
                    "database.insert.bulk.transaction.success"
            ));
            return OperationResult.success(obtainedId, feedback);
        } catch (InterruptedException e) {
            feedback.add(FeedbackMsg.create("database.insert.bulk.children.interrupted"));
            Thread.currentThread().interrupt();
            return OperationResult.canceled(null, feedback, e);
        } catch (ExecutionException e) {
            feedback.add(FeedbackMsg.create("database.insert.bulk.children.exception"));
            return OperationResult.failed(null, feedback, e.getCause());
        }
    }

    public OperationResult<Integer> transformItem(
            ItemRow parent, ScanLocation settings, ArrayList<ScannedItem> list
    ) {
        HashMap<String, Integer> descendants;
       try {
           descendants = db.read(conn -> DatabaseSelect.reverseEngineerParents(parent.path(), conn)).get();
       } catch (InterruptedException e) {
           feedback.add(FeedbackMsg.create("database.insert.bulk.descendants.interrupted"));
           Thread.currentThread().interrupt();
           return OperationResult.canceled(null, feedback, e);
       } catch (ExecutionException e) {
           feedback.add(FeedbackMsg.create("database.insert.bulk.descendants.exception"));
           return OperationResult.failed(null, feedback, e.getCause());
       }
       feedback.add(FeedbackMsg.create("database.insert.bulk.descendants.success", descendants.size()));

        try {
            db.writeWithTransaction(conn -> {
                DatabaseInsert.updateItem(parent, conn);
                feedback.add(FeedbackMsg.create(
                        "database.insert.bulk.parent.update.success"
                ));
                DatabaseInsert.createScanLocation(parent.id(), settings, conn);
                feedback.add(FeedbackMsg.create(
                        "database.insert.bulk.parent.scan.success",
                        "transformItem {db.writeWithTransaction}",
                        this.getClass().getName()
                ));
                DatabaseInsert.insertChildrenOf(parent.id(), list, descendants, conn);
                feedback.add(FeedbackMsg.create(
                        "database.insert.bulk.children.success",
                        "transformItem {db.writeWithTransaction}",
                        this.getClass().getName()
                ));
                return null;
            }).get();
            feedback.add(FeedbackMsg.create(
                    "database.insert.bulk.transaction.success"
            ));
            return OperationResult.success(parent.id(), feedback);
        } catch (InterruptedException e) {
            feedback.add(FeedbackMsg.create("database.insert.bulk.children.interrupted"));
            Thread.currentThread().interrupt();
            return OperationResult.canceled(null, feedback, e);
        } catch (ExecutionException e) {
            feedback.add(FeedbackMsg.create("database.insert.bulk.children.exception"));
            return OperationResult.failed(null, feedback, e.getCause());
        }
    }

    public OperationResult<Integer> rescanPath(
            ScanLocation parent, ArrayList<ScannedItem> list
    ) {
        HashMap<String, Integer> potentialDescendants;
        HashMap<String, Integer> dbDescendants;
        try {
            potentialDescendants = db.read(conn -> DatabaseSelect.reverseEngineerParents(parent.item().path(), conn)).get();
            dbDescendants = db.read(conn -> DatabaseSelect.getDescendantInfo(parent.item().id(), conn)).get();
        } catch (InterruptedException e) {
            feedback.add(FeedbackMsg.create("database.insert.bulk.descendants.interrupted"));
            Thread.currentThread().interrupt();
            return OperationResult.canceled(null, feedback, e);
        } catch (ExecutionException e) {
            feedback.add(FeedbackMsg.create("database.insert.bulk.descendants.exception"));
            return OperationResult.failed(null, feedback, e.getCause());
        }
        feedback.add(FeedbackMsg.create("database.insert.bulk.descendants.all.success", potentialDescendants.size(), dbDescendants.size()));
        try {
            db.writeWithTransaction(conn -> {
                DatabaseInsert.updateScanLocation(parent, conn);
                feedback.add(FeedbackMsg.create(
                        "database.insert.bulk.parent.scan.update.success",
                        "rescanPath {db.writeWithTransaction}",
                        this.getClass().getName(),
                        parent.item().id()
                ));
                DatabaseInsert.updateItems(
                        parent.item().id(), list, potentialDescendants, dbDescendants, conn
                );
                feedback.add(FeedbackMsg.create(
                        "database.insert.bulk.children.update.success",
                        "rescanPath {db.writeWithTransaction}",
                        this.getClass().getName()
                ));
                return null;
            }).get();
            return OperationResult.success(parent.item().id(), feedback);
        } catch (InterruptedException e) {
            feedback.add(FeedbackMsg.create("database.insert.bulk.children.interrupted"));
            Thread.currentThread().interrupt();
            return OperationResult.canceled(null, feedback, e);
        } catch (ExecutionException e) {
            feedback.add(FeedbackMsg.create("database.insert.bulk.children.exception"));
            return OperationResult.failed(null, feedback, e.getCause());
        }
    }
}
