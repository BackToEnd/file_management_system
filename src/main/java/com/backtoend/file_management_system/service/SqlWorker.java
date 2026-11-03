package com.backtoend.file_management_system.service;

import java.sql.Connection;
import java.sql.SQLException;

///Functional interface for handling database operations
/// @param <T> Generic datatype returned by the requested operation
/// @author Rogue049
@FunctionalInterface
public interface SqlWorker<T> {
    /// Performs a database operation using the provided connection
    /// @param conn Database connection
    /// @return The result of the database operation
    /// @throws SQLException If the database operation fails
    T execute(Connection conn) throws SQLException;
}
