package com.example.spotifyclone;

import java.io.Serializable;

public class Song implements Serializable {

    private String title;
    private String artist;
    private String imageUrl;
    private String songUrl;
    private String genre;
    private String albumName;

    // שדות חדשים למיקום
    private double lat = -1; // -1 אומר שאין לשיר מיקום מוגדר
    private double lon = -1;

    public Song() {}

    public Song(String title, String artist, String imageUrl, String songUrl, String genre, String albumName) {
        this.title = title;
        this.artist = artist;
        this.imageUrl = imageUrl;
        this.songUrl = songUrl;
        this.genre = genre;
        this.albumName = albumName;
    }

    // Getters & Setters חדשים
    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }
    public double getLon() { return lon; }
    public void setLon(double lon) { this.lon = lon; }

    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getImageUrl() { return imageUrl; }
    public String getSongUrl() { return songUrl; }
    public String getGenre() { return genre; }
    public String getAlbumName() { return albumName; }
}