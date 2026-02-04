package com.example.spotifyclone;

public class Song {
    private String title;
    private String imageUrl;
    private String songUrl;
    private String genre;

    // בנאי ריק חובה עבור Firebase
    public Song() {}

    public Song(String title, String imageUrl, String songUrl, String genre) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.songUrl = songUrl;
        this.genre = genre;
    }

    // Getters
    public String getTitle() { return title; }
    public String getImageUrl() { return imageUrl; }
    public String getSongUrl() { return songUrl; }
    public String getGenre() { return genre; }

    // Setters (אופציונלי, למקרה שתצטרך לעדכן נתונים)
    public void setTitle(String title) { this.title = title; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setSongUrl(String songUrl) { this.songUrl = songUrl; }
    public void setGenre(String genre) { this.genre = genre; }
}