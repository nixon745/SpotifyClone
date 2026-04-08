package com.example.spotifyclone;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import java.util.concurrent.TimeUnit;

public class PlayerActivity extends AppCompatActivity {

    private ImageView albumCover;
    private TextView albumTitle, albumArtist, currentTime, totalTime;
    private SeekBar seekBar;
    private ImageButton btnPlayPause, btnPrevious, btnNext, btnBack;

    private MusicManager musicManager;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        musicManager = MusicManager.getInstance();

        // אתחול UI
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

        if (musicManager.currentList != null && !musicManager.currentList.isEmpty()) {
            Song current = musicManager.currentList.get(musicManager.currentIndex);

            // אם השיר כבר מנגן - רק נעדכן UI, אם לא - נטען
            if (musicManager.mediaPlayer != null && musicManager.mediaPlayer.isPlaying()) {
                updateUI(current);
            } else {
                startNewSong(current);
            }
        }

        btnBack.setOnClickListener(v -> finish());
        btnPlayPause.setOnClickListener(v -> togglePlay());
        btnNext.setOnClickListener(v -> {
            musicManager.playNext(this);
            startNewSong(musicManager.currentList.get(musicManager.currentIndex));
        });
        btnPrevious.setOnClickListener(v -> {
            musicManager.playPrevious(this);
            startNewSong(musicManager.currentList.get(musicManager.currentIndex));
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && musicManager.mediaPlayer != null) musicManager.mediaPlayer.seekTo(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void startNewSong(Song song) {
        updateUI(song);
        musicManager.playSong(this, song);

        musicManager.mediaPlayer.setOnPreparedListener(mp -> {
            seekBar.setMax(mp.getDuration());
            totalTime.setText(formatTime(mp.getDuration()));
            mp.start();
            btnPlayPause.setImageResource(R.drawable.ic_pause);
            updateProgress();
        });

        musicManager.mediaPlayer.setOnCompletionListener(mp -> {
            musicManager.playNext(this);
            startNewSong(musicManager.currentList.get(musicManager.currentIndex));
        });
    }

    private void updateUI(Song song) {
        albumTitle.setText(song.getTitle());
        albumArtist.setText(song.getArtist());
        Glide.with(this).load(song.getImageUrl()).into(albumCover);
        if (musicManager.mediaPlayer != null) {
            btnPlayPause.setImageResource(musicManager.mediaPlayer.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
        }
    }

    private void togglePlay() {
        if (musicManager.mediaPlayer != null) {
            if (musicManager.mediaPlayer.isPlaying()) musicManager.mediaPlayer.pause();
            else musicManager.mediaPlayer.start();
            btnPlayPause.setImageResource(musicManager.mediaPlayer.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
            updateProgress();
        }
    }

    private void updateProgress() {
        if (musicManager.mediaPlayer != null && musicManager.mediaPlayer.isPlaying()) {
            seekBar.setProgress(musicManager.mediaPlayer.getCurrentPosition());
            currentTime.setText(formatTime(musicManager.mediaPlayer.getCurrentPosition()));
            handler.postDelayed(this::updateProgress, 1000);
        }
    }

    private String formatTime(int ms) {
        return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(ms),
                TimeUnit.MILLISECONDS.toSeconds(ms) % 60);
    }
}