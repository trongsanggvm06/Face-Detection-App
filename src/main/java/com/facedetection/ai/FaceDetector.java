package com.facedetection.ai;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.FaceDetectorYN;

public class FaceDetector {
    private FaceDetectorYN detector;
    private static final Size INPUT_SIZE = new Size(320, 320);

    public FaceDetector(String modelPath) {
        detector = FaceDetectorYN.create(
            modelPath,
            "",
            INPUT_SIZE
        );

        System.out.println("[AI] FaceDetector loaded: " + modelPath);

    }

    public Mat detect(Mat frame) {
        Mat faces = new Mat();
        Size frameSize = new Size(
            frame.cols(),
            frame.rows()
        );
        detector.setInputSize(frameSize);
        detector.detect(frame, faces);

        return faces;
    }

    public void drawFaces(Mat frame, Mat faces) {
        for(int i = 0; i < faces.rows(); i++) {
            int x = (int) faces.get(i, 0)[0];
            int y = (int) faces.get(i, 1)[0];
            int w = (int) faces.get(i, 2)[0];
            int h = (int) faces.get(i, 3)[0];
            Imgproc.rectangle(
                frame,
                new Point(x, y),
                new Point(x + w, y + h),
                new Scalar(0,255,0),
                2
                
            );
        }
    }
}
