package com.backtoend.file_management_system.tree;

import com.backtoend.file_management_system.model.ItemRow;
import com.backtoend.file_management_system.model.ScanLocation;

import java.util.ArrayList;

public record TreeData(
        ItemRow item,
        ScanLocation scanLocation,
        ArrayList<TreeData> children,
        boolean childrenLoaded
) {
}
