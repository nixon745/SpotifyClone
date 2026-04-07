package com.example.spotifyclone;

import java.io.Serializable;

public class Song implements Serializable {

    private String title;
    private String artist;
    private String imageUrl;
    private String songUrl;
    private String genre;
    private String albumName; // שדה חדש למיון וסדר

    public Song() {
        // חובה ל-Firestore
    }

    // בנאי מעודכן שכולל אלבום
    public Song(String title, String artist, String imageUrl, String songUrl, String genre, String albumName) {
        this.title = title;
        this.artist = artist;
        this.imageUrl = imageUrl;
        this.songUrl = songUrl;
        this.genre = genre;
        this.albumName = albumName;
    }

    // בנאי נוסף למקרה של שירים ללא אלבום (כדי לא לשבור קוד קיים)
    public Song(String title, String artist, String imageUrl, String songUrl, String genre) {
        this(title, artist, imageUrl, songUrl, genre, "Unknown Album");
    }

    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getImageUrl() { return imageUrl; }
    public String getSongUrl() { return songUrl; }
    public String getGenre() { return genre; }
    public String getAlbumName() { return albumName; } // ה-Getter שמשמש למיון
}