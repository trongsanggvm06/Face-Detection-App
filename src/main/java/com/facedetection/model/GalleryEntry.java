package com.facedetection.model;

import java.io.Serializable;

public class GalleryEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private float[] embedding;

    public GalleryEntry(String name, float[] embedding) {
        this.name = name;
        this.embedding = embedding;
    }

    public String getName() {
        return name;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    @Override
    public String toString() {
        return "GalleryEntry{name='" + name + "', embeddingLen=" + embedding.length + "}";
    }
}
