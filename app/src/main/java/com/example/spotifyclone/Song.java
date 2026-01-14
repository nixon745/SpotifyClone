package com.example.spotifyclone;

public class Song {

    private String title;
    private String imageUrl;

    public Song(String title, String imageUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
