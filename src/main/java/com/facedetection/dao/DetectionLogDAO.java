package com.facedetection.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DetectionLogDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

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
            System.out.println("[DAO] Log saved: person=" + personName
                    + " | score=" + String.format("%.4f", score));
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[DAO] Error insertLog: " + e.getMessage());
            return false;
        }
    }

    public boolean insertLog(String personName, double score) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return insertLog(personName, now, score);
    }

    public List<Object[]> getAllLogs() {
        List<Object[]> list = new ArrayList<>();
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
            System.out.println("[DAO] Retrieved " + list.size() + " log records.");
        } catch (SQLException e) {
            System.err.println("[DAO] Error getAllLogs: " + e.getMessage());
        }
        return list;
    }

    public List<Object[]> getLogsByPerson(String personName) {
        List<Object[]> list = new ArrayList<>();
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
            System.err.println("[DAO] Error getLogsByPerson: " + e.getMessage());
        }
        return list;
    }

    public static void main(String[] args) {
        System.out.println("=== DetectionLogDAO Test ===");
        DetectionLogDAO dao = new DetectionLogDAO();

        boolean ok1 = dao.insertLog("Nguyen Van A", 0.9231);
        boolean ok2 = dao.insertLog("Unknown",      0.4500);
        boolean ok3 = dao.insertLog("Nguyen Van A", 0.8875);
        System.out.println("Insert 1: " + (ok1 ? "OK" : "FAIL"));
        System.out.println("Insert 2: " + (ok2 ? "OK" : "FAIL"));
        System.out.println("Insert 3: " + (ok3 ? "OK" : "FAIL"));

        System.out.println("\n--- All logs ---");
        List<Object[]> logs = dao.getAllLogs();
        for (Object[] row : logs) {
            System.out.printf("  [%d] %-20s | %s | score=%.4f%n",
                    (int) row[0], (String) row[1], row[2].toString(), (double) row[3]);
        }

        System.out.println("\n--- Logs for 'Nguyen Van A' ---");
        List<Object[]> filtered = dao.getLogsByPerson("Nguyen Van A");
        for (Object[] row : filtered) {
            System.out.printf("  [%d] %s | score=%.4f%n",
                    (int) row[0], row[2].toString(), (double) row[3]);
        }

        DatabaseConnection.getInstance().closeConnection();
        System.out.println("=== Test complete ===");
    }
}
