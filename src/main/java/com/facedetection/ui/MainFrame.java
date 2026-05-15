package com.facedetection.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame {

    private RegisterPanel  registerPanel;
    private RecognizePanel recognizePanel;
    private ManagePanel    managePanel;
    private JTabbedPane    tabbedPane;

    private int currentTab = 0;

    public MainFrame() {
        setTitle("Face Detection App");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        registerPanel  = new RegisterPanel();
        recognizePanel = new RecognizePanel();
        managePanel    = new ManagePanel();

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        tabbedPane.addTab(" Đăng ký",   registerPanel);
        tabbedPane.addTab("Nhận diện", recognizePanel);
        tabbedPane.addTab("Quản lý",   managePanel);

        tabbedPane.addChangeListener(e -> {
            int newTab = tabbedPane.getSelectedIndex();
            if (newTab == currentTab) return;

            stopCamera(currentTab);
            startCamera(newTab);

            currentTab = newTab;
        });

        add(tabbedPane);

        registerPanel.startCamera();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                registerPanel.stopCamera();
                recognizePanel.stopCamera();
            }
        });
    }

    private void startCamera(int tabIndex) {
        if (tabIndex == 0) {
            registerPanel.startCamera();
        } else if (tabIndex == 1) {
            recognizePanel.startCamera();
        }
    }

    private void stopCamera(int tabIndex) {
        if (tabIndex == 0) {
            registerPanel.stopCamera();
        } else if (tabIndex == 1) {
            recognizePanel.stopCamera();
        }
    }
}
