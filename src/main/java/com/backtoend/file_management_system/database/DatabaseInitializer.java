package com.backtoend.file_management_system.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

import com.backtoend.file_management_system.Context;
import com.backtoend.file_management_system.enums.OperationStatus;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.util.FeedbackMsg;
import com.backtoend.file_management_system.util.OperationResult;

/**
 * Database helper class that initializes the DB by creating all required tables and applying all required restraints
 */
public class DatabaseInitializer {
    private final Context context;
    private static final AppLogger logger = AppLogger.getLogger(DatabaseInitializer.class);

    // Variables relevant for DB initialization and table creation
    private final String[] tables = {
            """
            CREATE TABLE IF NOT EXISTS node (
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
            node_id INTEGER PRIMARY KEY REFERENCES node(id),
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
            CREATE TABLE IF NOT EXISTS node_metadata (
            node_id INTEGER REFERENCES node (id),
            metadata INTEGER REFERENCES metadata_value (id),
            PRIMARY KEY (node_id, metadata)
            );
            """
    };
    private final HashMap<String, String> constraints;

    // Constructor
    public DatabaseInitializer(Context context) {
        this.constraints = new HashMap<>();
        this.context = context;
        this.constraints.put("node/parent", """
                ALTER TABLE node
                ADD COLUMN parent INTEGER REFERENCES node (id)
                """);
    }

    // === Utility Functions === \\

    /**
     * Initializes the database by creating all required tables (if they do not exist yet)
     * and adding all columns created at a later date (allows upgrading app version without
     * wiping your DB!)
     * @return Operation status and status messages
     */
    public OperationResult<Void> initiateDB() {
        logger.info("Attempting to initialize DB");
        ArrayList<FeedbackMsg> feedback = new ArrayList<>();

        // Establishing a connection to the DB
        try (Connection conn = this.context.db().connect();
             Statement stmt = conn.createStatement();
             ) {
            logger.debug("Connection established");

            // Creating all tables that do not yet exist
            for (String table : this.tables) {
                stmt.execute(table);
            }
            feedback.add(new FeedbackMsg(
                    this.getClass().getName() , "db.init.tables.create.all.success", new Object[] {}
            ));
            logger.info("All tables either created or skipped");

            // Applying constraints
            for (HashMap.Entry<String, String> set : this.constraints.entrySet()) {
                String[] check = set.getKey().split("/");
                logger.debug("Attempting to apply constraint(s) to table {" + check[0] + "}, column {" + check[1] + "}");
                if (!DatabaseCheck.columnExists(check[0], check[1], this.context.db())) {
                    logger.info("Applying missing column {" + check[1] + "} to table {" + check[0] + "}");
                    stmt.execute(set.getValue());
                    feedback.add(new FeedbackMsg(
                            this.getClass().getName(), "db.init.tables.alter.success",
                            new Object[] { check[0], check[1] }
                    ));
                }
            }

            return new OperationResult<>(OperationStatus.SUCCESS, null, feedback);
        } catch (SQLException e) {
            logger.error("Unable to initialize DB: ", e);
            feedback.add(new FeedbackMsg(
                    this.getClass().getName(), "db.init.tables.failed", new Object[] {}
            ));
            return new OperationResult<>(OperationStatus.FAILED, null, feedback);
        }
    }
}
