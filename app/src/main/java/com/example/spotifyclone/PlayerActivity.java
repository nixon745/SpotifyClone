package com.example.spotifyclone;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private boolean isPlaying = false;

    private ArrayList<Song> songList;
    private int currentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        songList = (ArrayList<Song>) getIntent().getSerializableExtra("songList");
        currentIndex = getIntent().getIntExtra("position", 0);

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

        loadSong(songList.get(currentIndex));

        btnBack.setOnClickListener(v -> finish());

        btnPlayPause.setOnClickListener(v -> {
            if (isPlaying) pauseMusic();
            else playMusic();
        });

        btnNext.setOnClickListener(v -> playNext());
        btnPrevious.setOnClickListener(v -> playPrevious());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void loadSong(Song song) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        albumTitle.setText(song.getTitle());
        albumArtist.setText(song.getArtist());

        Glide.with(this)
                .load(song.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(albumCover);

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );

            String songUrl = song.getSongUrl();

            // בדיקה האם השיר מקומי (בתוך האפליקציה) או מהאינטרנט
            if (songUrl.startsWith("android.resource")) {
                Uri uri = Uri.parse(songUrl);
                mediaPlayer.setDataSource(this, uri);
            } else {
                mediaPlayer.setDataSource(songUrl);
            }

            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                seekBar.setMax(mp.getDuration());
                totalTime.setText(formatTime(mp.getDuration()));
                playMusic();
            });

            mediaPlayer.setOnCompletionListener(mp -> playNext());

        } catch (IOException e) {
            Toast.makeText(this, "שגיאה בטעינת השיר: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void playNext() {
        if (currentIndex < songList.size() - 1) {
            currentIndex++;
            loadSong(songList.get(currentIndex));
        } else {
            Toast.makeText(this, "סוף הרשימה", Toast.LENGTH_SHORT).show();
        }
    }

    private void playPrevious() {
        if (currentIndex > 0) {
            currentIndex--;
            loadSong(songList.get(currentIndex));
        } else {
            if (mediaPlayer != null) mediaPlayer.seekTo(0);
        }
    }

    private void playMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            isPlaying = true;
            btnPlayPause.setImageResource(R.drawable.ic_pause);
            updateSeekBar();
        }
    }

    private void pauseMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            isPlaying = false;
            btnPlayPause.setImageResource(R.drawable.ic_play);
        }
    }

    private void updateSeekBar() {
        if (mediaPlayer != null && isPlaying) {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            currentTime.setText(formatTime(mediaPlayer.getCurrentPosition()));
            handler.postDelayed(this::updateSeekBar, 1000);
        }
    }

    private String formatTime(int milliseconds) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                TimeUnit.MINUTES.toSeconds(minutes);
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacksAndMessages(null);
    }
}