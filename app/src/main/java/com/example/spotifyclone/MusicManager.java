package com.example.spotifyclone;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

public class MusicManager {
    private static MusicManager instance;
    public MediaPlayer mediaPlayer;
    public ArrayList<Song> currentList;
    public int currentIndex = -1;

    public interface MusicUpdateListener {
        void onSongChanged();
    }
    private MusicUpdateListener listener;
    private int currentTrackColor = 0xFF2A2A2A;
    public int getCurrentTrackColor() {
        return currentTrackColor;
    }

    public void setCurrentTrackColor(int color) {
        this.currentTrackColor = color;
    }
    public void setListener(MusicUpdateListener listener) {
        this.listener = listener;
    }

    public static MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }

    public void playNext(Context context) {
        if (currentList == null || currentList.isEmpty()) return;

        if (currentIndex < currentList.size() - 1) {
            currentIndex++;
        } else {
            currentIndex = 0;
        }

        // משתמשים ב-context הרגיל ולא ב-ApplicationContext כדי לשמור על קשר ל-UI
        playSong(context, currentList.get(currentIndex));
    }

    public void playPrevious(Context context) {
        if (currentList == null || currentList.isEmpty()) return;

        if (currentIndex > 0) {
            currentIndex--;
        } else {
            currentIndex = currentList.size() - 1;
        }
        // תיקון: שלח context רגיל ולא ApplicationContext
        playSong(context, currentList.get(currentIndex));
    }

    public void playSong(Context context, Song song) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }

            // --- הוספה קריטית: איפוס ליסנרים קודמים ---
            mediaPlayer.setOnCompletionListener(null);
            mediaPlayer.setOnPreparedListener(null);

            mediaPlayer.reset();
            mediaPlayer.setDataSource(song.getSongUrl());

            // הגדרת מאזין לסיום שיר (מעבר אוטומטי)
            mediaPlayer.setOnCompletionListener(mp -> {
                Log.d("MusicDebug", "Song finished, playing next");
                playNext(context);
            });

            // הגדרת מאזין לשיר שמוכן (עדכון UI)
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (listener != null) {
                        Log.d("MusicDebug", "UI Update triggered for: " + song.getTitle());
                        listener.onSongChanged();
                    }
                });
            });

            mediaPlayer.prepareAsync();

        } catch (Exception e) {
            Log.e("MusicManager", "Error: " + e.getMessage());
        }
    }

    public void stopMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            currentIndex = -1;
        }
    }

    public interface FavoriteCallback {
        void onResult(boolean isFavorite);
    }

    public void toggleFavorite(Song song, FavoriteCallback callback) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;
        String rawId = song.getTitle() + song.getArtist();
        String docId = String.valueOf(rawId.hashCode());
        DocumentReference favRef = FirebaseFirestore.getInstance()
                .collection("users").document(userId)
                .collection("Favorites").document(docId);
        favRef.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                favRef.delete().addOnSuccessListener(aVoid -> callback.onResult(false));
            } else {
                favRef.set(song).addOnSuccessListener(aVoid -> callback.onResult(true));
            }
        });
    }
}