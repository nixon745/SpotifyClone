package com.example.spotifyclone;

public class Song {
    private String title;
    private int imageResId;

    public Song(String title, int imageResId) {
        this.title = title;
        this.imageResId = imageResId;
    }

    public String getTitle() {
        return title;
    }

    public int getImageResId() {
        return imageResId;
    }
}
