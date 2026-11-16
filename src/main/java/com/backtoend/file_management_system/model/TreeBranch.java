package com.backtoend.file_management_system.model;

import java.util.ArrayList;

public record TreeBranch(
        IItemInformation parent, ArrayList<IItemInformation> children
) {
}
