package com.example.spotifyclone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private ArrayList<Song> songList;
    private TextView welcomeText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        welcomeText = findViewById(R.id.welcomeText);
        setupNavigation();

        // הגדרת ה-RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        songList = new ArrayList<>();

        // התיקון: הסדר הוא (Context, List) לפי ה-SongAdapter שלך
        songAdapter = new SongAdapter(this, songList);
        recyclerView.setAdapter(songAdapter);

        String displayName = currentUser.getDisplayName();
        welcomeText.setText("שלום, " + (displayName != null ? displayName : "אורח") + "!");

        // --- חשוב: להפעיל פעם אחת כדי למלא את ה-Database ואז להחזיר ל-Comment ---
        // uploadSampleSongs();

        checkUserPreferencesAndLoadSongs(currentUser.getUid());
    }

    private void checkUserPreferencesAndLoadSongs(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> myGenres = (List<String>) documentSnapshot.get("favoriteGenres");

                        if (myGenres == null || myGenres.isEmpty()) {
                            // אם אין ז'אנרים - עוברים לבחירה
                            startActivity(new Intent(HomeActivity.this, GenreActivity.class));
                            finish();
                        } else {
                            // אם יש - טוענים שירים
                            fetchSongsByGenre(myGenres);
                        }
                    } else {
                        startActivity(new Intent(HomeActivity.this, GenreActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "שגיאה בטעינת נתונים", Toast.LENGTH_SHORT).show());
    }

    private void fetchSongsByGenre(List<String> genres) {
        if (genres == null || genres.isEmpty()) {
            Log.d("HomeActivity", "User has no genres selected");
            return;
        }

        Log.d("HomeActivity", "Fetching songs for genres: " + genres.toString());

        db.collection("Songs")
                .whereIn("genre", genres) // וודא שהשדה ב-Firestore נקרא genre בכתב קטן
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    songList.clear();
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("HomeActivity", "No songs found in Firestore for these genres");
                        Toast.makeText(this, "לא נמצאו שירים בז'אנרים שבחרת", Toast.LENGTH_SHORT).show();
                    } else {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Song song = document.toObject(Song.class);
                            songList.add(song);
                        }
                        songAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching songs", e));
    }

    private void uploadSampleSongs() {
        List<Song> samples = new ArrayList<>();

        // שים לב: הז'אנרים חייבים להיות זהים לבחירות ב-GenreActivity
        samples.add(new Song("Starboy", "https://upload.wikimedia.org/wikipedia/en/3/39/The_Weeknd_-_Starboy.png", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3", "Pop"));
        samples.add(new Song("Bohemian Rhapsody", "https://upload.wikimedia.org/wikipedia/en/e/ea/Queen_Bohemian_Rhapsody.png", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3", "Rock"));
        samples.add(new Song("Sicko Mode", "https://upload.wikimedia.org/wikipedia/en/0/0b/Astroworld_by_Travis_Scott.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3", "Hip Hop"));
        samples.add(new Song("סהרה", "https://m.media-amazon.com/images/I/51p6GfF9WPL._UX250_FMjpg_QL85_.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3", "Mizrahit"));

        for (Song s : samples) {
            db.collection("Songs").add(s);
        }
        Toast.makeText(this, "המאגר עודכן!", Toast.LENGTH_SHORT).show();
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) return true;
            if (itemId == R.id.nav_search) {
                Toast.makeText(this, "חיפוש - בקרוב", Toast.LENGTH_SHORT).show();
                return true;
            }
            if (itemId == R.id.nav_library) {
                Toast.makeText(this, "ספריה - בקרוב", Toast.LENGTH_SHORT).show();
                return true;
            }
            if (itemId == R.id.nav_logout) {
                mAuth.signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }
}