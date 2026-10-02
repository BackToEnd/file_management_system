package com.backtoend.file_management_system.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.nio.file.Path;

public class DatabaseInitializer {
    public static void main(String[] args) {
        // Identifying the path to the .db file;
        // This will be replaced with an Appdata location later
        Path dbPath = Path.of("data", "filesystem.db");
        String url = "jdbc:sqlite:" + dbPath;

        // Attempting to connect to the DB
        try (Connection con = DriverManager.getConnection(url)) {
            System.out.println("Connection to DB was established");
        } catch(SQLException e) {
            System.out.println("Error connecting to the DB:");
            // Unsophisticated error logging method due to lack of experience :I
            e.printStackTrace();
        }
    }
}
