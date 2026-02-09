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
            goToLogin();
            return;
        }

        welcomeText = findViewById(R.id.welcomeText);
        recyclerView = findViewById(R.id.recyclerView);

        // הגדרת תפריט הניווט (כולל Logout)
        setupNavigation();

        // הגדרת ה-RecyclerView בגריד של 2 טורים לנצלו שטח המסך
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        songList = new ArrayList<>();
        songAdapter = new SongAdapter(this, songList);
        recyclerView.setAdapter(songAdapter);

        welcomeText.setText("שלום, " + (currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "אורח") + "!");

        // --- חשוב מאוד: שחרר את ה-Comment לשורה למטה רק פעם אחת כדי למלא את המאגר בשירים ---
        // uploadSampleSongs();

        loadUserGenresAndSongs(currentUser.getUid());
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) return true;

            // אפשרות ההתנתקות
            if (itemId == R.id.nav_logout) {
                mAuth.signOut();
                Toast.makeText(this, "התנתקת בהצלחה", Toast.LENGTH_SHORT).show();
                goToLogin();
                return true;
            }

            if (itemId == R.id.nav_search || itemId == R.id.nav_library) {
                Toast.makeText(this, "בקרוב...", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void loadUserGenresAndSongs(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // שליפת הרשימה favoriteGenres כפי שרואים בתמונה שלך
                        List<String> myGenres = (List<String>) documentSnapshot.get("favoriteGenres");

                        if (myGenres != null && !myGenres.isEmpty()) {
                            Log.d("HomeActivity", "ז'אנרים שנמצאו: " + myGenres);
                            fetchSongsByGenre(myGenres);
                        } else {
                            // אם אין ז'אנרים, נשלח אותו חזרה לבחור
                            startActivity(new Intent(HomeActivity.this, GenreActivity.class));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("HomeActivity", "שגיאה בטעינת משתמש", e));
    }

    private void fetchSongsByGenre(List<String> genres) {
        // מחפש שירים שבהם השדה "genre" מופיע ברשימת ה-genres של המשתמש
        db.collection("Songs")
                .whereIn("genre", genres)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    songList.clear();
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("HomeActivity", "לא נמצאו שירים ב-DB תואמים לז'אנרים");
                        Toast.makeText(this, "לא נמצאו שירים מתאימים לטעם שלך", Toast.LENGTH_SHORT).show();
                    } else {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Song song = document.toObject(Song.class);
                            songList.add(song);
                        }
                        songAdapter.notifyDataSetChanged();
                        Log.d("HomeActivity", "נטענו " + songList.size() + " שירים");
                    }
                })
                .addOnFailureListener(e -> Log.e("HomeActivity", "שגיאה במשיכת שירים", e));
    }

    // פונקציה למילוי ראשוני של המאגר (להריץ פעם אחת בלבד!)
    private void uploadSampleSongs() {
        List<Song> samples = new ArrayList<>();

        // --- שירים בז'אנר Pop ---
        samples.add(new Song("As It Was", "https://upload.wikimedia.org/wikipedia/en/a/a2/Harry_Styles_-_As_It_Was.png", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3", "Pop"));
        samples.add(new Song("Stay", "https://upload.wikimedia.org/wikipedia/en/0/07/The_Kid_Laroi_and_Justin_Bieber_-_Stay.png", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3", "Pop"));

        // --- שירים בז'אנר Jazz (מופיע אצלך בתמונה) ---
        samples.add(new Song("Fly Me to the Moon", "https://upload.wikimedia.org/wikipedia/en/b/b2/Frank_Sinatra_-_It_Might_as_Well_Be_Swing.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3", "Jazz"));
        samples.add(new Song("What a Wonderful World", "https://m.media-amazon.com/images/I/71R2o5-UfDL._SL1500_.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3", "Jazz"));

        // --- שירים בז'אנר Electronic (מופיע אצלך בתמונה) ---
        samples.add(new Song("Wake Me Up", "https://upload.wikimedia.org/wikipedia/en/d/da/Avicii_Wake_Me_Up_Official_Single_Cover.png", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3", "Electronic"));
        samples.add(new Song("Clarity", "https://upload.wikimedia.org/wikipedia/en/a/a5/Zedd-Clarity.png", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3", "Electronic"));

        // --- שירים בז'אנר Mizrahit (מופיע אצלך בתמונה) ---
        samples.add(new Song("קרן שמש", "https://m.media-amazon.com/images/I/41KstT18SUL._UX250_FMjpg_QL85_.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3", "Mizrahit"));
        samples.add(new Song("שקיעות אדומות", "https://m.media-amazon.com/images/I/51p6GfF9WPL._UX250_FMjpg_QL85_.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3", "Mizrahit"));

        // --- שירים בז'אנר Rock ---
        samples.add(new Song("Believer", "https://upload.wikimedia.org/wikipedia/en/5/5c/Imagine_Dragons_Believer.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3", "Rock"));
        samples.add(new Song("Numb", "https://upload.wikimedia.org/wikipedia/en/b/b9/Linkin_Park_-_Numb_CD_cover.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3", "Rock"));

        // לולאה שמעלה הכל ל-Firestore
        for (Song s : samples) {
            db.collection("Songs").add(s)
                    .addOnSuccessListener(documentReference -> Log.d("Firestore", "שיר נוסף בהצלחה: " + s.getTitle()))
                    .addOnFailureListener(e -> Log.e("Firestore", "שגיאה בהוספת שיר", e));
        }

    }
}