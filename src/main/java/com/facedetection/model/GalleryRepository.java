package com.facedetection.model;

import com.facedetection.ai.FaceRecognizer;
import java.util.*;
public class GalleryRepository {
    private List<GalleryEntry> entries;

    public GalleryRepository() {
        entries = EmbeddingSerializer.load();
        System.out.println("San sang voi " + entries.size() + " nguoi da dang ky");
    }
    public void add (String name, float[] embedding) {
        GalleryEntry entry = new GalleryEntry(name, embedding);

        entries.add(entry);

        EmbeddingSerializer.save(entries);

        System.out.println("Da them: " + name + "\n\n\nTong: " +entries.size() + " nguoi");
    }
    public String findMatch(float[] queryEmbedding, FaceRecognizer recognizer) {
        if(entries.isEmpty()) {
            return "UNKNOWN";
        }
        String bestName = "UNKNOWN";
        double bestScore = -1;

        for( GalleryEntry entry : entries) {
            double score = cosineSimilarity(queryEmbedding, entry.getEmbedding());

            System.out.println("So sanh voi " + entry.getName() + ": score" + score);
            
            if (score > bestScore) {
                bestScore = score;
                bestName = entry.getName();
            }
        }
        if(bestScore < 0.363) {
            System.out.println("Khong khop voi ai");
            return "UNKNOWN";
        }
        System.out.println("Ket qua " + bestName + "(score = " +bestScore + ")");

        return bestName;
    }
    private double cosineSimilarity(float[] a, float[] b) {
        double dot = 0;
        double normA = 0;
        double normB = 0;

        for (int i = 0; i < a.length; i++) {
            dot += a[i] *b[i];
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
        System.out.println("Da xoa toan bo gallery!");
    }
}