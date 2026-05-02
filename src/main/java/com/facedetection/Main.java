package com.facedetection;

import org.opencv.core.Core;

public class Main {
    public static void main(String[] args) {
        System.load(System.getProperty("user.dir") + "\\opencv_java490.dll");
        System.out.println("OpenCV version: " + Core.VERSION);
        System.out.println("FaceDetectionApp started!");
    }
}