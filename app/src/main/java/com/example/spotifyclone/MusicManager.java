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

    // --- הוספה: מנגנון עדכון למסכים ---
    public interface MusicUpdateListener {
        void onSongChanged();
    }
    private MusicUpdateListener listener;
    public void setListener(MusicUpdateListener listener) {
        this.listener = listener;
    }
    // --------------------------------

    public static MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }

    public void playNext(Context context) {
        if (currentList == null || currentList.isEmpty()) return;

        if (currentIndex < currentList.size() - 1) {
            // יש שיר הבא - עוברים אליו כרגיל
            currentIndex++;
            playSong(context, currentList.get(currentIndex));
        } else {
            // הגענו לשיר האחרון בתור - נפעיל אותו מחדש (Loop)
            // אפשר גם לאפס את האינדקס ל-0 אם אתה רוצה שיחזור לתחילת הרשימה
            currentIndex = currentIndex; // נשארים על אותו אינדקס
            playSong(context, currentList.get(currentIndex));

            Log.d("MusicManager", "Last song in queue, restarting for loop.");
        }

        // עדכון ה-UI
        if (listener != null) {
            listener.onSongChanged();
        }
    }

    public void playPrevious(Context context) {
        if (currentList != null && currentIndex > 0) {
            currentIndex--;
            playSong(context, currentList.get(currentIndex));
            // הוספה: עדכון ה-UI
            if (listener != null) listener.onSongChanged();
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

            // --- הוספת מנגנון לופ לסוף התור ---
            mediaPlayer.setOnCompletionListener(mp -> {
                // בודקים אם אנחנו בשיר האחרון ברשימה
                if (currentList != null && currentIndex == currentList.size() - 1) {
                    Log.d("MusicManager", "הגענו לשיר האחרון - מפעיל לופ");
                    mp.seekTo(0); // חוזר לתחילת השיר
                    mp.start();   // מנגן שוב
                } else {
                    // אם יש עוד שירים, עובר לשיר הבא אוטומטית
                    playNext(context);
                }
            });
            // ---------------------------------

            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();

                if (listener != null) listener.onSongChanged();

                if (context instanceof HomeActivity) {
                    ((HomeActivity) context).updateMiniPlayerUI();
                } else if (context instanceof FavoritesActivity) {
                    ((FavoritesActivity) context).updateMiniPlayerUI();
                }
            });

            mediaPlayer.prepareAsync();

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