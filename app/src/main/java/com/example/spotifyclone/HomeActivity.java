package com.example.spotifyclone;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    SongAdapter songAdapter;
    ArrayList<Song> songList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    return true;
                case R.id.nav_search:
                    // TODO: לעבור למסך חיפוש
                    return true;
                case R.id.nav_library:
                    // TODO: לעבור למסך הספריה
                    return true;
            }
            return false;
        });

        // RecyclerView של שירים/אלבומים
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        songList = new ArrayList<>();
        // מוסיפים דוגמאות שירים/אלבומים
        songList.add(new Song("Song 1", R.drawable.album1));
        songList.add(new Song("Song 2", R.drawable.album2));
        songList.add(new Song("Song 3", R.drawable.album3));
        songList.add(new Song("Song 4", R.drawable.album4));
        songList.add(new Song("Song 5", R.drawable.album5));

        songAdapter = new SongAdapter(songList);
        recyclerView.setAdapter(songAdapter);
    }
}
