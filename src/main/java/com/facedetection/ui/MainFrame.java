package com.facedetection.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * MainFrame.java – Cửa sổ chính của ứng dụng
 * Dùng JTabbedPane với 3 tab:
 *   Tab 0: Đăng ký khuôn mặt  (RegisterPanel)
 *   Tab 1: Nhận diện realtime  (RecognizePanel)
 *   Tab 2: Quản lý gallery     (ManagePanel)
 *
 * Camera được mở/đóng theo tab đang active để tránh tranh chấp webcam.
 */
public class MainFrame extends JFrame {

    private RegisterPanel  registerPanel;
    private RecognizePanel recognizePanel;
    private ManagePanel    managePanel;
    private JTabbedPane    tabbedPane;

    private int currentTab = 0; // Tab đang active (mặc định: Đăng ký)

    public MainFrame() {
        setTitle("Face Detection App – BTL Java");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Khởi tạo các panel (camera CHƯA mở)
        registerPanel  = new RegisterPanel();
        recognizePanel = new RecognizePanel();
        managePanel    = new ManagePanel();

        // TabbedPane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        tabbedPane.addTab("📷  Đăng ký",   registerPanel);
        tabbedPane.addTab("🔍  Nhận diện", recognizePanel);
        tabbedPane.addTab("🗂  Quản lý",   managePanel);

        // Quản lý camera theo tab: chỉ mở camera của tab đang chọn
        tabbedPane.addChangeListener(e -> {
            int newTab = tabbedPane.getSelectedIndex();
            if (newTab == currentTab) return;

            // Dừng camera của tab cũ
            stopCamera(currentTab);

            // Mở camera của tab mới
            startCamera(newTab);

            currentTab = newTab;
        });

        add(tabbedPane);

        // Mở camera mặc định cho Tab 0 (Đăng ký)
        registerPanel.startCamera();

        // Đóng app: dừng tất cả camera
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                registerPanel.stopCamera();
                recognizePanel.stopCamera();
            }
        });
    }

    /** Mở camera của tab tương ứng */
    private void startCamera(int tabIndex) {
        if (tabIndex == 0) {
            registerPanel.startCamera();
        } else if (tabIndex == 1) {
            recognizePanel.startCamera();
        }
        // Tab 2 (ManagePanel) không cần camera
    }

    /** Dừng camera của tab tương ứng */
    private void stopCamera(int tabIndex) {
        if (tabIndex == 0) {
            registerPanel.stopCamera();
        } else if (tabIndex == 1) {
            recognizePanel.stopCamera();
        }
    }
}