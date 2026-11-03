package com.backtoend.file_management_system.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import com.backtoend.file_management_system.MainApplication;
import com.backtoend.file_management_system.enums.DbStatus;
import com.backtoend.file_management_system.enums.OperationStatus;
import com.backtoend.file_management_system.exceptions.OperationException;
import com.backtoend.file_management_system.logging.AppLogger;
import com.backtoend.file_management_system.util.FeedbackMsg;
import com.backtoend.file_management_system.util.OperationResult;

/// Database helper class that initializes the database by creating
/// all required tables and adding all constraint columns
/// @author Rogue049
public class DatabaseInitializer {
    private final Database db;
    private static final AppLogger logger = AppLogger.getLogger(DatabaseInitializer.class);
    private final HashMap<String, String> constraints;

    // region Variables relevant for DB initialization and table creation
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
    // endregion

    // Constructor
    public DatabaseInitializer() {
        this.db = MainApplication.getDatabaseInstance();
        this.constraints = new HashMap<>();
        this.constraints.put("node/parent", """
                ALTER TABLE node
                ADD COLUMN parent INTEGER REFERENCES node (id)
                """);
    }

    // === Utility Functions === \\

    // TODO: Should this method be called with `db.withConnection()`?
    /// Begins the DB initialization process by first attempting to create all tables,
    /// skipping any tables that already exist, then attempting to add constraint columns\
    /// Deals with any resulting SQLExceptions itself.\
    /// If the initialization is successful, it will set `Database`'s initialization status to
    /// `READY`
    /// @return An operation result with status and messages. Status will be `SUCCESS` if the
    /// initialization goes through without any problems, and `FAILED` if an SQLException is thrown
    public OperationResult<Void> initiateDB(Connection conn) throws SQLException{
        // Checking if the DB already has something going on
        if (this.db.getStatus() != DbStatus.NOT_INITIALIZED) {
            throw new IllegalStateException(
                    "Database initialization could not complete due to"
                    + "an invalid database status. Expected: "
                    + DbStatus.NOT_INITIALIZED.toString()
                    + "Got: " + this.db.getStatus().toString()
            );
        }

        // Beginning initialization
        this.db.setStatus(DbStatus.INITIALIZING);
        logger.info("Attempting to initialize DB");
        ArrayList<FeedbackMsg> feedback = new ArrayList<>();

        // Establishing a connection to the DB
        try (Statement stmt = conn.createStatement()) {

            // Creating all tables that do not yet exist
            for (String table : this.tables) {
                stmt.execute(table);
            }
            feedback.add(FeedbackMsg.create(
                    "db.init.tables.create.all.success",
                    new Object[] {}
            ));
            logger.info("All tables either created or skipped");

            // Applying constraints
            for (HashMap.Entry<String, String> set : this.constraints.entrySet()) {
                String[] check = set.getKey().split("/");
                logger.debug("Attempting to apply constraint(s) to table {" + check[0] + "}, column {" + check[1] + "}");
                if (!DatabaseCheck.columnExists(check[0], check[1], conn)) {
                    logger.info("Applying missing column {" + check[1] + "} to table {" + check[0] + "}");
                    stmt.execute(set.getValue());
                    feedback.add(FeedbackMsg.create(
                            "db.init.tables.alter.success",
                            new Object[] { check[0], check[1] }
                    ));
                }
            }
            return OperationResult.create(OperationStatus.SUCCESS, null, feedback);
        }
    }
}
