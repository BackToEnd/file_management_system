package com.backtoend.file_management_system.util;

import com.backtoend.file_management_system.error_handling.OperationResult;

import java.sql.Connection;
import java.sql.SQLException;

public class VariousCallbackInterfaces {
    @FunctionalInterface
    public interface IBooleanBiConsumer<T, U>{
        boolean accept(T t, U u);
    }

    @FunctionalInterface
    public interface IResultLogger<T, U, V> {
        void accept(T t, U u, V v);
    }

    @FunctionalInterface
    public interface IErrorCallback {
        void handle(OperationResult<?> result);
    }

    @FunctionalInterface
    public interface ISqlQuery<T> {
        T execute(Connection conn) throws SQLException;
    }
}
