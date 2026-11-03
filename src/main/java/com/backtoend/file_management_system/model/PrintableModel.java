package com.backtoend.file_management_system.model;

/// Interface providing unified methods to {@link ItemRow}
/// and {@link ScanLocation}
/// @author Rogue049
public interface PrintableModel {
    /// Transforms an instance of a class implementing this interface
    /// into a printable String with custom formatting
    /// @return Printable, formatted String
    String display();
}
