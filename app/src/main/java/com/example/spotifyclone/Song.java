package com.example.spotifyclone;

import java.io.Serializable;

public class Song implements Serializable {

    private String title;
    private String artist;
    private String imageUrl;
    private String songUrl;
    private String genre;

    public Song() {
        // חובה ל-Firestore
    }

    public Song(String title, String artist, String imageUrl, String songUrl, String genre) {
        this.title = title;
        this.artist = artist;
        this.imageUrl = imageUrl;
        this.songUrl = songUrl;
        this.genre = genre;
    }

    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getImageUrl() { return imageUrl; }
    public String getSongUrl() { return songUrl; }
    public String getGenre() { return genre; }
}