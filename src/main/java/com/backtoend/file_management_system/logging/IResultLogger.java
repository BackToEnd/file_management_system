package com.backtoend.file_management_system.logging;

@FunctionalInterface
public interface IResultLogger<T, U, V> {
    void accept(T t, U u, V v);
}
