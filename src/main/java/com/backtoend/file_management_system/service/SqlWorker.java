package com.backtoend.file_management_system.service;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface SqlWorker<T> {
    T execute(Connection conn) throws SQLException;
}
