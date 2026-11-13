module com.backtoend.file_management_system {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;
    requires javafx.swing;
    requires java.prefs;
    requires org.controlsfx.controls;
    requires java.sql;
    requires jdk.compiler;
    requires org.jetbrains.annotations;

    exports com.backtoend.file_management_system;
    exports com.backtoend.file_management_system.controller;
    exports com.backtoend.file_management_system.database;
    exports com.backtoend.file_management_system.logging;
    exports com.backtoend.file_management_system.util;

    opens com.backtoend.file_management_system to javafx.fxml;
    opens com.backtoend.file_management_system.controller to javafx.fxml;
    exports com.backtoend.file_management_system.error_handling;
    opens com.backtoend.file_management_system.error_handling to javafx.fxml;
}