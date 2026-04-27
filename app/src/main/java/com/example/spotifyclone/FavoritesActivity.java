package com.example.spotifyclone;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Map;

public class FavoritesActivity extends AppCompatActivity {

    // רכיבי רשימות
    private RecyclerView recyclerView, playlistsRecyclerView;
    private SongAdapter favAdapter;
    private PlaylistAdapter playlistAdapter;

    // רשימות נתונים
    private ArrayList<Song> favSongList;
    private ArrayList<Song> fullFavList;
    private ArrayList<Map<String, Object>> playlistsList;

    private FirebaseFirestore db;
    private String userId;

    // רכיבי מיני פלייר
    private View miniPlayer;
    private TextView miniTitle, miniArtist;
    private ImageView miniImage;
    private ImageButton miniPlayBtn, miniNextBtn, miniPrevBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        favSongList = new ArrayList<>();
        fullFavList = new ArrayList<>();
        playlistsList = new ArrayList<>();

        // 1. הגדרת רשימת המועדפים (Grid - מה שהיה לך)
        recyclerView = findViewById(R.id.favoritesRecyclerView);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            favAdapter = new SongAdapter(this, favSongList);
            recyclerView.setAdapter(favAdapter);
        }

        // 2. הגדרת רשימת הפלייליסטים (Horizontal - החדש)
        playlistsRecyclerView = findViewById(R.id.playlistsRecyclerView);
        if (playlistsRecyclerView != null) {
            playlistsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            playlistAdapter = new PlaylistAdapter(this, playlistsList);
            playlistsRecyclerView.setAdapter(playlistAdapter);
        }

        // 3. כפתור יצירת פלייליסט
        View btnCreatePlaylist = findViewById(R.id.btnCreatePlaylist);
        if (btnCreatePlaylist != null) {
            btnCreatePlaylist.setOnClickListener(v -> {
                Intent intent = new Intent(FavoritesActivity.this, CreatePlaylistActivity.class);
                startActivity(intent);
            });
        }

        initMiniPlayer();
        setupNavigation();
        setupSearchListener();
    }

    // --- פונקציות מקוריות לחיפוש ---
    private void setupSearchListener() {
        SearchView searchView = findViewById(R.id.searchViewFavorites);
        if (searchView == null) return;

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterFavorites(newText);
                return true;
            }
        });
    }

    private void filterFavorites(String query) {
        ArrayList<Song> filtered = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase().trim();

        for (Song song : fullFavList) {
            if (song.getTitle().toLowerCase().contains(lowerCaseQuery) ||
                    song.getArtist().toLowerCase().contains(lowerCaseQuery)) {
                filtered.add(song);
            }
        }

        favSongList.clear();
        favSongList.addAll(filtered);
        favAdapter.notifyDataSetChanged();
    }

    // --- פונקציות טעינה מ-Firebase ---
    private void loadFavoritesFromFirestore() {
        if (userId == null) return;

        db.collection("users").document(userId).collection("Favorites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // חשוב: favSongList ו-fullFavList הן הרשימות היחידות שמשפיעות על התצוגה של "שירים שאהבת"
                    favSongList.clear();
                    fullFavList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Song song = doc.toObject(Song.class);
                        favSongList.add(song);
                        fullFavList.add(song);
                    }
                    favAdapter.notifyDataSetChanged();
                });
    }

    private void loadUserPlaylists() {
        if (userId == null) return;

        db.collection("users")
                .document(userId)
                .collection("Playlists")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // רשימה נפרדת לחלוטין לפלייליסטים!
                    playlistsList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        playlistsList.add(doc.getData());
                    }
                    if (playlistAdapter != null) {
                        playlistAdapter.notifyDataSetChanged();
                    }
                });
    }

    // --- ניהול נגן וניווט (המקור שלך) ---
    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(R.id.nav_library);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            View favSearch = findViewById(R.id.searchViewFavorites);

            if (itemId == R.id.nav_home) {
                hideKeyboard(favSearch);
                finish();
                return true;
            } else if (itemId == R.id.nav_search) {
                showKeyboard((SearchView) favSearch);
                return false;
            } else if (itemId == R.id.nav_library) {
                return true;
            } else if (itemId == R.id.nav_profile) {
                hideKeyboard(favSearch);
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void initMiniPlayer() {
        miniPlayer = findViewById(R.id.miniPlayer);
        miniTitle = findViewById(R.id.miniSongTitle);
        miniArtist = findViewById(R.id.miniArtistName);
        miniImage = findViewById(R.id.miniAlbumArt);
        miniPlayBtn = findViewById(R.id.miniBtnPlayPause);
        miniNextBtn = findViewById(R.id.miniBtnNext);
        miniPrevBtn = findViewById(R.id.miniBtnPrev);

        MusicManager mm = MusicManager.getInstance();

        if (miniPlayer != null) {
            miniPlayer.setOnClickListener(v -> startActivity(new Intent(this, PlayerActivity.class)));
        }

        if (miniPlayBtn != null) {
            miniPlayBtn.setOnClickListener(v -> {
                if (mm.mediaPlayer != null) {
                    if (mm.mediaPlayer.isPlaying()) mm.mediaPlayer.pause();
                    else mm.mediaPlayer.start();
                    updateMiniPlayerUI();
                }
            });
        }

        if (miniNextBtn != null) miniNextBtn.setOnClickListener(v -> mm.playNext(this));
        if (miniPrevBtn != null) miniPrevBtn.setOnClickListener(v -> mm.playPrevious(this));
    }

    public void updateMiniPlayerUI() {
        try {
            MusicManager mm = MusicManager.getInstance();
            if (miniPlayer == null) return;

            if (mm.mediaPlayer != null && mm.currentIndex != -1 && mm.currentList != null && !mm.currentList.isEmpty()) {
                Song current = mm.currentList.get(mm.currentIndex);
                if (miniTitle != null) miniTitle.setText(current.getTitle());
                if (miniArtist != null) miniArtist.setText(current.getArtist());
                if (miniImage != null) {
                    Glide.with(this).load(current.getImageUrl()).placeholder(R.drawable.ic_launcher_background).into(miniImage);
                }
                if (miniPlayBtn != null) {
                    miniPlayBtn.setImageResource(mm.mediaPlayer.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
                }
                miniPlayer.setVisibility(View.VISIBLE);
            } else {
                miniPlayer.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            android.util.Log.e("MiniPlayerError", "Error: " + e.getMessage());
        }
    }

    private void showKeyboard(SearchView sView) {
        if (sView != null) {
            sView.setIconified(false);
            sView.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(sView, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavoritesFromFirestore();
        loadUserPlaylists(); // טעינת הפלייליסטים בכל חזרה למסך

        MusicManager.getInstance().setListener(() -> runOnUiThread(this::updateMiniPlayerUI));
        updateMiniPlayerUI();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_library);
    }
}