package server.dao;

import java.sql.*;

public class DBConnection {
    private static Connection conn;

    public static synchronized Connection getConnection() throws SQLException {
        if (conn == null) {
            conn = DriverManager.getConnection("jdbc:sqlite:todo.db");
            try (Statement s = conn.createStatement()) {
                s.execute("PRAGMA foreign_keys = ON");
            }
            initSchema();
        }
        return conn;
    }

    private static void initSchema() throws SQLException {
        try (Statement s = conn.createStatement()) {
            s.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE,
                    password_hash TEXT,
                    role TEXT DEFAULT 'user'
                );
            """);
            s.execute("""
                CREATE TABLE IF NOT EXISTS tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    text TEXT,
                    done INTEGER DEFAULT 0,
                    priority TEXT,
                    category TEXT,
                    created_at TEXT,
                    updated_at TEXT,
                    FOREIGN KEY(user_id) REFERENCES users(id)
                );
            """);
        }
    }
}
