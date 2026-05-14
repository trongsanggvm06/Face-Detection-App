package com.facedetection.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String DB_HOST     = "localhost";
    private static final String DB_PORT     = "3306";
    private static final String DB_NAME     = "detection_log";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "";

    private static final String DB_URL =
            "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=Asia/Ho_Chi_Minh"
            + "&characterEncoding=UTF-8";

    private static DatabaseConnection instance  = null;
    private        Connection          connection = null;

    private DatabaseConnection() {}

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed() || !connection.isValid(2)) {
            System.out.println("[DB] Connecting to: " + DB_URL);
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("[DB] Connected to database: " + DB_NAME);
            createTableIfNotExists();
        }
        return connection;
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS detection_log ("
                   + "  id          INT AUTO_INCREMENT PRIMARY KEY, "
                   + "  person_name VARCHAR(255) NOT NULL, "
                   + "  timestamp   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                   + "  score       DOUBLE       NOT NULL DEFAULT 0.0 "
                   + ")";
        try {
            Statement stmt = connection.createStatement();
            stmt.execute(sql);
            stmt.close();
            System.out.println("[DB] Table detection_log is ready.");
        } catch (SQLException e) {
            System.err.println("[DB] Error creating table: " + e.getMessage());
        }
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("[DB] Connection closed.");
                }
            } catch (SQLException e) {
                System.err.println("[DB] Error closing connection: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }

    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed() && conn.isValid(3);
        } catch (SQLException e) {
            System.err.println("[DB] Connection test failed: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== DatabaseConnection Test ===");
        DatabaseConnection db = DatabaseConnection.getInstance();
        if (db.testConnection()) {
            System.out.println(">>> CONNECTION OK - Table detection_log ready.");
        } else {
            System.out.println(">>> CONNECTION FAILED - Check XAMPP and database.");
        }
        db.closeConnection();
    }
}
