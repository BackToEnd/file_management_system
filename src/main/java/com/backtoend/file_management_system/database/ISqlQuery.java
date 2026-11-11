package com.backtoend.file_management_system.database;

import java.sql.Connection;
import java.sql.SQLException;

/// Functional interface that performs a given database operation through
/// a database connection.
/// @param <T> datatype of the operation performed through this interface
public interface ISqlQuery<T> {
    /// Execute the given database operation and receive a result
    /// @param conn a database connection
    /// @return the result of the performed operation
    /// @throws SQLException if the given operation cannot be completed
    T execute(Connection conn) throws SQLException;
}
