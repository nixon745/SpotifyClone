package com.example.spotifyclone;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import java.io.IOException;
import java.util.ArrayList;

public class MusicManager {
    private static MusicManager instance;
    public MediaPlayer mediaPlayer;
    public ArrayList<Song> currentList;
    public int currentIndex = -1;

    public static MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }
    public void stopMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
            currentIndex = -1;
        }
    }
    public void playSong(Context context, Song song) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }
            mediaPlayer.reset();
            mediaPlayer.setDataSource(song.getSongUrl());

            // הגדרת סוג האודיו (חשוב לאנדרואיד גרסאות חדשות)
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());

            // הכנה אסינכרונית - קריטי למנוע את ה-Toast של השגיאה!
            mediaPlayer.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}