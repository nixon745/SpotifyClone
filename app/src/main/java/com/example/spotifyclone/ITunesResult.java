package com.example.spotifyclone;

public class ITunesResult {
    private String trackName;
    private String primaryGenreName;
    private String artistName;
    private String collectionName;
    private String artworkUrl100;
    private String previewUrl;

    // אלו ה-Getters. בלעדיהם, ה-HomeActivity לא יוכל "למשוך" את המידע
    public String getTrackName() {
        return trackName;
    }
    public String primaryGenreName() {
        return primaryGenreName;
    }

    public String collectionName() {
        return collectionName;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getArtworkUrl100() {
        return artworkUrl100;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    // הפונקציה שהופכת את זה לאובייקט Song שאתה כבר מכיר
    public Song toSong() {
        return new Song(trackName, artistName, artworkUrl100, previewUrl, primaryGenreName, collectionName);
    }
}