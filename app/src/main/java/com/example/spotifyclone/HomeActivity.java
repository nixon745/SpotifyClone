package com.example.spotifyclone;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    SongAdapter songAdapter;
    ArrayList<Song> songList;
    TextView welcomeText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();

        // בדיקה אם המשתמש מחובר
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // אם לא מחובר, חזרה ללוגין
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        welcomeText = findViewById(R.id.welcomeText);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // הצגת שם המשתמש
        String displayName = currentUser.getDisplayName();
        if (displayName != null && !displayName.isEmpty()) {
            welcomeText.setText("שלום, " + displayName + "!");
        } else {
            String email = currentUser.getEmail();
            if (email != null) {
                String username = email.split("@")[0];
                welcomeText.setText("שלום, " + username + "!");
            }
        }

        bottomNav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    return true;
                } else if (itemId == R.id.nav_search) {
                    Toast.makeText(HomeActivity.this, "חיפוש - בקרוב", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_library) {
                    Toast.makeText(HomeActivity.this, "ספריה - בקרוב", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_logout) {
                    logout();
                    return true;
                }
                return false;
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        songList = new ArrayList<>();

        songList.add(new Song(
                "The College Dropout",
                "https://upload.wikimedia.org/wikipedia/en/a/a3/Kanyewest_collegedropout.jpg"
        ));

        songList.add(new Song(
                "Late Registration",
                "https://upload.wikimedia.org/wikipedia/en/f/f4/Late_registration_cd_cover.jpg"
        ));

        songList.add(new Song(
                "Graduation",
                "https://upload.wikimedia.org/wikipedia/en/7/70/Graduation_%28album%29.jpg"
        ));

        songList.add(new Song(
                "808s & Heartbreak",
                "https://upload.wikimedia.org/wikipedia/en/f/f1/808s_%26_Heartbreak.png"
        ));

        songList.add(new Song(
                "My Beautiful Dark Twisted Fantasy",
                "https://upload.wikimedia.org/wikipedia/en/f/f0/My_Beautiful_Dark_Twisted_Fantasy.jpg"
        ));

        songList.add(new Song(
                "Yeezus",
                "https://upload.wikimedia.org/wikipedia/en/4/4b/Yeezus_album_cover.png"
        ));

        songList.add(new Song(
                "The Life of Pablo",
                "https://upload.wikimedia.org/wikipedia/en/4/4d/The_life_of_pablo_alternate.jpg"
        ));

        songAdapter = new SongAdapter(songList);
        recyclerView.setAdapter(songAdapter);
    }

    private void logout() {
        mAuth.signOut();
        Toast.makeText(this, "התנתקת בהצלחה", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}