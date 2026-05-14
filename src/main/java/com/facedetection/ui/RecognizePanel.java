package com.facedetection.ui;

import com.facedetection.ai.FaceDetector;
import com.facedetection.ai.FaceRecognizer;
import com.facedetection.dao.DetectionLogDAO;
import com.facedetection.model.GalleryEntry;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecognizePanel extends JPanel {

    private CameraPanel cameraPanel;
    private JLabel nameLabel;
    private JLabel scoreLabel;
    private JLabel statusLabel;
    private JButton btnStartStop;
    private DefaultTableModel tableModel;

    private static final Color BG = Color.WHITE;
    private static final Color PANEL_BG = new Color(245, 245, 245);
    private static final Color TEXT = new Color(33, 37, 41);
    private static final Color PRIMARY = new Color(0, 123, 255);
    private static final Color SUCCESS = new Color(40, 167, 69);
    private static final Color DANGER = new Color(220, 53, 69);
    private static final Color ORANGE = new Color(255, 140, 0);

    // -----------------------------------------------------------
    // AI + DAO
    // -----------------------------------------------------------
    private FaceDetector faceDetector;
    private FaceRecognizer faceRecognizer;
    private DetectionLogDAO dao;

    // -----------------------------------------------------------
    // Recognition control
    // -----------------------------------------------------------
    private Thread recognitionThread;
    private volatile boolean running = false;

    private static final double THRESHOLD = 0.363;
    private static final int RECOGNIZE_MS = 400;
    private static final long LOG_INTERVAL_MS = 5000;
    private static final int NO_FACE_TOLERANCE = 5;

    private String lastLoggedName = "";
    private long lastLogTime = 0;
    private int noFaceCount = 0;

    private static final String MODEL_DETECT = "models/face_detection_yunet_2023mar.onnx";
    private static final String MODEL_RECOG = "models/face_recognition_sface_2021dec.onnx";
    private static final String GALLERY_FILE = "gallery.dat";

    public RecognizePanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        cameraPanel = new CameraPanel();

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(300, 0));
        rightPanel.setBackground(PANEL_BG);
        rightPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("KẾT QUẢ NHẬN DIỆN");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        nameLabel = new JLabel("---");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 26));
        nameLabel.setForeground(TEXT);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        scoreLabel = new JLabel("Độ tin cậy: --");
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        scoreLabel.setForeground(TEXT);
        scoreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Status
        statusLabel = new JLabel("Chưa bắt đầu");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        statusLabel.setForeground(ORANGE);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Bảng lịch sử nhận diện
        String[] cols = { "Thời gian", "Tên", "Score" };
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable logTable = new JTable(tableModel);
        logTable.setFont(new Font("Arial", Font.PLAIN, 12));
        logTable.setRowHeight(22);
        logTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        logTable.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(logTable);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        scrollPane.setBorder(new TitledBorder("Lịch sử nhận diện (10 gần nhất)"));

        btnStartStop = new JButton("  Bắt đầu nhận diện");
        btnStartStop.setBackground(SUCCESS);
        btnStartStop.setForeground(Color.WHITE);
        btnStartStop.setFocusPainted(false);
        btnStartStop.setFont(new Font("Arial", Font.BOLD, 14));
        btnStartStop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btnStartStop.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnStartStop.addActionListener(e -> toggleRecognition());

        rightPanel.add(title);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(nameLabel);
        rightPanel.add(Box.createVerticalStrut(4));
        rightPanel.add(scoreLabel);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(scrollPane);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(btnStartStop);

        add(cameraPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        initAI();
        dao = new DetectionLogDAO();
    }

    public void startCamera() {
        cameraPanel.startCamera();
    }

    private void initAI() {
        try {
            faceDetector = new FaceDetector(MODEL_DETECT);
            faceRecognizer = new FaceRecognizer(MODEL_RECOG);
            System.out.println("[RecognizePanel] AI models ready.");
        } catch (Exception e) {
            System.err.println("[RecognizePanel] Error loading AI: " + e.getMessage());
            faceDetector = null;
            faceRecognizer = null;
        }
    }

    private void toggleRecognition() {
        if (running) {
            stopRecognition();
        } else {
            startRecognition();
        }
    }

    private void startRecognition() {
        if (faceDetector == null || faceRecognizer == null) {
            JOptionPane.showMessageDialog(this, "AI chưa sẵn sàng!\nKiểm tra file model trong thư mục models/");
            return;
        }
        List<GalleryEntry> gallery = loadGallery();
        if (gallery.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Gallery trống!\nVui lòng đăng ký khuôn mặt trước.");
            return;
        }

        running = true;
        btnStartStop.setText("  Dừng nhận diện");
        btnStartStop.setBackground(DANGER);
        cameraPanel.setFaceDetector(faceDetector);

        recognitionThread = new Thread(() -> recognitionLoop(gallery));
        recognitionThread.setDaemon(true);
        recognitionThread.start();
    }

    private void stopRecognition() {
        running = false;
        // Tắt bounding box
        cameraPanel.setFaceDetector(null);
        btnStartStop.setText(" Bắt đầu nhận diện");
        btnStartStop.setBackground(SUCCESS);
        nameLabel.setText("---");
        scoreLabel.setText("Độ tin cậy: --");
        statusLabel.setText("Đã dừng");
        statusLabel.setForeground(ORANGE);
    }

    // -----------------------------------------------------------
    private void recognitionLoop(List<GalleryEntry> gallery) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        while (running) {
            try {
                Thread.sleep(RECOGNIZE_MS);

                BufferedImage img = cameraPanel.getCurrentImage();
                if (img == null)
                    continue;

                Mat frameMat = bufferedImageToMat(img);
                if (frameMat == null)
                    continue;

                Mat faces = faceDetector.detect(frameMat);

                if (faces == null || faces.rows() == 0) {
                    noFaceCount++;
                    if (noFaceCount >= NO_FACE_TOLERANCE) {
                        SwingUtilities.invokeLater(() -> {
                            nameLabel.setText("Không có mặt");
                            nameLabel.setForeground(ORANGE);
                            scoreLabel.setText("Độ tin cậy: --");
                            statusLabel.setText("Không phát hiện khuôn mặt");
                        });
                    }
                    continue;
                }
                noFaceCount = 0;

                Mat feature = faceRecognizer.extractFeature(frameMat, faces);
                float[] embedding = matToFloatArray(feature);

                String[] result = findBestMatch(embedding, gallery);
                String recognizedName = result[0];
                double score = Double.parseDouble(result[1]);
                String timeStr = sdf.format(new Date());

                SwingUtilities.invokeLater(() -> {
                    nameLabel.setText(recognizedName);
                    nameLabel.setForeground(recognizedName.equals("UNKNOWN") ? DANGER : SUCCESS);
                    scoreLabel.setText(String.format(Locale.US, "Độ tin cậy: %.1f%%", score));
                });

                long now = System.currentTimeMillis();
                boolean nameChanged = !recognizedName.equals(lastLoggedName);
                boolean timeElapsed = (now - lastLogTime) >= LOG_INTERVAL_MS;

                if (nameChanged || timeElapsed) {
                    double logScore = score / 100.0;
                    dao.insertLog(recognizedName, logScore);
                    lastLoggedName = recognizedName;
                    lastLogTime = now;

                    final String n = recognizedName;
                    final String sc = String.format(Locale.US, "%.1f%%", score);
                    final String t = timeStr;
                    SwingUtilities.invokeLater(() -> {
                        tableModel.insertRow(0, new Object[] { t, n, sc });
                        if (tableModel.getRowCount() > 10) {
                            tableModel.removeRow(tableModel.getRowCount() - 1);
                        }
                    });
                }

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("[RecognizePanel] Loi nhan dien: " + e.getMessage());
            }
        }
    }

    // -----------------------------------------------------------
    // So khớp embedding với gallery → trả về {name, score%}
    // -----------------------------------------------------------
    private String[] findBestMatch(float[] query, List<GalleryEntry> gallery) {
        String bestName = "UNKNOWN";
        double bestScore = -1.0;

        for (GalleryEntry entry : gallery) {
            double score = cosineSimilarity(query, entry.getEmbedding());
            if (score > bestScore) {
                bestScore = score;
                bestName  = entry.getName();
            }
        }

        if (bestScore < THRESHOLD) {
            bestName = "UNKNOWN";
        }

        return new String[] { bestName, String.format(Locale.US, "%.1f", bestScore * 100) };
    }

    private double cosineSimilarity(float[] a, float[] b) {
        double dot = 0, normA = 0, normB = 0;
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0)
            return 0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    @SuppressWarnings("unchecked")
    private List<GalleryEntry> loadGallery() {
        File file = new File(GALLERY_FILE);
        if (!file.exists()) {
            System.out.println("[RecognizePanel] gallery.dat not found.");
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            List<GalleryEntry> entries = (List<GalleryEntry>) ois.readObject();
            System.out.println("[RecognizePanel] Loaded " + entries.size() + " person(s) from gallery.dat");
            return entries;
        } catch (Exception e) {
            System.err.println("[RecognizePanel] Error reading gallery.dat: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private Mat bufferedImageToMat(BufferedImage bi) {
        try {
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
            System.err.println("[RecognizePanel] Error converting image: " + e.getMessage());
            return null;
        }
    }

    private float[] matToFloatArray(Mat mat) {
        int size = (int) mat.total();
        float[] result = new float[size];
        mat.get(0, 0, result);
        return result;
    }

    public void stopCamera() {
        stopRecognition();
        cameraPanel.stopCamera();
    }
}
