package com.example.spotifyclone;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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

    private MusicManager musicManager;
    private LinearLayout miniPlayer;
    private TextView miniTitle, miniArtist;
    private ImageView miniImage;
    private ImageButton miniPlayBtn, miniNextBtn, miniPrevBtn;
    private BatteryReceiver batteryReceiver = new BatteryReceiver();
    private boolean isCurrentlyLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 1. קודם כל מאתחלים רשימות כדי שלא יהיה NullPointerException
        songList = new ArrayList<>();
        fullSongList = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        musicManager = MusicManager.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            goToLogin();
            return;
        }

        // 2. קישור ה-UI (וודא שה-IDs תואמים ל-XML ששלחת)
        welcomeText = findViewById(R.id.welcomeText);
        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchView);
        miniPlayer = findViewById(R.id.miniPlayer);
        miniTitle = findViewById(R.id.miniSongTitle);
        miniArtist = findViewById(R.id.miniArtistName);
        miniImage = findViewById(R.id.miniAlbumArt);
        miniPlayBtn = findViewById(R.id.miniBtnPlayPause);
        miniNextBtn = findViewById(R.id.miniBtnNext);
        miniPrevBtn = findViewById(R.id.miniBtnPrev);

        // 3. הגדרת ה-RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        songAdapter = new SongAdapter(this, songList);
        recyclerView.setAdapter(songAdapter);

        // 4. הגדרת המאזינים (Listeners)
        setupSearchListener();
        setupMiniPlayerListeners();
        setupNavigation();

        // 5. טעינת נתונים

        loadInitialData();

        try {
            registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_LOW));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (getIntent().getBooleanExtra("openSearch", false)) {
            searchView.requestFocus();
            searchView.setIconified(false);
        }
    }

    private void loadInitialData() {
        if (isCurrentlyLoading) return;
        isCurrentlyLoading = true;

        // ניקוי רשימות התחלתי
        fullSongList.clear();
        songList.clear();
        songAdapter.notifyDataSetChanged();

        String[] mainstreamArtists = {"Kanye West", "Taylor Swift", "Drake", "The Weeknd", "Omer Adam", "Travis Scott"};
        final int[] pendingRequests = {mainstreamArtists.length};
        final ArrayList<Song> tempData = new ArrayList<>();

        for (String artist : mainstreamArtists) {
            RetrofitClient.getApiService().searchSongs(artist, 50, "song").enqueue(new Callback<ITunesResponse>() {
                @Override
                public void onResponse(Call<ITunesResponse> call, Response<ITunesResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        for (ITunesResult result : response.body().getResults()) {
                            tempData.add(result.toSong());
                        }
                    }
                    // קריאה לפונקציה (שמנו לב שהשם תואם למה שכתוב כאן)
                    handleRequestFinished(pendingRequests, tempData);
                }

                @Override
                public void onFailure(Call<ITunesResponse> call, Throwable t) {
                    handleRequestFinished(pendingRequests, tempData);
                }
            });
        }
    }

    private void handleRequestFinished(int[] pending, ArrayList<Song> collectedSongs) {
        pending[0]--;

        // רק כשכל האמנים חזרו מה-API
        if (pending[0] <= 0) {
            // משיכת שירים מ-Firebase
            db.collection("Songs").get().addOnSuccessListener(querySnapshots -> {
                for (QueryDocumentSnapshot doc : querySnapshots) {
                    collectedSongs.add(doc.toObject(Song.class));
                }

                // --- סינון כפילויות ---
                ArrayList<Song> finalCleanList = new ArrayList<>();
                java.util.HashSet<String> seen = new java.util.HashSet<>();

                for (Song s : collectedSongs) {
                    // יצירת מפתח ייחודי משילוב של שם ואמן
                    String key = (s.getTitle() + s.getArtist()).toLowerCase().trim();
                    if (!seen.contains(key)) {
                        seen.add(key);
                        finalCleanList.add(s);
                    }
                }

                // --- מיון מעורבב לפי אלבום ---
                Collections.sort(finalCleanList, (s1, s2) -> {
                    String a1 = s1.getAlbumName() != null ? s1.getAlbumName() : "Unknown";
                    String a2 = s2.getAlbumName() != null ? s2.getAlbumName() : "Unknown";
                    return a1.compareToIgnoreCase(a2);
                });

                // עדכון ה-UI פעם אחת בלבד
                runOnUiThread(() -> {
                    fullSongList.clear();
                    fullSongList.addAll(finalCleanList);
                    songList.clear();
                    songList.addAll(finalCleanList);
                    songAdapter.notifyDataSetChanged();
                    isCurrentlyLoading = false;
                });
            });
        }
    }

    // פונקציית עזר למקרה של כישלון ב-Firestore
    private void processAndDisplay(ArrayList<Song> list) {
        ArrayList<Song> distinct = new ArrayList<>();
        java.util.HashSet<String> seen = new java.util.HashSet<>();
        for (Song s : list) {
            String key = (s.getTitle() + s.getArtist()).toLowerCase().replaceAll("\\s+", "");
            if (!seen.contains(key)) { seen.add(key); distinct.add(s); }
        }
        runOnUiThread(() -> {
            fullSongList.clear();
            fullSongList.addAll(distinct);
            songList.clear();
            songList.addAll(distinct);
            songAdapter.notifyDataSetChanged();
        });
    }

    private void setupSearchListener() {
        if (searchView == null) return;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                if (fullSongList == null || fullSongList.isEmpty()) return false;
                if (newText.isEmpty()) {
                    songList.clear();
                    songList.addAll(fullSongList);
                    songAdapter.notifyDataSetChanged();
                } else {
                    filterSongs(newText);
                }
                return true;
            }
        });
    }

    private void filterSongs(String query) {
        ArrayList<Song> filtered = new ArrayList<>();
        java.util.HashSet<String> seenInSearch = new java.util.HashSet<>();
        String lowerQuery = query.toLowerCase();

        for (Song song : fullSongList) {
            String title = (song.getTitle() != null) ? song.getTitle().toLowerCase() : "";
            String artist = (song.getArtist() != null) ? song.getArtist().toLowerCase() : "";

            if (title.contains(lowerQuery) || artist.contains(lowerQuery)) {
                String key = title + artist;
                if (!seenInSearch.contains(key)) {
                    seenInSearch.add(key);
                    filtered.add(song);
                }
            }
        }
        songList.clear();
        songList.addAll(filtered);
        songAdapter.notifyDataSetChanged();
    }

    private void setupMiniPlayerListeners() {
        if (miniPlayer == null) return;
        miniPlayer.setOnClickListener(v -> startActivity(new Intent(this, PlayerActivity.class)));

        if (miniPlayBtn != null) {
            miniPlayBtn.setOnClickListener(v -> {
                if (musicManager.mediaPlayer != null) {
                    if (musicManager.mediaPlayer.isPlaying()) musicManager.mediaPlayer.pause();
                    else musicManager.mediaPlayer.start();
                    updateMiniPlayerUI();
                }
            });
        }

        if (miniNextBtn != null) {
            miniNextBtn.setOnClickListener(v -> {
                musicManager.playNext(this);
                if (musicManager.mediaPlayer != null) {
                    musicManager.mediaPlayer.setOnPreparedListener(mp -> {
                        mp.start();
                        updateMiniPlayerUI();
                    });
                }
            });
        }

        if (miniPrevBtn != null) {
            miniPrevBtn.setOnClickListener(v -> {
                musicManager.playPrevious(this);
                if (musicManager.mediaPlayer != null) {
                    musicManager.mediaPlayer.setOnPreparedListener(mp -> {
                        mp.start();
                        updateMiniPlayerUI();
                    });
                }
            });
        }
    }

    public void updateMiniPlayerUI() {
        if (musicManager != null && musicManager.mediaPlayer != null && musicManager.currentIndex != -1 && miniPlayer != null) {
            miniPlayer.setVisibility(View.VISIBLE);
            Song current = musicManager.currentList.get(musicManager.currentIndex);
            if (miniTitle != null) miniTitle.setText(current.getTitle());
            if (miniArtist != null) miniArtist.setText(current.getArtist());
            if (miniImage != null) Glide.with(this).load(current.getImageUrl()).into(miniImage);
            if (miniPlayBtn != null) {
                miniPlayBtn.setImageResource(musicManager.mediaPlayer.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
            }
        } else if (miniPlayer != null) {
            miniPlayer.setVisibility(View.GONE);
        }
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                hideKeyboard(searchView); // ירוץ רק אם searchView לא null
                return true;
            } else if (itemId == R.id.nav_search) {
                showKeyboard(searchView);
                return false;
            } else if (itemId == R.id.nav_library) {
                hideKeyboard(searchView); // חשוב לסגור לפני המעבר
                startActivity(new Intent(this, FavoritesActivity.class));
                return true;
            }
            return false;
        });
    }

    // פונקציות הפורמט שביקשת:
    private void showKeyboard(SearchView sView) {
        if (sView != null) {
            sView.setIconified(false);
            sView.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(sView, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    private void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMiniPlayerUI();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);

        // טעינת השם המעודכן מה-Firestore
        loadUserName();
    }
    private void loadUserName() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            TextView welcomeText = findViewById(R.id.welcomeText); // תוודא שזה ה-ID של הטקסט שלך
                            welcomeText.setText("שלום, " + name);
                        }
                    });
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(batteryReceiver); } catch (Exception e) {}
    }
}