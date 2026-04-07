package com.example.spotifyclone;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private ArrayList<Song> songList;
    private ArrayList<Song> fullSongList;
    private TextView welcomeText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SearchView searchView;

    // ה-Music Manager שלנו
    private MusicManager musicManager;
    private LinearLayout miniPlayer;
    private TextView miniTitle, miniArtist;
    private ImageView miniImage;
    private ImageButton miniPlayBtn;
    private BatteryReceiver batteryReceiver = new BatteryReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // אתחול ה-MusicManager
        musicManager = MusicManager.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            goToLogin();
            return;
        }

        // קישור אלמנטים מה-XML
        welcomeText = findViewById(R.id.welcomeText);
        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchView);
        miniPlayer = findViewById(R.id.miniPlayer);
        miniTitle = findViewById(R.id.miniSongTitle);
        miniArtist = findViewById(R.id.miniArtistName);
        miniImage = findViewById(R.id.miniAlbumArt);
        miniPlayBtn = findViewById(R.id.miniBtnPlayPause);

        setupNavigation();
        setupRecyclerView();

        welcomeText.setText("שלום, " + (currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "אורח") + "!");

        setupSearchListener();
        setupMiniPlayerListeners();

        // טעינת הנתונים (מיינסטרים + פיירבייס בסוף)
        loadInitialData();

        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_LOW));
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        songList = new ArrayList<>();
        fullSongList = new ArrayList<>();
        songAdapter = new SongAdapter(this, songList);
        recyclerView.setAdapter(songAdapter);
    }

    private void loadInitialData() {
        ArrayList<Song> combined = new ArrayList<>();
        String[] mainstreamArtists = {"Kanye West", "Taylor Swift", "Drake", "The Weeknd", "Omer Adam", "Travis Scott" , "Bad Bunny"};
        final int[] pendingRequests = {mainstreamArtists.length};

        for (String artist : mainstreamArtists) {
            RetrofitClient.getApiService().searchSongs(artist, 40, "song").enqueue(new Callback<ITunesResponse>() {
                @Override
                public void onResponse(Call<ITunesResponse> call, Response<ITunesResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        for (ITunesResult result : response.body().getResults()) {
                            combined.add(result.toSong());
                        }
                    }
                    checkAllRequestsFinished(pendingRequests, combined);
                }

                @Override
                public void onFailure(Call<ITunesResponse> call, Throwable t) {
                    checkAllRequestsFinished(pendingRequests, combined);
                }
            });
        }
    }

    private void checkAllRequestsFinished(int[] pending, ArrayList<Song> combined) {
        pending[0]--;
        if (pending[0] <= 0) {
            db.collection("Songs").get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Song fbSong = doc.toObject(Song.class);
                    // מניעת כפילויות
                    boolean exists = false;
                    for (Song s : combined) {
                        if (s.getTitle().equalsIgnoreCase(fbSong.getTitle())) { exists = true; break; }
                    }
                    if (!exists) combined.add(fbSong);
                }

                // מיון לפי אומן ואז אלבום
                combined.sort((s1, s2) -> {
                    int artComp = s1.getArtist().compareToIgnoreCase(s2.getArtist());
                    if (artComp != 0) return artComp;
                    return (s1.getAlbumName() != null ? s1.getAlbumName() : "")
                            .compareToIgnoreCase(s2.getAlbumName() != null ? s2.getAlbumName() : "");
                });

                runOnUiThread(() -> {
                    fullSongList.clear();
                    fullSongList.addAll(combined);
                    songList.clear();
                    songList.addAll(combined);
                    songAdapter.notifyDataSetChanged();
                });
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMiniPlayerUI();
    }

    private void updateMiniPlayerUI() {
        if (musicManager != null && musicManager.mediaPlayer != null && musicManager.currentIndex != -1) {
            miniPlayer.setVisibility(View.VISIBLE);
            Song current = musicManager.currentList.get(musicManager.currentIndex);
            miniTitle.setText(current.getTitle());
            miniArtist.setText(current.getArtist());
            Glide.with(this).load(current.getImageUrl()).into(miniImage);
            miniPlayBtn.setImageResource(musicManager.mediaPlayer.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
        } else {
            miniPlayer.setVisibility(View.GONE);
        }
    }

    private void setupMiniPlayerListeners() {
        miniPlayer.setOnClickListener(v -> {
            Intent intent = new Intent(this, PlayerActivity.class);
            intent.putExtra("songList", musicManager.currentList);
            intent.putExtra("position", musicManager.currentIndex);
            startActivity(intent);
        });

        miniPlayBtn.setOnClickListener(v -> {
            if (musicManager.mediaPlayer != null) {
                if (musicManager.mediaPlayer.isPlaying()) {
                    musicManager.mediaPlayer.pause();
                } else {
                    musicManager.mediaPlayer.start();
                }
                updateMiniPlayerUI();
            }
        });
    }

    // --- שאר פונקציות הניווט והחיפוש נשארות אותו דבר ---
    private void setupSearchListener() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.trim().isEmpty()) searchMusicFromAPI(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) resetToMainList();
                else filterSongs(newText);
                return true;
            }
        });
    }

    private void searchMusicFromAPI(String query) {
        RetrofitClient.getApiService().searchSongs(query, 100, "song").enqueue(new Callback<ITunesResponse>() {
            @Override
            public void onResponse(Call<ITunesResponse> call, Response<ITunesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ArrayList<Song> results = new ArrayList<>();
                    for (ITunesResult r : response.body().getResults()) results.add(r.toSong());
                    runOnUiThread(() -> {
                        songList.clear();
                        songList.addAll(results);
                        songAdapter.notifyDataSetChanged();
                    });
                }
            }
            @Override public void onFailure(Call<ITunesResponse> call, Throwable t) {}
        });
    }

    private void resetToMainList() {
        songList.clear();
        songList.addAll(fullSongList);
        songAdapter.notifyDataSetChanged();
    }

    private void filterSongs(String query) {
        ArrayList<Song> filteredList = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase().trim();

        for (Song song : fullSongList) {
            // בדיקה אם השאילתה קיימת בשם השיר או בשם האמן
            if (song.getTitle().toLowerCase().contains(lowerCaseQuery) ||
                    song.getArtist().toLowerCase().contains(lowerCaseQuery)) {
                filteredList.add(song);
            }
        }

        // עדכון האדפטר עם הרשימה המסוננת
        songAdapter.setFilteredList(filteredList);
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_logout) {
                mAuth.signOut();
                goToLogin();
                return true;
            }
            return false;
        });
    }

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(batteryReceiver);
    }
}