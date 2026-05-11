package com.facedetection.ui;

import com.facedetection.ai.FaceDetector;
import com.facedetection.ai.FaceRecognizer;
import com.facedetection.dao.DatabaseConnection;
import com.facedetection.dao.DetectionLogDAO;
import com.facedetection.model.GalleryEntry;
import com.facedetection.model.GalleryRepository;

import org.opencv.core.Mat;
import org.opencv.core.CvType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * RegisterPanel.java – Nhiệm vụ 13 (Khanh – Chính)
 *
 * UI đăng ký khuôn mặt:
 * 1. Nhập tên người dùng
 * 2. Chụp ảnh từ webcam
 * 3. Detect khuôn mặt + trích xuất embedding (SFace)
 * 4. Lưu GalleryEntry vào gallery.dat (ObjectOutputStream)
 * 5. Ghi log vào DB (DetectionLogDAO)
 *
 * Pass condition: file gallery.dat tồn tại sau khi đăng ký,
 * restart app vẫn nhận được người đó.
 *
 * @author Viết Khánh
 */
public class RegisterPanel extends JPanel {

    // -------------------------------------------------------
    // UI components (giữ nguyên từ Tài)
    // -------------------------------------------------------
    JTextField txtName;
    JLabel previewLabel, statusLabel;
    JButton btnCapture, btnSave, btnCancel;
    CameraPanel cameraPanel;
    BufferedImage capturedImage;

    Color bg = Color.WHITE;
    Color panel = new Color(245, 245, 245);
    Color text = new Color(33, 37, 41);
    Color primary = new Color(0, 123, 255);
    Color success = new Color(40, 167, 69);
    Color danger = new Color(220, 53, 69);

    // -------------------------------------------------------
    // AI & DAO (Khánh thêm)
    // -------------------------------------------------------
    private FaceDetector faceDetector;
    private FaceRecognizer faceRecognizer;
    private DetectionLogDAO dao;
    private GalleryRepository galleryRepo;

    /** Kết quả detect của ảnh vừa chụp (dùng để extract embedding) */
    private Mat detectedFaces;
    /** Frame Mat tương ứng với capturedImage */
    private Mat capturedMat;

    /** Đường dẫn các model và file gallery */
    private static final String MODEL_DETECT = "models/face_detection_yunet_2023mar.onnx";
    private static final String MODEL_RECOG = "models/face_recognition_sface_2021dec.onnx";
    private static final String GALLERY_FILE = "gallery.dat";

    // -------------------------------------------------------
    // Constructor
    // -------------------------------------------------------
    public RegisterPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(bg);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        cameraPanel = new CameraPanel();

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(300, 0));
        rightPanel.setBackground(panel);
        rightPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblName = new JLabel("Tên người dùng:");
        lblName.setForeground(text);
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtName = new JTextField();
        txtName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        txtName.setAlignmentX(Component.LEFT_ALIGNMENT);

        previewLabel = new JLabel("PREVIEW", SwingConstants.CENTER);
        previewLabel.setOpaque(true);
        previewLabel.setBackground(Color.WHITE);
        previewLabel.setForeground(text);
        previewLabel.setPreferredSize(new Dimension(270, 250));
        previewLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        previewLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnCapture = new JButton("Chụp");
        btnSave = new JButton("Lưu");
        btnCancel = new JButton("Hủy");

        setButtonStyle(btnCapture, primary);
        setButtonStyle(btnSave, success);
        setButtonStyle(btnCancel, danger);

        statusLabel = new JLabel("Sẵn sàng");
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

        add(cameraPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        btnCapture.addActionListener(e -> captureImage());
        btnSave.addActionListener(e -> saveImage());
        btnCancel.addActionListener(e -> resetForm());

        // Khởi tạo AI + DAO + Gallery
        initAI();
        dao = new DetectionLogDAO();
        galleryRepo = new GalleryRepository();
    }

    /** Được gọi bởi MainFrame khi tab Đăng ký được chọn */
    public void startCamera() {
        cameraPanel.startCamera();
    }

    // -------------------------------------------------------
    // Khởi tạo FaceDetector + FaceRecognizer
    // -------------------------------------------------------
    private void initAI() {
        try {
            faceDetector = new FaceDetector(MODEL_DETECT);
            faceRecognizer = new FaceRecognizer(MODEL_RECOG);
            System.out.println("[RegisterPanel] Đã khởi tạo AI models.");
        } catch (Exception e) {
            System.err.println("[RegisterPanel] Lỗi load model AI: " + e.getMessage());
            faceDetector = null;
            faceRecognizer = null;
        }
    }

    // -------------------------------------------------------
    // Nút Chụp
    // -------------------------------------------------------
    private void captureImage() {
        capturedImage = cameraPanel.getCurrentImage();

        if (capturedImage == null) {
            JOptionPane.showMessageDialog(this, "Chưa có ảnh từ webcam!");
            return;
        }

        // Hiển thị preview
        Image img = capturedImage.getScaledInstance(270, 250, Image.SCALE_SMOOTH);
        previewLabel.setText("");
        previewLabel.setIcon(new ImageIcon(img));

        // Convert BufferedImage → Mat để detect
        capturedMat = bufferedImageToMat(capturedImage);
        detectedFaces = null;

        if (faceDetector != null && capturedMat != null) {
            detectedFaces = faceDetector.detect(capturedMat);
            int count = (detectedFaces != null) ? detectedFaces.rows() : 0;

            if (count == 0) {
                statusLabel.setText("Không phát hiện khuôn mặt – chụp lại!");
                statusLabel.setForeground(danger);
            } else {
                statusLabel.setText("Phát hiện " + count + " khuôn mặt ✓");
                statusLabel.setForeground(new Color(40, 167, 69));
            }
        } else {
            statusLabel.setText("Đã chụp ảnh (AI chưa sẵn sàng)");
            statusLabel.setForeground(text);
        }
    }

    // -------------------------------------------------------
    // Nút Lưu – Logic chính (Khánh)
    // -------------------------------------------------------
    private void saveImage() {
        // --- Validate ---
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên!");
            return;
        }
        if (capturedImage == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chụp ảnh trước!");
            return;
        }
        if (detectedFaces == null || detectedFaces.rows() == 0) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy khuôn mặt trong ảnh!\nVui lòng chụp lại.");
            return;
        }

        statusLabel.setText("Đang xử lý...");
        statusLabel.setForeground(primary);

        // --- Bước 1: Extract embedding ---
        float[] embedding = null;
        double score = 0.0;

        if (faceRecognizer != null && capturedMat != null) {
            try {
                Mat feature = faceRecognizer.extractFeature(capturedMat, detectedFaces);
                embedding = matToFloatArray(feature);
                score = 1.0; // Đăng ký mới → confidence = 1.0
                System.out.println("[RegisterPanel] Đã trích xuất embedding: " + embedding.length + " chiều");
            } catch (Exception e) {
                System.err.println("[RegisterPanel] Lỗi extract embedding: " + e.getMessage());
            }
        }

        // --- Bước 2: Lưu vào gallery.dat qua GalleryRepository ---
        if (embedding != null) {
            try {
                galleryRepo.add(name, embedding);
                System.out.println("[RegisterPanel] Đã lưu gallery.dat cho: " + name);
            } catch (Exception ex) {
                System.err.println("[RegisterPanel] Lưu gallery.dat thất bại: " + ex.getMessage());
            }
        }

        // --- Bước 3: Ghi log vào DB ---
        boolean savedDB = dao.insertLog(name, score);
        if (savedDB) {
            System.out.println("[RegisterPanel] Đã ghi log DB: " + name + " | score=" + score);
        } else {
            System.err.println("[RegisterPanel] Ghi log DB thất bại!");
        }

        // --- Thông báo kết quả ---
        String msg;
        if (embedding != null && savedDB) {
            msg = "✓ Đã đăng ký khuôn mặt thành công!\n"
                    + "Tên: " + name + "\n"
                    + "Đã lưu vào gallery.dat";
        } else if (savedDB) {
            msg = "✓ Đã đăng ký: " + name + "\n(Lưu DB thành công, gallery.dat chưa lưu được)";
        } else {
            msg = "⚠ Đã đăng ký: " + name + "\n(Có lỗi khi lưu, kiểm tra console)";
        }

        JOptionPane.showMessageDialog(this, msg, "Đăng ký khuôn mặt", JOptionPane.INFORMATION_MESSAGE);
        statusLabel.setText("Đã lưu: " + name);
        statusLabel.setForeground(success);
        resetForm();
    }

    // saveToGallery() đã được thay bằng galleryRepo.add() ở trên

    // -------------------------------------------------------
    // Nút Hủy
    // -------------------------------------------------------
    private void resetForm() {
        txtName.setText("");
        previewLabel.setIcon(null);
        previewLabel.setText("PREVIEW");
        statusLabel.setText("Sẵn sàng");
        statusLabel.setForeground(text);
        capturedImage = null;
        capturedMat = null;
        detectedFaces = null;
    }

    // -------------------------------------------------------
    // Util: BufferedImage → Mat
    // -------------------------------------------------------
    /**
     * Chuyển đổi BufferedImage sang OpenCV Mat (BGR).
     */
    private Mat bufferedImageToMat(BufferedImage bi) {
        try {
            // Đảm bảo ảnh dạng BGR (TYPE_3BYTE_BGR)
            BufferedImage converted = new BufferedImage(
                    bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D g = converted.createGraphics();
            g.drawImage(bi, 0, 0, null);
            g.dispose();

            byte[] pixels = ((DataBufferByte) converted.getRaster().getDataBuffer()).getData();
            Mat mat = new Mat(converted.getHeight(), converted.getWidth(), CvType.CV_8UC3);
            mat.put(0, 0, pixels);
            return mat;
        } catch (Exception e) {
            System.err.println("[RegisterPanel] Lỗi convert BufferedImage→Mat: " + e.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------
    // Util: Mat (embedding) → float[]
    // -------------------------------------------------------
    /**
     * Chuyển Mat embedding (1 hàng, N cột, CV_32F) sang float[].
     */
    private float[] matToFloatArray(Mat mat) {
        int size = (int) mat.total();
        float[] result = new float[size];
        mat.get(0, 0, result);
        return result;
    }

    // -------------------------------------------------------
    // Util: Button style
    // -------------------------------------------------------
    private void setButtonStyle(JButton btn, Color bgColor) {
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    // -------------------------------------------------------
    // Stop camera khi đóng panel
    // -------------------------------------------------------
    public void stopCamera() {
        cameraPanel.stopCamera();
        DatabaseConnection.getInstance().closeConnection();
    }
}