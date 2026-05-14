package com.facedetection.model;

import java.io.*;
import java.util.*;

public class EmbeddingSerializer {
    private static final String GALLERY_FILE = "gallery.dat";

    public static void save(List<GalleryEntry> entries) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(GALLERY_FILE))) {
            oos.writeObject(entries);
            System.out.println("[Gallery] Saved " + entries.size() + " entries to gallery.dat");
        } catch (IOException e) {
            System.err.println("[Gallery] Error writing gallery.dat: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static List<GalleryEntry> load() {
        File file = new File(GALLERY_FILE);
        if (!file.exists()) {
            System.out.println("[Gallery] gallery.dat not found - starting empty.");
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            List<GalleryEntry> entries = (List<GalleryEntry>) ois.readObject();
            System.out.println("[Gallery] Loaded " + entries.size() + " entries from gallery.dat");
            return entries;
        } catch (Exception e) {
            System.err.println("[Gallery] Error reading gallery.dat: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}