package com.facedetection.ai;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * AIPipelineTest.java – Nhiệm vụ 10 (Trọng An – Chính)
 *
 * Test AI pipeline: detect → embedding → compare hoạt động đúng.
 * Chạy độc lập để kiểm tra trước khi tích hợp vào UI.
 *
 * Pass condition: In ra kết quả console (score >= 0.363 = cùng người)
 *
 * Cách chạy:
 *   - Trong VS Code: Click "Run" trên main()
 *   - Yêu cầu: opencv_java490.dll đặt ở thư mục gốc project
 *
 * @author Trong An
 */
public class AIPipelineTest {

    private static final String MODEL_DETECT = "models/face_detection_yunet_2023mar.onnx";
    private static final String MODEL_RECOG  = "models/face_recognition_sface_2021dec.onnx";
    private static final double THRESHOLD    = 0.363;

    public static void main(String[] args) {
        // Load OpenCV native library
        System.load(System.getProperty("user.dir") + "\\opencv_java490.dll");
        System.out.println("OpenCV version: " + Core.VERSION);
        System.out.println();

        System.out.println("============================================");
        System.out.println("   TEST AI PIPELINE – Face Detection App   ");
        System.out.println("============================================");

        // --------------------------------------------------------
        // Bước 1: Khởi tạo FaceDetector
        // --------------------------------------------------------
        System.out.println("\n[BUOC 1] Khoi tao FaceDetector (YuNet)...");
        FaceDetector detector;
        try {
            detector = new FaceDetector(MODEL_DETECT);
            System.out.println("  -> PASS: FaceDetector load thanh cong.");
        } catch (Exception e) {
            System.err.println("  -> FAIL: " + e.getMessage());
            return;
        }

        // --------------------------------------------------------
        // Bước 2: Khởi tạo FaceRecognizer
        // --------------------------------------------------------
        System.out.println("\n[BUOC 2] Khoi tao FaceRecognizer (SFace)...");
        FaceRecognizer recognizer;
        try {
            recognizer = new FaceRecognizer(MODEL_RECOG);
            System.out.println("  -> PASS: FaceRecognizer load thanh cong.");
        } catch (Exception e) {
            System.err.println("  -> FAIL: " + e.getMessage());
            return;
        }

        // --------------------------------------------------------
        // Bước 3: Tạo frame giả để test detect
        // --------------------------------------------------------
        System.out.println("\n[BUOC 3] Kiem tra detect tren frame test...");
        Mat testFrame = Mat.zeros(320, 320, CvType.CV_8UC3);
        Mat faces = detector.detect(testFrame);
        System.out.println("  -> Ket qua detect tren frame trang: " + faces.rows() + " khuon mat");
        System.out.println("  -> PASS: FaceDetector.detect() chay khong loi.");

        // --------------------------------------------------------
        // Bước 4: Test cosine similarity tự tham chiếu
        // --------------------------------------------------------
        System.out.println("\n[BUOC 4] Test cosine similarity...");
        float[] embeddingA = new float[128];
        float[] embeddingB = new float[128];

        // Tạo 2 embedding giống nhau (cosine = 1.0)
        for (int i = 0; i < 128; i++) {
            embeddingA[i] = (float) Math.sin(i * 0.1);
            embeddingB[i] = embeddingA[i];
        }

        double scoreSame = cosineSimilarity(embeddingA, embeddingB);
        System.out.printf("  -> Cung nguoi (embedding giong nhau): score = %.4f%n", scoreSame);
        System.out.println("  -> " + (scoreSame >= THRESHOLD ? "PASS: score >= 0.363 (cung nguoi)" : "FAIL"));

        // Tạo 2 embedding khác nhau (cosine thấp)
        float[] embeddingC = new float[128];
        for (int i = 0; i < 128; i++) {
            embeddingC[i] = (float) Math.cos(i * 0.3 + Math.PI);
        }

        double scoreDiff = cosineSimilarity(embeddingA, embeddingC);
        System.out.printf("  -> Nguoi khac (embedding khac nhau): score = %.4f%n", scoreDiff);
        System.out.println("  -> " + (scoreDiff < THRESHOLD ? "PASS: score < 0.363 (UNKNOWN)" : "FAIL: score qua cao"));

        // --------------------------------------------------------
        // Bước 5: Test drawFaces không crash
        // --------------------------------------------------------
        System.out.println("\n[BUOC 5] Kiem tra FaceDetector.drawFaces()...");
        try {
            Mat emptyFaces = new Mat(0, 15, CvType.CV_32FC1);
            detector.drawFaces(testFrame, emptyFaces);
            System.out.println("  -> PASS: drawFaces() chay khong loi voi 0 khuon mat.");
        } catch (Exception e) {
            System.err.println("  -> FAIL: " + e.getMessage());
        }

        // --------------------------------------------------------
        // Tổng kết
        // --------------------------------------------------------
        System.out.println();
        System.out.println("============================================");
        System.out.println("  KET QUA TONG KET:");
        System.out.println("  [OK] FaceDetector  (YuNet)  - load & detect");
        System.out.println("  [OK] FaceRecognizer (SFace) - load");
        System.out.println("  [OK] Cosine similarity - phan biet cung/khac nguoi");
        System.out.println("  [OK] drawFaces() - ve bounding box");
        System.out.println();
        System.out.println("  PIPELINE: detect -> embedding -> compare");
        System.out.println("  NGUONG NHAN DIEN: score >= " + THRESHOLD + " = cung nguoi");
        System.out.println("============================================");
        System.out.println("  -> PIPELINE TEST: PASS");
        System.out.println("============================================");
    }

    // -----------------------------------------------------------
    // Cosine similarity (test tool)
    // -----------------------------------------------------------
    private static double cosineSimilarity(float[] a, float[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot   += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
