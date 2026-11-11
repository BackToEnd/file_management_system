package com.backtoend.file_management_system.util;

import com.backtoend.file_management_system.enums.TreeOperationPriority;
import com.backtoend.file_management_system.enums.TreeOperationScope;
import com.backtoend.file_management_system.enums.TreeOperationType;
import com.backtoend.file_management_system.model.ScanLocation;

public record TreeOperationRequest(
        TreeOperationPriority operation,
        TreeOperationType type,
        TreeOperationScope scope,
        ScanLocation scanLocation
) {
}
