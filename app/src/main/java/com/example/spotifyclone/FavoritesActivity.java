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

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SongAdapter favAdapter;
    private ArrayList<Song> favSongList;
    private FirebaseFirestore db;
    private String userId;

    // משתנים למיני פלייר
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

        // 1. אתחול הרשימה
        favSongList = new ArrayList<>();

        // תיקון ה-ID: משתמשים בשם המדויק מה-XML שלך
        recyclerView = findViewById(R.id.favoritesRecyclerView);

        if (recyclerView != null) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            favAdapter = new SongAdapter(this, favSongList);
            recyclerView.setAdapter(favAdapter);
        }

        // 2. אתחול המיני פלייר (ה-IDs מגיעים מה-include של ה-layout_mini_player)
        initMiniPlayer();

        // 3. הגדרת תפריט ניווט תחתון
        setupNavigation();
    }

    private void initMiniPlayer() {
        miniPlayer = findViewById(R.id.miniPlayer);
        miniTitle = findViewById(R.id.miniSongTitle);
        miniArtist = findViewById(R.id.miniArtistName);
        miniImage = findViewById(R.id.miniAlbumArt);
        miniPlayBtn = findViewById(R.id.miniBtnPlayPause);
        miniNextBtn = findViewById(R.id.miniBtnNext);
        miniPrevBtn = findViewById(R.id.miniBtnPrev);

        // הגדרת לחיצות למיני פלייר
        if (miniPlayBtn != null) {
            miniPlayBtn.setOnClickListener(v -> {
                MusicManager mm = MusicManager.getInstance();
                if (mm.mediaPlayer != null) {
                    if (mm.mediaPlayer.isPlaying()) mm.mediaPlayer.pause();
                    else mm.mediaPlayer.start();
                    updateMiniPlayerUI();
                }
            });
        }
    }

    public void updateMiniPlayerUI() {
        MusicManager mm = MusicManager.getInstance();
        if (mm.mediaPlayer != null && mm.currentIndex != -1 && mm.currentList != null) {
            miniPlayer.setVisibility(View.VISIBLE);
            Song current = mm.currentList.get(mm.currentIndex);
            miniTitle.setText(current.getTitle());
            miniArtist.setText(current.getArtist());
            Glide.with(this).load(current.getImageUrl()).into(miniImage);
            miniPlayBtn.setImageResource(mm.mediaPlayer.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
        } else {
            miniPlayer.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavoritesFromFirestore();
        updateMiniPlayerUI();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_library);
    }

    private void loadFavoritesFromFirestore() {
        if (userId == null) return;

        db.collection("users")
                .document(userId)
                .collection("Favorites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    favSongList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Song song = doc.toObject(Song.class);
                        favSongList.add(song);
                    }
                    favAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "שגיאה בטעינה", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_library);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // מציאת ה-SearchView מה-XML
            androidx.appcompat.widget.SearchView favSearch = findViewById(R.id.searchViewFavorites);

            if (itemId == R.id.nav_home) {
                if (favSearch != null) hideKeyboard(favSearch);
                finish(); // חוזר ל-Home
                return true;
            } else if (itemId == R.id.nav_search) {
                if (favSearch != null) showKeyboard(favSearch);
                return false;
            } else if (itemId == R.id.nav_library) {
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }
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
}