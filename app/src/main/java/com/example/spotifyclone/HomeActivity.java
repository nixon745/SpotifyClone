package com.example.spotifyclone;

import android.os.Bundle;
import android.widget.Toast;
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

       BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        recyclerView = findViewById(R.id.recyclerView);

        bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    Toast.makeText(this, "בית", Toast.LENGTH_SHORT).show();
                   return true;
                case R.id.nav_search:
                    Toast.makeText(this, "חיפוש", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.nav_library:
                   Toast.makeText(this, "ספריה", Toast.LENGTH_SHORT).show();
                    return true;
            }
            return false;
        });

        // RecyclerView אופקי
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

       songList = new ArrayList<>();
        // דוגמה של אלבומים (תוכל להחליף באמיתיים)
        songList.add(new Song("אלבום 1", R.drawable.ic_home));
        songList.add(new Song("אלבום 2", R.drawable.ic_search));
        songList.add(new Song("אלבום 3", R.drawable.ic_library));

        songAdapter = new SongAdapter(songList);
        recyclerView.setAdapter(songAdapter);
    }
}
