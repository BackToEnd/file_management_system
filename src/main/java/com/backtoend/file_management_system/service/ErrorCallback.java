package com.backtoend.file_management_system.service;

// TODO: Actually implement this thing

/// Functional interface that has not yet been fully implemented.
/// Will eventually be used to handle errors by a dedicated error-handler class
/// @param <T> The return type of the process executed through this interface
/// @author Rogue049
@FunctionalInterface
public interface ErrorCallback<T> {
    /// Error-handling for an operation performed through the caller; still uncertain about
    /// the exact implementation and functionality of this method
    /// @return  The result of the error-handler performing the requested operation
    T execute();
}
