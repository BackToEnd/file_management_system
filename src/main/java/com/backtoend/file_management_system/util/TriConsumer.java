package com.backtoend.file_management_system.util;

/// Functional interface for tri-consumer callbacks
/// @param <T> Datatype of the first parameter
/// @param <U> Datatype of the second parameter
/// @param <V> Datatype of the third parameter
@FunctionalInterface
public interface TriConsumer<T, U, V> {
    /// Performs any provided operation using the given parameters
    /// @param t Parameter 1
    /// @param u Parameter 2
    /// @param v Parameter 3
    void accept (T t, U u, V v);
}
