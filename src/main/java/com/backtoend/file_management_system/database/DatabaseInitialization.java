package com.backtoend.file_management_system.database;

import com.backtoend.file_management_system.error_handling.FeedbackMsg;
import com.backtoend.file_management_system.error_handling.OperationResult;
import com.backtoend.file_management_system.logging.AppLogger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseInitialization {
    /// Logger instance
    private static final AppLogger logger = AppLogger.getLogger(DatabaseInitialization.class);
    /// `Database` singleton instance
    private final Database db;
    /// A `HashMap` of constraints, mapping table/column to query
    private final HashMap<String, String> constraints;

    // region Preparing all queries for table creation
    /// All table creation queries
    private final String[] tables = {"""
            CREATE TABLE IF NOT EXISTS item (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            path TEXT NOT NULL,
            display_name TEXT,
            file_type TEXT,
            last_modified INTEGER,
            size REAL,
            status TEXT
            );
            """, """
            CREATE TABLE IF NOT EXISTS scan_location (
            item_id INTEGER PRIMARY KEY REFERENCES item(id),
            scan_depth INTEGER,
            update_mode TEXT,
            scan_source TEXT,
            generation INTEGER,
            status TEXT
            );
            """, """
            CREATE TABLE IF NOT EXISTS category (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT
            );
            """, """
            CREATE TABLE IF NOT EXISTS metadata_type (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT,
            allow_multiple INTEGER,
            category INTEGER REFERENCES category (id)
            );
            """, """
            CREATE TABLE IF NOT EXISTS metadata_value (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            type INTEGER REFERENCES metadata_type (id),
            value TEXT,
            UNIQUE (type, value)
            );
            """, """
            CREATE TABLE IF NOT EXISTS item_metadata (
            item_id INTEGER REFERENCES item (id),
            metadata INTEGER REFERENCES metadata_value (id),
            PRIMARY KEY (item_id, metadata)
            );
            """
    };
    // endregion

    /// Constructor. Defines {@link #constraints}.
    /// @param db `Database` singleton instance
    public DatabaseInitialization(Database db) {
        this.db = db;
        this.constraints = new HashMap<>();
        this.constraints.put("item/parent", """
               ALTER TABLE item
               ADD COLUMN parent INTEGER REFERENCES item (id);
               """
        );
    }

    /// Attempts to initialize the database by setting journal mode, creating tables, and
    /// applying constraints/other table alter operations
    /// @param conn Database connection
    /// @return an operation result with status, feedback messages, and an exception if one was thrown
    public OperationResult<Void> initiateDb(Connection conn) {
        ArrayList<FeedbackMsg> feedback = new ArrayList<>();
        if (this.db.getStatus() != Database.DbStatus.INITIALIZING) {
            feedback.add(FeedbackMsg.create(
                "database.initialization.status.illegal",
                    new Object[] { this.db.getStatus() }
            ));
            return OperationResult.failed(
                    null, feedback, new IllegalStateException(
                            MessageFormat.format(
                                    "Invalid database status. Expected: {0}. Got: {1}",
                                    new Object[] { Database.DbStatus.INITIALIZING.toString(), this.db.getStatus().toString() }
                            )
                    )
            );
        }

        // Beginning initialization
        feedback.add(FeedbackMsg.create(
                "database.initialization.status.valid"
        ));
        try (Statement stmt = conn.createStatement()) {
            // Setting journal mode to 'WAL' to allow simultaneous reading and writing
            stmt.execute(
                    "PRAGMA journal_mode = WAL"
            );
            feedback.add(FeedbackMsg.create(
                    "database.initialization.journey.success"
            ));

            // Creating all tables
            for (String table : this.tables) {
                stmt.execute(table);
            }
            logger.info("All tables created or skipped");
            feedback.add(FeedbackMsg.create(
                    "database.initialization.tables.success"
            ));

            // Applying constraints and other table modifications
            for (HashMap.Entry<String, String> set : this.constraints.entrySet()) {
                String[] check = set.getKey().split("/");
                logger.debug(MessageFormat.format(
                        "Attempting to apply column {0} to table {1}",
                        new Object[] { check[0], check[1] }
                        )
                );
                if (!this.doesColumnExist(check[0], check[1], conn)) {
                    stmt.execute(set.getValue());
                }
            }
            feedback.add(FeedbackMsg.create(
                    "database.initialization.constraints.success"
            ));
            return OperationResult.success(
                    null, feedback
            );
        } catch (SQLException e) {
            feedback.add(FeedbackMsg.create(
                    "database.initialization.exception.sql"
            ));
            return OperationResult.failed(
                    null, feedback, e
            );
        }
    }

    // === Private helper methods === \\

    /// Checks if the given table already contains the given column
    /// @param table name of the table
    /// @param column name of the column
    /// @param conn Database connection
    /// @return `true` if the table already contains the column, otherwise `false`
    /// @throws SQLException if the database operation fails
    private boolean doesColumnExist(String table, String column, Connection conn) throws SQLException {
        String query = "PRAGMA table_info(\"" + table.replace("\"", "\"\"") + "\");";
        try (Statement stmt = conn.createStatement();
             ResultSet result = stmt.executeQuery(query)) {
            while(result.next()) {
                if (column.equalsIgnoreCase(result.getString("name"))) {
                    return true;
                }
            }
            return false;
        }
    }
}
