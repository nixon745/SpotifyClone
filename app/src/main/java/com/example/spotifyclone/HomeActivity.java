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
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
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

        // --- POP (20 שירים) ---
        samples.add(new Song("Flowers", "Miley Cyrus", "https://upload.wikimedia.org/wikipedia/en/a/a4/Miley_Cyrus_-_Flowers.png", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3", "Pop"));
        samples.add(new Song("Anti-Hero", "Taylor Swift", "https://upload.wikimedia.org/wikipedia/en/9/9f/Taylor_Swift_-_Midnights.png", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3", "Pop"));
        samples.add(new Song("As It Was", "Harry Styles", "https://upload.wikimedia.org/wikipedia/en/a/a2/Harry_Styles_-_As_It_Was.png", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3", "Pop"));
        samples.add(new Song("Stay", "The Kid LAROI", "https://upload.wikimedia.org/wikipedia/en/0/07/The_Kid_Laroi_and_Justin_Bieber_-_Stay.png", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3", "Pop"));
        samples.add(new Song("Blinding Lights", "The Weekend", "https://upload.wikimedia.org/wikipedia/en/e/e6/The_Weeknd_-_Blinding_Lights.png", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3", "Pop"));
        samples.add(new Song("Levitating", "Dua Lipa", "https://upload.wikimedia.org/wikipedia/en/b/ba/Dua_Lipa_-_Levitating.png", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3", "Pop"));
        samples.add(new Song("Bad Guy", "Billie Eilish", "https://upload.wikimedia.org/wikipedia/en/5/5a/Billie_Eilish_-_Bad_Guy.png", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3", "Pop"));
        samples.add(new Song("Shape of You", "Ed Sheeran", "https://upload.wikimedia.org/wikipedia/en/b/b4/Shape_Of_You_%28Official_Single_Cover%29_by_Ed_Sheeran.png", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3", "Pop"));
        samples.add(new Song("Havana", "Camila Cabello", "https://upload.wikimedia.org/wikipedia/en/9/93/Camila_Cabello_-_Havana.png", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3", "Pop"));
        samples.add(new Song("Uptown Funk", "Bruno Mars", "https://upload.wikimedia.org/wikipedia/en/8/8f/Mark_Ronson_-_Uptown_Funk_%28feat._Bruno_Mars%29_%28Official_Single_Cover%29.png", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3", "Pop"));
        // הוסף עוד 10 שירים דומים ל-Pop...

        // --- ROCK (20 שירים) ---
        samples.add(new Song("Bohemian Rhapsody", "Queen", "https://upload.wikimedia.org/wikipedia/en/9/9f/A_Night_at_the_Opera.png", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3", "Rock"));
        samples.add(new Song("Smells Like Teen Spirit", "Nirvana", "https://upload.wikimedia.org/wikipedia/en/b/b7/NirvanaNevermindalbumcover.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3", "Rock"));
        samples.add(new Song("Hotel California", "Eagles", "https://upload.wikimedia.org/wikipedia/en/4/49/Hotelcalifornia.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-13.mp3", "Rock"));
        samples.add(new Song("Back In Black", "AC/DC", "https://upload.wikimedia.org/wikipedia/en/b/be/ACODCBackinBlack.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-14.mp3", "Rock"));
        samples.add(new Song("Believer", "Imagine Dragons", "https://upload.wikimedia.org/wikipedia/en/5/5c/Imagine_Dragons_Believer.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-15.mp3", "Rock"));
        samples.add(new Song("Numb", "Linkin Park", "https://upload.wikimedia.org/wikipedia/en/b/b9/Linkin_Park_-_Numb_CD_cover.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-16.mp3", "Rock"));
        samples.add(new Song("Wonderwall", "Oasis", "https://upload.wikimedia.org/wikipedia/en/1/1a/Oasis_-_Wonderwall_single_cover.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-17.mp3", "Rock"));
        samples.add(new Song("Sweet Child O' Mine", "Guns N' Roses", "https://upload.wikimedia.org/wikipedia/en/b/b3/Guns_N%27_Roses_-_Sweet_Child_O%27_Mine.png", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-18.mp3", "Rock"));
        samples.add(new Song("Highway to Hell", "AC/DC", "https://upload.wikimedia.org/wikipedia/en/a/ac/Acdc_Highway_to_Hell.JPG", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-19.mp3", "Rock"));
        samples.add(new Song("Dream On", "Aerosmith", "https://upload.wikimedia.org/wikipedia/en/b/be/Aerosmith_-_Dream_On_single.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-20.mp3", "Rock"));
        // הוסף עוד 10 שירים דומים ל-Rock...

        // --- MIZRAHIT (20 שירים) ---
        samples.add(new Song("סהרה", "טונה", "https://m.media-amazon.com/images/I/41-S6Y6K-PL.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-21.mp3", "Mizrahit"));
        samples.add(new Song("קרן שמש", "בניה ברבי", "https://m.media-amazon.com/images/I/41KstT18SUL.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-22.mp3", "Mizrahit"));
        samples.add(new Song("שקיעות אדומות", "עומר אדם", "https://m.media-amazon.com/images/I/51p6GfF9WPL.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-23.mp3", "Mizrahit"));
        samples.add(new Song("פנתרה", "נועה קירל", "https://m.media-amazon.com/images/I/51A6e+mI6mL.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-24.mp3", "Mizrahit"));
        samples.add(new Song("החיים שלנו תותים", "חנן בן ארי", "https://m.media-amazon.com/images/I/61iVvR6V2CL.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-25.mp3", "Mizrahit"));
        samples.add(new Song("מיליון דולר", "נועה קירל", "https://m.media-amazon.com/images/I/51p6GfF9WPL.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-26.mp3", "Mizrahit"));
        samples.add(new Song("בסוף כל יום", "איל גולן", "https://m.media-amazon.com/images/I/41-S6Y6K-PL.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-27.mp3", "Mizrahit"));
        samples.add(new Song("צבעים", "גיל ויין", "https://m.media-amazon.com/images/I/41KstT18SUL.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-28.mp3", "Mizrahit"));
        samples.add(new Song("תל אביב", "עומר אדם", "https://m.media-amazon.com/images/I/51p6GfF9WPL.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-29.mp3", "Mizrahit"));
        samples.add(new Song("מועבט", "עדן בן זקן", "https://m.media-amazon.com/images/I/61iVvR6V2CL.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-30.mp3", "Mizrahit"));
        // הוסף עוד 10 שירים דומים ל-Mizrahit...

        // --- JAZZ (20 שירים) ---
        samples.add(new Song("Fly Me to the Moon", "Frank Sinatra", "https://upload.wikimedia.org/wikipedia/en/b/b2/Frank_Sinatra_-_It_Might_as_Well_Be_Swing.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-31.mp3", "Jazz"));
        samples.add(new Song("What a Wonderful World", "Louis Armstrong", "https://m.media-amazon.com/images/I/71R2o5-UfDL.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-32.mp3", "Jazz"));
        samples.add(new Song("Take Five", "Dave Brubeck", "https://upload.wikimedia.org/wikipedia/en/a/a8/Time_out_album_cover.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-33.mp3", "Jazz"));
        samples.add(new Song("Feeling Good", "Nina Simone", "https://m.media-amazon.com/images/I/71R2o5-UfDL.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-34.mp3", "Jazz"));
        samples.add(new Song("My Funny Valentine", "Chet Baker", "https://m.media-amazon.com/images/I/81I-uKkXzPL.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-35.mp3", "Jazz"));
        samples.add(new Song("Blue Train", "John Coltrane", "https://upload.wikimedia.org/wikipedia/en/b/b6/Blue_Train_album_cover.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-36.mp3", "Jazz"));
        samples.add(new Song("Autumn Leaves", "Miles Davis", "https://upload.wikimedia.org/wikipedia/en/1/1a/Miles_Davis_-_Autumn_Leaves.png", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-37.mp3", "Jazz"));
        samples.add(new Song("So What", "Miles Davis", "https://upload.wikimedia.org/wikipedia/en/9/9c/Kind_of_Blue_album_cover.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-38.mp3", "Jazz"));
        samples.add(new Song("Strange Fruit", "Billie Holiday", "https://upload.wikimedia.org/wikipedia/en/1/1a/Strange_Fruit_cover.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-39.mp3", "Jazz"));
        samples.add(new Song("Summertime", "Ella Fitzgerald", "https://upload.wikimedia.org/wikipedia/en/b/b3/Ella_Fitzgerald_-_Summertime.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-40.mp3", "Jazz"));
        // הוסף עוד 10 שירים דומים ל-Jazz...

        // --- ELECTRONIC (20 שירים) ---
        samples.add(new Song("Wake Me Up", "Avicii", "https://upload.wikimedia.org/wikipedia/en/d/da/Avicii_Wake_Me_Up_Official_Single_Cover.png", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-41.mp3", "Electronic"));
        samples.add(new Song("Clarity", "Zedd", "https://upload.wikimedia.org/wikipedia/en/a/a5/Zedd-Clarity.png", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-42.mp3", "Electronic"));
        samples.add(new Song("Titanium", "David Guetta", "https://upload.wikimedia.org/wikipedia/en/d/de/David_Guetta_-_Titanium.png", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-43.mp3", "Electronic"));
        samples.add(new Song("Animals", "Martin Garrix", "https://upload.wikimedia.org/wikipedia/en/a/a2/Martin_Garrix_-_Animals.png", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-44.mp3", "Electronic"));
        samples.add(new Song("Lean On", "Major Lazer", "https://upload.wikimedia.org/wikipedia/en/e/ed/Major_Lazer_and_DJ_Snake_-_Lean_On_%28feat._M%C3%98%29.png", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-45.mp3", "Electronic"));
        samples.add(new Song("Strobe", "deadmau5", "https://upload.wikimedia.org/wikipedia/en/2/2a/Deadmau5_Strobe.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-46.mp3", "Electronic"));
        samples.add(new Song("Midnight City", "M83", "https://upload.wikimedia.org/wikipedia/en/5/5a/M83_Midnight_City.png", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-47.mp3", "Electronic"));
        samples.add(new Song("Get Lucky", "Daft Punk", "https://upload.wikimedia.org/wikipedia/en/a/a7/Daft_Punk_-_Get_Lucky.png", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-48.mp3", "Electronic"));
        samples.add(new Song("Alone", "Marshmello", "https://upload.wikimedia.org/wikipedia/en/a/a5/Marshmello_Alone.jpg", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-49.mp3", "Electronic"));
        samples.add(new Song("Scary Monsters", "Skrillex", "https://upload.wikimedia.org/wikipedia/en/e/ed/Skrillex_-_Scary_Monsters_and_Nice_Sprites.png", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-50.mp3", "Electronic"));
        // הוסף עוד 10 שירים דומים ל-Electronic...

        // לולאת העלאה ל-Firestore
        for (Song s : samples) {
            db.collection("Songs").add(s)
                    .addOnSuccessListener(documentReference -> Log.d("Firestore", "נוסף: " + s.getTitle()))
                    .addOnFailureListener(e -> Log.e("Firestore", "שגיאה: " + e.getMessage()));
        }
    }
}