package com.facedetection.ai;

import org.opencv.core.Mat;
import org.opencv.objdetect.FaceRecognizerSF;

public class FaceRecognizer {

    private FaceRecognizerSF recognizer;
    private static final double THRESHOLD = 0.363;
    
    public FaceRecognizer(String modelPath) {
        recognizer = FaceRecognizerSF.create(modelPath, "");
        System.out.println("[AI] FaceRecognizer loaded: " + modelPath);
    }

    public Mat extractFeature(Mat frame, Mat faceBox) {
        Mat aligned = new Mat();
        recognizer.alignCrop(frame, faceBox.row(0), aligned);
        Mat feature = new Mat();
        recognizer.feature(aligned, feature);
        return feature;
    }

    public boolean match(Mat t1, Mat t2) {
        double score = recognizer.match(t1, t2, FaceRecognizerSF.FR_COSINE);
        System.out.println("[AI] Similarity score: " + score);
        return score >= THRESHOLD;
    }
}
