package com.facedetection.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class RegisterPanel extends JPanel{
    JTextField txtName;
    JLabel previewLabel,statusLabel;
    JButton btnCapture,btnSave,btnCancel;
    CameraPanel cameraPanel;
    BufferedImage capturedImage;

    Color bg=Color.WHITE;
    Color panel=new Color(245,245,245);
    Color text=new Color(33,37,41);

    Color primary=new Color(0,123,255);
    Color success=new Color(40,167,69);
    Color danger=new Color(220,53,69);

    public RegisterPanel(){
        setLayout(new BorderLayout(15,15));
        setBackground(bg);
        setBorder(new EmptyBorder(20,20,20,20));

        cameraPanel=new CameraPanel();

        JPanel rightPanel=new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel,BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(300,0));
        rightPanel.setBackground(panel);
        rightPanel.setBorder(new EmptyBorder(15,15,15,15));

        JLabel lblName=new JLabel("Tên người dùng:");
        lblName.setForeground(text);
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtName=new JTextField();
        txtName.setMaximumSize(new Dimension(Integer.MAX_VALUE,35));
        txtName.setAlignmentX(Component.LEFT_ALIGNMENT);

        previewLabel=new JLabel("PREVIEW",SwingConstants.CENTER);
        previewLabel.setOpaque(true);
        previewLabel.setBackground(Color.WHITE);
        previewLabel.setForeground(text);
        previewLabel.setPreferredSize(new Dimension(270,250));
        previewLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE,250));
        previewLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnCapture=new JButton("Chụp");
        btnSave=new JButton("Lưu");
        btnCancel=new JButton("Hủy");

        setButtonStyle(btnCapture,primary);
        setButtonStyle(btnSave,success);
        setButtonStyle(btnCancel,danger);

        statusLabel=new JLabel("Sẵn sàng");
        statusLabel.setForeground(text);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        rightPanel.add(lblName);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(txtName);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(previewLabel);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(btnCapture);
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(btnSave);
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(btnCancel);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(statusLabel);

        add(cameraPanel,BorderLayout.CENTER);
        add(rightPanel,BorderLayout.EAST);

        btnCapture.addActionListener(e->captureImage());
        btnSave.addActionListener(e->saveImage());
        btnCancel.addActionListener(e->resetForm());

        cameraPanel.startCamera();
    }

    private void setButtonStyle(JButton btn,Color bgColor){
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial",Font.BOLD,16));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE,45));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void captureImage(){
        capturedImage=cameraPanel.getCurrentImage();

        if(capturedImage==null){
            JOptionPane.showMessageDialog(this,"Chưa có ảnh từ webcam");
            return;
        }

        Image img=capturedImage.getScaledInstance(270,250,Image.SCALE_SMOOTH);
        previewLabel.setText("");
        previewLabel.setIcon(new ImageIcon(img));
        statusLabel.setText("Đã chụp ảnh");
    }

    private void saveImage(){
        String name=txtName.getText().trim();

        if(name.isEmpty()){
            JOptionPane.showMessageDialog(this,"Vui lòng nhập tên");
            return;
        }

        if(capturedImage==null){
            JOptionPane.showMessageDialog(this,"Vui lòng chụp ảnh trước");
            return;
        }

        JOptionPane.showMessageDialog(this,"Đã lưu khuôn mặt cho: "+name);
        resetForm();
    }

    private void resetForm(){
        txtName.setText("");
        previewLabel.setIcon(null);
        previewLabel.setText("PREVIEW");
        statusLabel.setText("Sẵn sàng");
        capturedImage=null;
    }

    public void stopCamera(){
        cameraPanel.stopCamera();
    }
}