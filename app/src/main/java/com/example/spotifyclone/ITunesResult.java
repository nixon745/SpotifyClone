package com.example.spotifyclone;

public class ITunesResult {
    private String trackName;
    private String primaryGenreName;
    private String artistName;
    private String collectionName; // זה שם האלבום ב-iTunes
    private String artworkUrl100;
    private String previewUrl;

    public String getTrackName() {
        return trackName;
    }

    public String getPrimaryGenreName() { // הוספתי get למען הסדר הטוב
        return primaryGenreName;
    }

    public String getCollectionName() { // הוספתי get
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

    // הפונקציה שהופכת את תוצאת ה-API לאובייקט Song
    public Song toSong() {
        // חשוב: הסדר כאן חייב להתאים בדיוק לבנאי (Constructor) שבנית ב-Song.java
        // title, artist, imageUrl, songUrl, genre, albumName
        return new Song(
                trackName,
                artistName,
                artworkUrl100,
                previewUrl,
                primaryGenreName,
                collectionName // זה נכנס לתוך ה-albumName ב-Song
        );
    }
}