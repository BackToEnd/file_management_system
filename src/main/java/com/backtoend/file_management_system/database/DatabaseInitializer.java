package com.backtoend.file_management_system.database;

import java.sql.*;
import java.util.HashMap;

import com.backtoend.file_management_system.Context;
import com.backtoend.file_management_system.logging.AppLogger;

public class DatabaseInitializer {
    private final Database db;
    private static final AppLogger logger = AppLogger.getLogger(DatabaseInitializer.class);

    // Variables relevant for DB initialization and table creation
    private final String[] tables = {
            """
            CREATE TABLE IF NOT EXISTS node (
                    id INTEGER PRIMARY KEY,
                    name TEXT NOT NULL,
                    path TEXT NOT NULL,
                    display_name TEXT,
                    file_type TEXT,
                    last_modified TEXT,
                    size REAL,
                    status TEXT
                    );
            """, """
            CREATE TABLE IF NOT EXISTS scan_location (
            node_id INTEGER PRIMARY KEY,
            scan_depth INTEGER,
            update_mode TEXT,
            scan_source TEXT,
            generation INTEGER,
            status TEXT
            );
            """, """
            CREATE TABLE IF NOT EXISTS category (
            id INTEGER PRIMARY KEY,
            name TEXT
            );
            """, """
            CREATE TABLE IF NOT EXISTS metadata_type (
            id INTEGER PRIMARY KEY,
            name TEXT,
            allow_multiple INTEGER,
            category INTEGER REFERENCES category (id)
            );
            """, """
            CREATE TABLE IF NOT EXISTS metadata_value (
            id INTEGER PRIMARY KEY,
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
    private HashMap<String, String> constraints = new HashMap<>();


    public DatabaseInitializer(Context context) {
        logger.debug("Instance created");
        this.db = context.db();
        this.constraints.put("node/parent", """
                ALTER TABLE node
                ADD COLUMN parent INTEGER REFERENCES node (id)
                """);
    }

    // === Utility Functions === \\

    /**
     * Creates all tables and adds all constraints
     * @return whether the DB could be initialized
     */
    public boolean initiateDB() {
        logger.info("Attempting to initialize DB");
        try (Connection conn = this.db.connect()) {
            logger.debug("Connection established");
            Statement stmt = conn.createStatement();
            for (String table : this.tables) {
                stmt.execute(table);
            }
            logger.info("All tables either created or skipped");
            for (HashMap.Entry<String, String> set : this.constraints.entrySet()) {
                String[] check = set.getKey().split("/");
                logger.debug("Attempting to apply constraint(s) to table {" + check[0] + "}, column {" + check[1] + "}");
                if (!this.db.columnExists(check[0], check[1])) {
                    logger.info("Applying missing column {" + check[1] + "} to table {" + check[0] + "}");
                    stmt.execute(set.getValue());
                }
            }
            return true;
        } catch (SQLException e) {
            logger.info("Unable to initialize DB:");
            e.printStackTrace();
            return false;
        }
    }
}
