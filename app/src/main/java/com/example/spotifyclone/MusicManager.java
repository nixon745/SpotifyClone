package com.example.spotifyclone;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

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

    public void playNext(Context context) {
        if (currentList != null && currentIndex < currentList.size() - 1) {
            currentIndex++;
            playSong(context, currentList.get(currentIndex));
        }
    }

    public void playPrevious(Context context) {
        if (currentList != null && currentIndex > 0) {
            currentIndex--;
            playSong(context, currentList.get(currentIndex));
        }
    }

    public void playSong(Context context, Song song) {
        // בדיקה: אם זה אותו שיר שכבר מנגן, אל תעשה כלום
        // (אופציונלי: מחק את ה-if הזה אם אתה רוצה שלחיצה תמיד תתחיל מהתחלה)
    /*
    if (mediaPlayer != null && currentIndex != -1 &&
        currentList.get(currentIndex).getSongUrl().equals(song.getSongUrl())) {
        if (!mediaPlayer.isPlaying()) mediaPlayer.start();
        return;
    }
    */

        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }

            mediaPlayer.reset(); // מאפס את הנגן מהשיר הקודם
            mediaPlayer.setDataSource(song.getSongUrl());

            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());

            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();

                // עדכון המיני פלייר ב-HomeActivity או FavoritesActivity אם הן פתוחות
                if (context instanceof HomeActivity) {
                    ((HomeActivity) context).updateMiniPlayerUI();
                } else if (context instanceof FavoritesActivity) {
                    ((FavoritesActivity) context).updateMiniPlayerUI();
                }
            });

            mediaPlayer.prepareAsync(); // טעינה מהאינטרנט

        } catch (Exception e) {
            Log.e("MusicManager", "Error playing song", e);
        }
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

    public interface FavoriteCallback {
        void onResult(boolean isFavorite);
    }

    public void toggleFavorite(Song song, FavoriteCallback callback) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        String rawId = song.getTitle() + song.getArtist();
        String docId = String.valueOf(rawId.hashCode());

        // שימוש ב-users (אות קטנה)
        DocumentReference favRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("Favorites")
                .document(docId);

        favRef.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                favRef.delete().addOnSuccessListener(aVoid -> callback.onResult(false));
            } else {
                favRef.set(song).addOnSuccessListener(aVoid -> callback.onResult(true));
            }
        });
    }
}