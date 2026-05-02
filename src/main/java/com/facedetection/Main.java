package com.facedetection;

import javax.swing.SwingUtilities;
import org.opencv.core.Core;
import com.facedetection.ui.MainFrame;

public class Main{
    public static void main(String[] args){
        System.load(System.getProperty("user.dir")+"\\opencv_java490.dll");

        System.out.println("OpenCV version: "+Core.VERSION);
        System.out.println("FaceDetectionApp started!");

        SwingUtilities.invokeLater(()->{
            new MainFrame().setVisible(true);
        });
    }
}