package com.backtoend.file_management_system.service;

import com.backtoend.file_management_system.MainApplication;
import com.backtoend.file_management_system.database.Database;
import com.backtoend.file_management_system.database.DatabaseFetch;
import com.backtoend.file_management_system.model.ItemRow;
import com.backtoend.file_management_system.model.ScanLocation;
import com.backtoend.file_management_system.model.TreeOperationResult;
import com.backtoend.file_management_system.util.QueuedTreeOperation;
import java.sql.SQLException;
import java.util.ArrayList;

public class TreeOperationExecutor {
    private final static Database db = MainApplication.getDatabaseInstance();

    public static ArrayList<ScanLocation> getBaseItems(QueuedTreeOperation request) throws SQLException {
        ArrayList<ItemRow> baseItems = db.withConnection(DatabaseFetch::getAllParentItems);
        ArrayList<ScanLocation> scanLocations = new ArrayList<>();
        for (ItemRow item : baseItems) {
            scanLocations.add(db.withConnection(conn ->
                    DatabaseFetch.getScanLocationOf(item, conn)
            ));
        }
        return scanLocations;
    }

    public static TreeOperationResult getAllChildrenOf(QueuedTreeOperation request) throws SQLException {
        int id = request.request().scanLocation().node().id();
        ArrayList<ItemRow> children = db.withConnection(conn ->
                DatabaseFetch.getChildrenOf(id, conn)
        );
        ArrayList<ScanLocation> parentChildren = new ArrayList<>();
        for (ItemRow item : children) {
            if (item.hasChildren()) {
                parentChildren.add(db.withConnection(conn ->
                        DatabaseFetch.getScanLocationOf(item, conn)
                ));
                children.remove(item);
            }
        }
        return new TreeOperationResult(
                request.request().type(),
                children,
                parentChildren
        );
    }

    public static TreeOperationResult getInfoOn(QueuedTreeOperation request) throws SQLException {
        ArrayList<ItemRow> itemRow = new ArrayList<>();
        ArrayList<ScanLocation> scanLocation;
        int id = request.request().scanLocation().node().id();
        ItemRow item = db.withConnection(conn ->
                DatabaseFetch.getInfo(id, conn)
        );
        if (item.hasChildren()) {
            scanLocation = new ArrayList<>();
            scanLocation.add(db.withConnection(conn ->
                    DatabaseFetch.getScanLocationOf(item, conn)
            ));
        } else {
            scanLocation = null;
        }
        itemRow.add(item);
        return new TreeOperationResult(
                request.request().type(),
                itemRow,
                scanLocation
        );
    }
}
