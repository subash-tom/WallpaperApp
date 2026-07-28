package com.example.pixivo;

import java.io.Serializable;

public class WallpaperModel implements Serializable {

    private String imageUrl;
    private String category;

    // Image already loaded or not
    private boolean loaded = false;

    public WallpaperModel() {
    }

    public WallpaperModel(String imageUrl, String category) {
        this.imageUrl = imageUrl;
        this.category = category;
        this.loaded = false;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }
}