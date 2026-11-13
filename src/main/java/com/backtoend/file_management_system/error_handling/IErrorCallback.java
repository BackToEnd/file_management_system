package com.backtoend.file_management_system.error_handling;

@FunctionalInterface
public interface IErrorCallback {
    void handle(OperationResult<?> result);
}
