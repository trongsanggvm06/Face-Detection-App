package com.facedetection.model;

import com.facedetection.ai.FaceRecognizer;
import java.util.*;

/**
 * GalleryRepository.java – Task 4 (Trong An)
 * In-memory gallery loaded from gallery.dat. Supports add, findMatch, clear.
 * @author Trong An
 */
public class GalleryRepository {
    private List<GalleryEntry> entries;

    public GalleryRepository() {
        entries = EmbeddingSerializer.load();
        System.out.println("[Gallery] Ready with " + entries.size() + " registered person(s).");
    }

    public void add(String name, float[] embedding) {
        GalleryEntry entry = new GalleryEntry(name, embedding);
        entries.add(entry);
        EmbeddingSerializer.save(entries);
        System.out.println("[Gallery] Added: " + name + " | Total: " + entries.size() + " person(s).");
    }

    public String findMatch(float[] queryEmbedding, FaceRecognizer recognizer) {
        if (entries.isEmpty()) {
            return "UNKNOWN";
        }
        String bestName  = "UNKNOWN";
        double bestScore = -1;

        for (GalleryEntry entry : entries) {
            double score = cosineSimilarity(queryEmbedding, entry.getEmbedding());
            System.out.println("[Gallery] Compare with " + entry.getName() + ": score=" + String.format("%.4f", score));
            if (score > bestScore) {
                bestScore = score;
                bestName  = entry.getName();
            }
        }
        if (bestScore < 0.363) {
            System.out.println("[Gallery] No match found.");
            return "UNKNOWN";
        }
        System.out.println("[Gallery] Match: " + bestName + " (score=" + String.format("%.4f", bestScore) + ")");
        return bestName;
    }

    private double cosineSimilarity(float[] a, float[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot   += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public List<GalleryEntry> getAll() {
        return entries;
    }

    public void clear() {
        entries.clear();
        EmbeddingSerializer.save(entries);
        System.out.println("[Gallery] All entries cleared.");
    }
}