package com.facedetection.ui;

import com.facedetection.ai.FaceDetector;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;

public class CameraPanel extends JPanel {

    VideoCapture camera;
    Mat          frame;
    BufferedImage image;
    boolean      running = false;

    private volatile FaceDetector faceDetector = null;

    public CameraPanel() {
        setBackground(Color.BLACK);
    }

    public void setFaceDetector(FaceDetector detector) {
        this.faceDetector = detector;
    }

    public void startCamera() {
        camera = new VideoCapture(0);
        frame  = new Mat();

        if (!camera.isOpened()) {
            JOptionPane.showMessageDialog(this, "Không mở được webcam");
            return;
        }

        running = true;

        Thread t = new Thread(() -> {
            while (running) {
                if (camera.read(frame)) {

                    FaceDetector detector = faceDetector;
                    if (detector != null && !frame.empty()) {
                        try {
                            Mat faces = detector.detect(frame);
                            if (faces != null && faces.rows() > 0) {
                                detector.drawFaces(frame, faces);
                            }
                        } catch (Exception e) {
                        }
                    }

                    image = matToImage(frame);
                    repaint();
                }

                try {
                    Thread.sleep(30);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        t.setDaemon(true);
        t.start();
    }

    public void stopCamera() {
        running = false;
        faceDetector = null;
        if (camera != null) {
            camera.release();
        }
    }

    public BufferedImage getCurrentImage() {
        return image;
    }

    public Mat getCurrentFrame() {
        return frame;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (image != null) {
            g.drawImage(image, getWidth(), 0, -getWidth(), getHeight(), null);
        } else {
            g.setColor(Color.WHITE);
            g.drawString("Đang mở webcam...", getWidth() / 2 - 60, getHeight() / 2);
        }
    }

    private BufferedImage matToImage(Mat mat) {
        Mat rgb = new Mat();
        Imgproc.cvtColor(mat, rgb, Imgproc.COLOR_BGR2RGB);

        int width    = rgb.width();
        int height   = rgb.height();
        int channels = rgb.channels();

        byte[] source = new byte[width * height * channels];
        rgb.get(0, 0, source);

        BufferedImage img    = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        byte[]        target = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();

        for (int i = 0; i < source.length; i += 3) {
            target[i]     = source[i + 2];
            target[i + 1] = source[i + 1];
            target[i + 2] = source[i];
        }

        return img;
    }
}