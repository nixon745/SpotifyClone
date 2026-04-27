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

        // שימוש ב-ApplicationContext כדי שהנגן לא יהיה קשור למסך שעלול להיסגר
        playSong(context.getApplicationContext(), currentList.get(currentIndex));
    }

    public void playPrevious(Context context) {
        if (currentList != null && currentIndex > 0) {
            currentIndex--;
            playSong(context.getApplicationContext(), currentList.get(currentIndex));
        }
    }

    public void playSong(Context context, Song song) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }

            mediaPlayer.reset();
            mediaPlayer.setDataSource(song.getSongUrl());
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());

            // מעבר אוטומטי בטוח
            mediaPlayer.setOnCompletionListener(mp -> {
                new Handler(Looper.getMainLooper()).post(() -> playNext(context));
            });

            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                // עדכון UI בטוח דרך ה-Handler הראשי
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (listener != null) {
                        try {
                            listener.onSongChanged();
                        } catch (Exception e) {
                            Log.e("MusicManager", "Update failed: " + e.getMessage());
                        }
                    }
                });
            });

            mediaPlayer.prepareAsync();

        } catch (Exception e) {
            Log.e("MusicManager", "Error playing song", e);
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