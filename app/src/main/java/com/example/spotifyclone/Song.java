package com.example.spotifyclone;

public class Song {
    private String title;
    private String imageUrl;
    private String songUrl;

    public Song(String title, String imageUrl, String songUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.songUrl = songUrl;
    }

    // Constructor ישן לתאימות לאחור
    public Song(String title, String imageUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.songUrl = "";
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSongUrl() {
        return songUrl;
    }
}