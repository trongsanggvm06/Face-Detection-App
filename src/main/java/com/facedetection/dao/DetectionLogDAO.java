package com.facedetection.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DetectionLogDAO.java – Mục 7 (Khánh)
 *
 * Chức năng:
 *   1. INSERT log mỗi lần nhận diện khuôn mặt (person_name, timestamp, score).
 *   2. SELECT đơn giản để xem lịch sử nhận diện.
 *
 * Sử dụng bảng detection_log (tạo tự động bởi DatabaseConnection):
 *   id          INT           PRIMARY KEY AUTO_INCREMENT
 *   person_name VARCHAR(255)  -- tên người được nhận diện
 *   timestamp   DATETIME      -- thời điểm nhận diện
 *   score       DOUBLE        -- độ tin cậy (0.0 – 1.0)
 *
 * @author Viết Khánh
 */
public class DetectionLogDAO {

    // Lấy connection từ Singleton
    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ===================================================================
    // INSERT – Ghi log mỗi lần nhận diện
    // ===================================================================
    /**
     * Lưu một lần nhận diện khuôn mặt vào CSDL.
     *
     * @param personName Tên người được nhận diện (dùng "Unknown" nếu không xác định)
     * @param timestamp  Thời điểm nhận diện
     * @param score      Độ tin cậy của kết quả nhận diện (0.0 – 1.0)
     * @return {@code true} nếu insert thành công
     */
    public boolean insertLog(String personName, Timestamp timestamp, double score) {
        String sql = "INSERT INTO detection_log (person_name, timestamp, score) "
                   + "VALUES (?, ?, ?)";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            stmt.setString(1, personName);
            stmt.setTimestamp(2, timestamp);
            stmt.setDouble(3, score);

            int rows = stmt.executeUpdate();
            stmt.close();

            System.out.println("[DAO] Đã ghi log: person=" + personName
                    + " | score=" + String.format("%.4f", score));
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("[DAO] Lỗi insertLog: " + e.getMessage());
            return false;
        }
    }

    /**
     * Overload tiện dụng – tự động lấy thời điểm hiện tại làm timestamp.
     *
     * @param personName Tên người được nhận diện
     * @param score      Độ tin cậy (0.0 – 1.0)
     * @return {@code true} nếu insert thành công
     */
    public boolean insertLog(String personName, double score) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return insertLog(personName, now, score);
    }

    // ===================================================================
    // SELECT – Xem toàn bộ lịch sử nhận diện
    // ===================================================================
    /**
     * Lấy toàn bộ lịch sử nhận diện, sắp xếp mới nhất trước.
     *
     * @return Danh sách các mảng Object[]{id, person_name, timestamp, score}
     */
    public List<Object[]> getAllLogs() {
        List<Object[]> list = new ArrayList<Object[]>();
        String sql = "SELECT id, person_name, timestamp, score "
                   + "FROM detection_log "
                   + "ORDER BY timestamp DESC";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[4];
                row[0] = rs.getInt("id");
                row[1] = rs.getString("person_name");
                row[2] = rs.getTimestamp("timestamp");
                row[3] = rs.getDouble("score");
                list.add(row);
            }

            rs.close();
            stmt.close();
            System.out.println("[DAO] Lấy được " + list.size() + " bản ghi log.");

        } catch (SQLException e) {
            System.err.println("[DAO] Lỗi getAllLogs: " + e.getMessage());
        }
        return list;
    }

    // ===================================================================
    // SELECT – Lọc theo tên người
    // ===================================================================
    /**
     * Lấy lịch sử nhận diện của một người cụ thể.
     *
     * @param personName Tên cần lọc
     * @return Danh sách các mảng Object[]{id, person_name, timestamp, score}
     */
    public List<Object[]> getLogsByPerson(String personName) {
        List<Object[]> list = new ArrayList<Object[]>();
        String sql = "SELECT id, person_name, timestamp, score "
                   + "FROM detection_log "
                   + "WHERE person_name = ? "
                   + "ORDER BY timestamp DESC";
        try {
            PreparedStatement stmt = getConn().prepareStatement(sql);
            stmt.setString(1, personName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[4];
                row[0] = rs.getInt("id");
                row[1] = rs.getString("person_name");
                row[2] = rs.getTimestamp("timestamp");
                row[3] = rs.getDouble("score");
                list.add(row);
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("[DAO] Lỗi getLogsByPerson: " + e.getMessage());
        }
        return list;
    }

    // ===================================================================
    // main() – test INSERT + SELECT
    // ===================================================================
    public static void main(String[] args) {
        System.out.println("=== Kiểm tra DetectionLogDAO ===");

        DetectionLogDAO dao = new DetectionLogDAO();

        // Test INSERT – ghi log nhận diện
        boolean ok1 = dao.insertLog("Nguyen Van A", 0.9231);
        boolean ok2 = dao.insertLog("Unknown",      0.4500);
        boolean ok3 = dao.insertLog("Nguyen Van A", 0.8875);
        System.out.println("Insert lần 1: " + (ok1 ? "OK" : "FAIL"));
        System.out.println("Insert lần 2: " + (ok2 ? "OK" : "FAIL"));
        System.out.println("Insert lần 3: " + (ok3 ? "OK" : "FAIL"));

        System.out.println();

        // Test SELECT – xem toàn bộ lịch sử
        System.out.println("--- Toàn bộ lịch sử nhận diện ---");
        List<Object[]> logs = dao.getAllLogs();
        for (Object[] row : logs) {
            System.out.printf("  [%d] %-20s | %s | score=%.4f%n",
                    (int) row[0],
                    (String) row[1],
                    row[2].toString(),
                    (double) row[3]);
        }

        System.out.println();

        // Test SELECT – lọc theo tên
        System.out.println("--- Lịch sử của 'Nguyen Van A' ---");
        List<Object[]> filtered = dao.getLogsByPerson("Nguyen Van A");
        for (Object[] row : filtered) {
            System.out.printf("  [%d] %s | score=%.4f%n",
                    (int) row[0],
                    row[2].toString(),
                    (double) row[3]);
        }

        // Đóng kết nối
        DatabaseConnection.getInstance().closeConnection();
        System.out.println("=== Kết thúc test ===");
    }
}
