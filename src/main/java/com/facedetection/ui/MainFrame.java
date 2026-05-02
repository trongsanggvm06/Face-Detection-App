package com.facedetection.ui;

import javax.swing.*;

import java.awt.event.*;

public class MainFrame extends JFrame{
    RegisterPanel registerPanel;

    public MainFrame(){
        setTitle("Face Detection App");
        setSize(1000,650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        registerPanel=new RegisterPanel();
        add(registerPanel);

        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                registerPanel.stopCamera();
            }
        });
    }
}