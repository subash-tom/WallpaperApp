package com.example.pixivo;

import java.io.Serializable;

public class WallpaperModel implements Serializable {

    private String imageUrl;
    private String category;

    public WallpaperModel() {
    }

    public WallpaperModel(String imageUrl, String category) {
        this.imageUrl = imageUrl;
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}