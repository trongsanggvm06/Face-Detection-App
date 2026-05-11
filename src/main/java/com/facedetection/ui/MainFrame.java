package com.facedetection.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * MainFrame.java – Cửa sổ chính của ứng dụng
 * Dùng JTabbedPane để chứa 3 panel:
 *   Tab 1: Đăng ký khuôn mặt  (RegisterPanel)
 *   Tab 2: Nhận diện realtime  (RecognizePanel)
 *   Tab 3: Quản lý gallery     (ManagePanel)
 */
public class MainFrame extends JFrame {

    private RegisterPanel  registerPanel;
    private RecognizePanel recognizePanel;
    private ManagePanel    managePanel;
    private JTabbedPane    tabbedPane;

    public MainFrame() {
        setTitle("Face Detection App – BTL Java");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Khởi tạo các panel
        registerPanel  = new RegisterPanel();
        recognizePanel = new RecognizePanel();
        managePanel    = new ManagePanel();

        // TabbedPane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        tabbedPane.addTab("📷  Đăng ký",    registerPanel);
        tabbedPane.addTab("🔍  Nhận diện",  recognizePanel);
        tabbedPane.addTab("🗂  Quản lý",    managePanel);

        // Khi chuyển tab: dừng camera tab cũ, cập nhật gallery nếu cần
        tabbedPane.addChangeListener(e -> {
            int idx = tabbedPane.getSelectedIndex();
            // Tab Quản lý: tự reload lại bảng
            if (idx == 2) {
                managePanel.refreshTable();
            }
        });

        add(tabbedPane);

        // Khi đóng app: dừng toàn bộ camera + đóng DB
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                registerPanel.stopCamera();
                recognizePanel.stopCamera();
            }
        });
    }
}