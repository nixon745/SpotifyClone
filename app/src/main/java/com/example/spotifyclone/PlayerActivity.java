package com.example.spotifyclone;

import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import java.util.concurrent.TimeUnit;

public class PlayerActivity extends AppCompatActivity implements MusicManager.MusicUpdateListener {

    private ImageView albumCover;
    private TextView albumTitle, albumArtist, currentTime, totalTime;
    private SeekBar seekBar;
    private ImageButton btnPlayPause, btnPrevious, btnNext, btnBack;
    private RelativeLayout playerRootLayout; // הוספנו את זה לרקע

    private MusicManager musicManager;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        musicManager = MusicManager.getInstance();
        musicManager.setListener(this);

        // אתחול UI
        playerRootLayout = findViewById(R.id.playerRootLayout);
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

            // עדכון UI ראשוני
            updateUI(current);

            if (musicManager.mediaPlayer != null && musicManager.mediaPlayer.isPlaying()) {
                setupSeekBar();
                updateProgress();
            } else {
                startNewSong(current);
            }
        }

        btnBack.setOnClickListener(v -> finish());
        btnPlayPause.setOnClickListener(v -> togglePlay());
        btnNext.setOnClickListener(v -> musicManager.playNext(this));
        btnPrevious.setOnClickListener(v -> musicManager.playPrevious(this));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && musicManager.mediaPlayer != null) {
                    musicManager.mediaPlayer.seekTo(progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void startNewSong(Song song) {
        updateUI(song);
        musicManager.playSong(this, song);
    }

    // הגרסה האחת והיחידה של updateUI
    private void updateUI(Song song) {
        albumTitle.setText(song.getTitle());
        albumArtist.setText(song.getArtist());
        albumTitle.setSelected(true);

        albumArtist.setText(song.getArtist());
        // שימוש ב-Palette לחילוץ צבע ועיצוב הרקע
        Glide.with(this)
                .asBitmap()
                .load(song.getImageUrl())
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        albumCover.setImageBitmap(resource);

                        Palette.from(resource).generate(palette -> {
                            if (palette != null) {
                                // מחלץ צבע דומיננטי או אפור כהה כברירת מחדל
                                int dominantColor = palette.getDominantColor(0xFF222222);

                                // יצירת ה-Gradient (מדורג) מהצבע לשחור
                                GradientDrawable gd = new GradientDrawable(
                                        GradientDrawable.Orientation.TOP_BOTTOM,
                                        new int[]{dominantColor, 0xFF121212}
                                );
                                playerRootLayout.setBackground(gd);
                            }
                        });
                    }

                    @Override
                    public void onLoadCleared(@Nullable android.graphics.drawable.Drawable placeholder) {}
                });

        if (musicManager.mediaPlayer != null) {
            btnPlayPause.setImageResource(musicManager.mediaPlayer.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
        }
    }

    private void setupSeekBar() {
        if (musicManager.mediaPlayer != null) {
            seekBar.setMax(musicManager.mediaPlayer.getDuration());
            totalTime.setText(formatTime(musicManager.mediaPlayer.getDuration()));
        }
    }

    private void togglePlay() {
        if (musicManager.mediaPlayer != null) {
            if (musicManager.mediaPlayer.isPlaying()) {
                musicManager.mediaPlayer.pause();
            } else {
                musicManager.mediaPlayer.start();
            }
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

    @Override
    public void onSongChanged() {
        runOnUiThread(() -> {
            if (!isFinishing() && musicManager.currentIndex != -1) {
                Song current = musicManager.currentList.get(musicManager.currentIndex);
                updateUI(current);
                setupSeekBar();
                updateProgress();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        musicManager.setListener(null);
        handler.removeCallbacksAndMessages(null);
    }
}