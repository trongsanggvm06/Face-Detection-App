package com.facedetection.ui;

import com.facedetection.model.GalleryEntry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ManagePanel.java – Nhiệm vụ 15 (Khánh – Chính, Ext)
 *
 * UI quản lý gallery.dat:
 *   1. Xem danh sách tất cả người đã đăng ký
 *   2. Xóa một người được chọn
 *   3. Xóa toàn bộ gallery
 *   4. Làm mới danh sách
 *
 * @author Viet Khanh
 */
public class ManagePanel extends JPanel {

    // -----------------------------------------------------------
    // UI
    // -----------------------------------------------------------
    private DefaultTableModel tableModel;
    private JTable            table;
    private JLabel            statusLabel;
    private JButton           btnRefresh, btnDelete, btnClearAll;

    // -----------------------------------------------------------
    // Colors
    // -----------------------------------------------------------
    private static final Color BG      = Color.WHITE;
    private static final Color PANEL_BG = new Color(245, 245, 245);
    private static final Color TEXT    = new Color(33, 37, 41);
    private static final Color PRIMARY = new Color(0, 123, 255);
    private static final Color DANGER  = new Color(220, 53, 69);
    private static final Color SUCCESS = new Color(40, 167, 69);
    private static final Color ORANGE  = new Color(255, 140, 0);

    private static final String GALLERY_FILE = "gallery.dat";

    // -----------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------
    public ManagePanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // ---- Header ----
        JLabel header = new JLabel("QUẢN LÝ GALLERY KHUÔN MẶT");
        header.setFont(new Font("Arial", Font.BOLD, 18));
        header.setForeground(PRIMARY);
        header.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(header, BorderLayout.NORTH);

        // ---- Table ----
        String[] cols = {"STT", "Tên người dùng", "Số chiều embedding"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setRowHeight(26);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.getColumnModel().getColumn(0).setMaxWidth(50);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new TitledBorder("Danh sách người đã đăng ký"));
        add(scrollPane, BorderLayout.CENTER);

        // ---- Bottom panel ----
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 5));
        bottomPanel.setBackground(BG);

        // Status
        statusLabel = new JLabel("Nhấn 'Làm mới' để tải danh sách.");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        statusLabel.setForeground(ORANGE);
        bottomPanel.add(statusLabel, BorderLayout.NORTH);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        btnPanel.setBackground(BG);

        btnRefresh  = makeButton("🔄  Làm mới",  PRIMARY);
        btnDelete   = makeButton("🗑  Xóa mục chọn", DANGER);
        btnClearAll = makeButton("✖  Xóa tất cả", new Color(150, 0, 0));

        btnPanel.add(btnRefresh);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClearAll);
        bottomPanel.add(btnPanel, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);

        // ---- Events ----
        btnRefresh.addActionListener(e  -> refreshTable());
        btnDelete.addActionListener(e   -> deleteSelected());
        btnClearAll.addActionListener(e -> clearAll());

        // Load ngay khi mở
        refreshTable();
    }

    // -----------------------------------------------------------
    // Load gallery.dat → hiển thị vào table
    // -----------------------------------------------------------
    private void refreshTable() {
        tableModel.setRowCount(0);
        List<GalleryEntry> entries = loadGallery();

        if (entries.isEmpty()) {
            statusLabel.setText("Gallery trống – chưa có ai đăng ký.");
            statusLabel.setForeground(ORANGE);
            return;
        }

        for (int i = 0; i < entries.size(); i++) {
            GalleryEntry e = entries.get(i);
            tableModel.addRow(new Object[]{
                i + 1,
                e.getName(),
                e.getEmbedding() != null ? e.getEmbedding().length : 0
            });
        }
        statusLabel.setText("Tổng: " + entries.size() + " người đã đăng ký.");
        statusLabel.setForeground(SUCCESS);
    }

    // -----------------------------------------------------------
    // Xóa mục đang chọn trong table
    // -----------------------------------------------------------
    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một người để xóa!");
            return;
        }

        String name = (String) tableModel.getValueAt(selectedRow, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xóa khuôn mặt của: " + name + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        // Load gallery, xóa entry có tên khớp (lần đầu tiên tìm thấy)
        List<GalleryEntry> entries = loadGallery();
        boolean removed = false;
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getName().equals(name) && !removed) {
                entries.remove(i);
                removed = true;
                break;
            }
        }

        if (removed) {
            saveGallery(entries);
            statusLabel.setText("Đã xóa: " + name);
            statusLabel.setForeground(SUCCESS);
            refreshTable();
        } else {
            statusLabel.setText("Không tìm thấy để xóa.");
            statusLabel.setForeground(DANGER);
        }
    }

    // -----------------------------------------------------------
    // Xóa toàn bộ gallery
    // -----------------------------------------------------------
    private void clearAll() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Gallery đã trống rồi!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Xóa TOÀN BỘ " + tableModel.getRowCount() + " người trong gallery?\nHành động này không thể hoàn tác!",
                "Xác nhận xóa tất cả", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        saveGallery(new ArrayList<>());
        statusLabel.setText("Đã xóa toàn bộ gallery.");
        statusLabel.setForeground(DANGER);
        refreshTable();
    }

    // -----------------------------------------------------------
    // Load gallery.dat
    // -----------------------------------------------------------
    @SuppressWarnings("unchecked")
    private List<GalleryEntry> loadGallery() {
        File file = new File(GALLERY_FILE);
        if (!file.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<GalleryEntry>) ois.readObject();
        } catch (Exception e) {
            System.err.println("[ManagePanel] Lỗi đọc gallery.dat: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // -----------------------------------------------------------
    // Lưu lại gallery.dat
    // -----------------------------------------------------------
    private void saveGallery(List<GalleryEntry> entries) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(GALLERY_FILE))) {
            oos.writeObject(entries);
            System.out.println("[ManagePanel] Đã lưu gallery: " + entries.size() + " người.");
        } catch (IOException e) {
            System.err.println("[ManagePanel] Lỗi ghi gallery.dat: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi lưu gallery: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------
    // Util: tạo button
    // -----------------------------------------------------------
    private JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(170, 38));
        return btn;
    }
}
