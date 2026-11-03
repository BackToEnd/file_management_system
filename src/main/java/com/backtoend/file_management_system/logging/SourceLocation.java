package com.backtoend.file_management_system.logging;

/// Record class keeping track of source locations; relevant for logging, mostly
/// @param sourceClass Name of the class that did something
/// @param sourceMethod Name of a method that did something
public record SourceLocation(String sourceClass, String sourceMethod) {
    public String getEffectiveClassName() {
        if (sourceClass.contains(".")) {
            return sourceClass.substring(sourceClass.lastIndexOf('.') + 1);
        }
        return sourceClass;
    }
}
