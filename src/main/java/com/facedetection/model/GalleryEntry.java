package com.facedetection.model;

import java.io.Serializable;

/**
 * GalleryEntry.java
 *
 * Model lưu thông tin một khuôn mặt đã đăng ký.
 * Implement Serializable để lưu vào file gallery.dat bằng ObjectOutputStream.
 *
 * @author Viết Khánh
 */
public class GalleryEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Tên người được đăng ký */
    private String name;

    /** Vector embedding 128 chiều trích xuất bởi SFace */
    private float[] embedding;

    public GalleryEntry(String name, float[] embedding) {
        this.name      = name;
        this.embedding = embedding;
    }

    public String getName()      { return name; }
    public float[] getEmbedding(){ return embedding; }

    @Override
    public String toString() {
        return "GalleryEntry{name='" + name + "', embeddingLen=" + embedding.length + "}";
    }
}
