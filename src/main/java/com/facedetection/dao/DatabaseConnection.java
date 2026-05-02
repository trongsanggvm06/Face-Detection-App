package com.facedetection.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DatabaseConnection.java – Mục 6 (Khánh)
 *
 * Chức năng:
 *   1. Kết nối MySQL qua JDBC (Singleton – chỉ 1 connection duy nhất).
 *   2. Tự động tạo bảng detection_log khi kết nối lần đầu.
 *
 * Cấu trúc bảng detection_log:
 *   id          INT  AUTO_INCREMENT PRIMARY KEY
 *   person_name VARCHAR(255)   -- tên người được nhận diện
 *   timestamp   DATETIME       -- thời điểm nhận diện
 *   score       DOUBLE         -- độ tin cậy (confidence score, 0.0 – 1.0)
 *
 * @author Viết Khánh
 */
public class DatabaseConnection {

    // -------------------------------------------------------
    // Thông tin kết nối – chỉnh sửa khi cần
    // -------------------------------------------------------
    private static final String DB_HOST     = "localhost";
    private static final String DB_PORT     = "3306";
    private static final String DB_NAME     = "detection_log";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "";          // XAMPP mặc định để trống

    private static final String DB_URL =
            "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=Asia/Ho_Chi_Minh"
            + "&characterEncoding=UTF-8";

    // -------------------------------------------------------
    // Singleton
    // -------------------------------------------------------
    private static DatabaseConnection instance = null;
    private Connection connection = null;

    // Constructor private
    private DatabaseConnection() {}

    /**
     * Trả về instance Singleton (thread-safe).
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    // -------------------------------------------------------
    // Lấy kết nối
    // -------------------------------------------------------
    /**
     * Trả về Connection đang hoạt động.
     * Tự động tạo kết nối mới nếu chưa mở hoặc đã bị đóng.
     * Sau khi kết nối, tự động tạo bảng detection_log nếu chưa tồn tại.
     *
     * @return Connection tới CSDL
     * @throws SQLException nếu không thể kết nối
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed() || !connection.isValid(2)) {
            System.out.println("[DB] Đang kết nối tới: " + DB_URL);
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("[DB] Kết nối thành công tới database: " + DB_NAME);
            createTableIfNotExists();   // Tạo bảng nếu chưa có
        }
        return connection;
    }

    // -------------------------------------------------------
    // Tự động tạo bảng detection_log
    // -------------------------------------------------------
    /**
     * Tạo bảng detection_log nếu chưa tồn tại trong database.
     * Cột: id, person_name, timestamp, score.
     */
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
            System.out.println("[DB] Bảng detection_log đã sẵn sàng.");
        } catch (SQLException e) {
            System.err.println("[DB] Lỗi tạo bảng: " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // Đóng kết nối
    // -------------------------------------------------------
    /**
     * Đóng kết nối CSDL. Gọi khi ứng dụng tắt.
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("[DB] Đã đóng kết nối CSDL.");
                }
            } catch (SQLException e) {
                System.err.println("[DB] Lỗi đóng kết nối: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }

    // -------------------------------------------------------
    // Kiểm tra nhanh
    // -------------------------------------------------------
    /**
     * Kiểm tra kết nối có hoạt động không.
     *
     * @return true nếu kết nối OK
     */
    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed() && conn.isValid(3);
        } catch (SQLException e) {
            System.err.println("[DB] Test kết nối thất bại: " + e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------
    // main() – test độc lập
    // -------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== Kiểm tra DatabaseConnection ===");
        DatabaseConnection db = DatabaseConnection.getInstance();
        if (db.testConnection()) {
            System.out.println(">>> KẾT NỐI THÀNH CÔNG! Bảng detection_log sẵn sàng.");
        } else {
            System.out.println(">>> KẾT NỐI THẤT BẠI. Kiểm tra lại XAMPP và database.");
        }
        db.closeConnection();
    }
}
