package org.example.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
/**
 * Reads DB connection settings from configs/config.properties and
 * provides connections. Also initialises the schema on first use.
 */
public class DatabaseConfig {

    private static final Logger logger = LogManager.getLogger(DatabaseConfig.class);
    private final String dbUrl;

    public DatabaseConfig() {
        this.dbUrl = loadUrl();
        logger.info("DatabaseConfig initialised → {}", dbUrl);
        initSchema();
    }

    private String loadUrl() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in != null) {
                props.load(in);
                logger.debug("Loaded config.properties");
            } else {
                logger.warn("config.properties not found, using default SQLite path");
            }
        } catch (IOException e) {
            logger.error("Could not read config.properties: {}", e.getMessage(), e);
        }
        return props.getProperty("db.url", "jdbc:sqlite:basketball.db");
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    private void initSchema() {
        logger.info("Initialising database schema...");
        String[] ddl = {
                """
            CREATE TABLE IF NOT EXISTS cashiers (
                id       INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                fullName TEXT NOT NULL
            )
            """,
                """
            CREATE TABLE IF NOT EXISTS matches (
                id             INTEGER PRIMARY KEY AUTOINCREMENT,
                name           TEXT    NOT NULL,
                ticketPrice    REAL    NOT NULL,
                totalSeats     INTEGER NOT NULL,
                availableSeats INTEGER NOT NULL
            )
            """,
                """
            CREATE TABLE IF NOT EXISTS customers (
                id      INTEGER PRIMARY KEY AUTOINCREMENT,
                name    TEXT NOT NULL,
                address TEXT NOT NULL
            )
            """,
                """
            CREATE TABLE IF NOT EXISTS tickets (
                id            INTEGER PRIMARY KEY AUTOINCREMENT,
                customerId    INTEGER NOT NULL REFERENCES customers(id),
                matchId       INTEGER NOT NULL REFERENCES matches(id),
                numberOfSeats INTEGER NOT NULL
            )
            """
        };

        try (Connection conn = getConnection();
             var stmt = conn.createStatement()) {
            for (String sql : ddl) stmt.execute(sql);
            logger.info("Schema ready.");
        } catch (SQLException e) {
            logger.error("Schema init failed: {}", e.getMessage(), e);
            throw new RuntimeException("Cannot initialise DB schema", e);
        }
    }
}