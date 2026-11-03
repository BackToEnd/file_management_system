package com.backtoend.file_management_system.exceptions;

/// Custom exception class used to handle broader exceptions caught by
/// helper functions in order to properly escalate them to the appropriate caller
/// and error-handling system
/// @author Rogue049
public class OperationException extends Exception{
    public OperationException(String msg) {
        super(msg);
    }
    public OperationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
