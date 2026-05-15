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

public class ManagePanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable            table;
    private JLabel            statusLabel;
    private JButton           btnRefresh, btnDelete, btnClearAll;

    private static final Color BG      = Color.WHITE;
    private static final Color PANEL_BG = new Color(245, 245, 245);
    private static final Color TEXT    = new Color(33, 37, 41);
    private static final Color PRIMARY = new Color(0, 123, 255);
    private static final Color DANGER  = new Color(220, 53, 69);
    private static final Color SUCCESS = new Color(40, 167, 69);
    private static final Color ORANGE  = new Color(255, 140, 0);

    private static final String GALLERY_FILE = "gallery.dat";

    public ManagePanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel header = new JLabel("QUẢN LÝ GALLERY KHUÔN MẶT");
        header.setFont(new Font("Arial", Font.BOLD, 18));
        header.setForeground(PRIMARY);
        header.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(header, BorderLayout.NORTH);


        String[] cols = {"STT", "Tên người dùng", "Trạng thái"};
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


        JPanel bottomPanel = new JPanel(new BorderLayout(10, 5));
        bottomPanel.setBackground(BG);

        statusLabel = new JLabel("Nhấn 'Làm mới' để tải danh sách.");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        statusLabel.setForeground(ORANGE);
        bottomPanel.add(statusLabel, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        btnPanel.setBackground(BG);

        btnRefresh  = makeButton(" Làm mới",  PRIMARY);
        btnDelete   = makeButton("  Xoá mục chọn", DANGER);
        btnClearAll = makeButton("  Xoá tất cả", new Color(150, 0, 0));

        btnPanel.add(btnRefresh);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClearAll);
        bottomPanel.add(btnPanel, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e  -> refreshTable());
        btnDelete.addActionListener(e   -> deleteSelected());
        btnClearAll.addActionListener(e -> clearAll());

        refreshTable();
    }

    public void refreshTable() {
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
                e.getEmbedding() != null ? "Đã đăng ký" : "Lỗi"
            });
        }
        statusLabel.setText("Tổng: " + entries.size() + " người đã đăng ký.");
        statusLabel.setForeground(SUCCESS);
    }

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một người để xoá!");
            return;
        }

        String name = (String) tableModel.getValueAt(selectedRow, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xoá khuôn mặt của: " + name + "?",
                "Xác nhận xoá", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

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
            statusLabel.setText("Đã xoá: " + name);
            statusLabel.setForeground(SUCCESS);
            refreshTable();
        } else {
            statusLabel.setText("Không tìm thấy để xoá.");
            statusLabel.setForeground(DANGER);
        }
    }

    private void clearAll() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Gallery đã trống rồi!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Xoá TOÀN BỘ " + tableModel.getRowCount() + " người trong gallery?",
                "Xác nhận xoá tất cả", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        saveGallery(new ArrayList<>());
        statusLabel.setText("Đã xoá toàn bộ gallery.");
        statusLabel.setForeground(DANGER);
        refreshTable();
    }

    @SuppressWarnings("unchecked")
    private List<GalleryEntry> loadGallery() {
        File file = new File(GALLERY_FILE);
        if (!file.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<GalleryEntry>) ois.readObject();
        } catch (Exception e) {
            System.err.println("[ManagePanel] Error reading gallery.dat: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void saveGallery(List<GalleryEntry> entries) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(GALLERY_FILE))) {
            oos.writeObject(entries);
            System.out.println("[ManagePanel] Gallery saved: " + entries.size() + " entries.");
        } catch (IOException e) {
            System.err.println("[ManagePanel] Error writing gallery.dat: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi lưu gallery: " + e.getMessage());
        }
    }

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
