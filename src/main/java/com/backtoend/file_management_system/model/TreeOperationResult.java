package com.backtoend.file_management_system.model;

import com.backtoend.file_management_system.enums.TreeOperationType;

import java.util.ArrayList;

public record TreeOperationResult(
        TreeOperationType operation,
        ArrayList<ItemRow> items,
        ArrayList<ScanLocation> locations
) {
}
