package com.facedetection.model;

import java.io.*;
import java.util.*;

public class EmbeddingSerializer {
    private static final String GALLERY_FILE = "gallery.dat";

    public static void save(List<GalleryEntry> entries) {
        try (ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream(GALLERY_FILE))) {
            oos.writeObject(entries);
            System.out.println("Da luu" + entries.size() + "nguoi vao gallery.dat");
        } catch (IOException e) {
            System.err.println( "Loi ghi file" + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static List<GalleryEntry> load() {
        File file = new File(GALLERY_FILE);
        if (file.exists()) {
            System.out.println("Chua co file luu");
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            List<GalleryEntry> entries = (List<GalleryEntry>) ois.readObject();
            System.out.println("Da doc " + entries.size() + " nguoi tu gallery.dat");
            return entries;
        } catch (Exception e) {
            System.err.println("Loi doc file " +e.getMessage());
            return new ArrayList<>();
        }
    }
}