package com.example.spotifyclone;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class PlayerActivity extends AppCompatActivity {

    private ImageView albumCover;
    private TextView albumTitle, albumArtist, currentTime, totalTime;
    private SeekBar seekBar;
    private ImageButton btnPlayPause, btnPrevious, btnNext, btnBack;

    private MusicManager musicManager; // משתמש במנהל במקום MediaPlayer מקומי
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        musicManager = MusicManager.getInstance();

        ArrayList<Song> incomingList = (ArrayList<Song>) getIntent().getSerializableExtra("songList");
        int incomingPos = getIntent().getIntExtra("position", 0);

        albumCover = findViewById(R.id.albumCover);
        albumTitle = findViewById(R.id.albumTitle);
        albumArtist = findViewById(R.id.albumArtist);
        currentTime = findViewById(R.id.currentTime);
        totalTime = findViewById(R.id.totalTime);
        seekBar = findViewById(R.id.seekBar);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);

        // לוגיקה: אם השיר שנבחר כבר מתנגן - אל תפסיק אותו!
        if (musicManager.currentList == null ||
                !musicManager.currentList.get(musicManager.currentIndex).getTitle().equals(incomingList.get(incomingPos).getTitle())) {

            musicManager.currentList = incomingList;
            musicManager.currentIndex = incomingPos;
            loadSong(musicManager.currentList.get(musicManager.currentIndex));
        } else {
            // רק מציג את הפרטים של השיר שכבר רץ
            displaySongDetails(musicManager.currentList.get(musicManager.currentIndex));
            updateUIState();
        }

        btnBack.setOnClickListener(v -> finish());
        btnPlayPause.setOnClickListener(v -> togglePlayPause());
        btnNext.setOnClickListener(v -> playNext());
        btnPrevious.setOnClickListener(v -> playPrevious());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && musicManager.mediaPlayer != null) musicManager.mediaPlayer.seekTo(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void loadSong(Song song) {
        displaySongDetails(song);
        musicManager.playSong(this, song);

        musicManager.mediaPlayer.setOnPreparedListener(mp -> {
            seekBar.setMax(mp.getDuration());
            totalTime.setText(formatTime(mp.getDuration()));
            mp.start();
            btnPlayPause.setImageResource(R.drawable.ic_pause);
            updateSeekBar();
        });

        musicManager.mediaPlayer.setOnCompletionListener(mp -> playNext());

        musicManager.mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(this, "שגיאה בניגון", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void displaySongDetails(Song song) {
        albumTitle.setText(song.getTitle());
        albumArtist.setText(song.getArtist());
        Glide.with(this).load(song.getImageUrl()).into(albumCover);
    }

    private void togglePlayPause() {
        if (musicManager.mediaPlayer != null) {
            if (musicManager.mediaPlayer.isPlaying()) musicManager.mediaPlayer.pause();
            else musicManager.mediaPlayer.start();
            updateUIState();
        }
    }

    private void updateUIState() {
        btnPlayPause.setImageResource(musicManager.mediaPlayer.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
        updateSeekBar();
    }

    private void playNext() {
        if (musicManager.currentIndex < musicManager.currentList.size() - 1) {
            musicManager.currentIndex++;
            loadSong(musicManager.currentList.get(musicManager.currentIndex));
        }
    }

    private void playPrevious() {
        if (musicManager.currentIndex > 0) {
            musicManager.currentIndex--;
            loadSong(musicManager.currentList.get(musicManager.currentIndex));
        }
    }

    private void updateSeekBar() {
        if (musicManager.mediaPlayer != null && musicManager.mediaPlayer.isPlaying()) {
            seekBar.setProgress(musicManager.mediaPlayer.getCurrentPosition());
            currentTime.setText(formatTime(musicManager.mediaPlayer.getCurrentPosition()));
            handler.postDelayed(this::updateSeekBar, 1000);
        }
    }

    private String formatTime(int ms) {
        return String.format("%d:%02d", TimeUnit.MILLISECONDS.toMinutes(ms),
                TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms)));
    }
}