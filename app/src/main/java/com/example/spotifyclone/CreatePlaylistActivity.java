package com.example.spotifyclone;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreatePlaylistActivity extends AppCompatActivity {

    private EditText etPlaylistName;
    private RecyclerView rvSelectSongs;
    private Button btnSave;
    private SearchView searchSongs;

    private SelectSongsAdapter adapter;
    private ArrayList<Song> favoritesForSelection = new ArrayList<>();
    private ArrayList<Song> selectedSongs = new ArrayList<>();

    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_playlist);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        etPlaylistName = findViewById(R.id.etPlaylistName);
        rvSelectSongs = findViewById(R.id.rvSelectSongs);
        btnSave = findViewById(R.id.btnSavePlaylist);
        searchSongs = findViewById(R.id.searchSongsToSelect);

        setupRecyclerView();
        loadFavoritesOnly();

        btnSave.setOnClickListener(v -> savePlaylistToFirebase());

        // חיבור החיפוש
        if (searchSongs != null) {
            searchSongs.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) { return false; }

                @Override
                public boolean onQueryTextChange(String newText) {
                    filterSongs(newText);
                    return true;
                }
            });
        }
    }

    private void filterSongs(String text) {
        ArrayList<Song> filtered = new ArrayList<>();
        for (Song song : favoritesForSelection) {
            if (song.getTitle().toLowerCase().contains(text.toLowerCase()) ||
                    song.getArtist().toLowerCase().contains(text.toLowerCase())) {
                filtered.add(song);
            }
        }
        adapter.updateList(filtered);
    }

    private void setupRecyclerView() {
        rvSelectSongs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SelectSongsAdapter(favoritesForSelection, selectedSongs, song -> {
            if (selectedSongs.contains(song)) {
                selectedSongs.remove(song);
            } else {
                selectedSongs.add(song);
            }
        });
        rvSelectSongs.setAdapter(adapter);
    }

    private void loadFavoritesOnly() {
        if (userId == null) return;
        db.collection("users").document(userId).collection("Favorites")
                .get().addOnSuccessListener(querySnapshot -> {
                    favoritesForSelection.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        favoritesForSelection.add(doc.toObject(Song.class));
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void savePlaylistToFirebase() {
        String name = etPlaylistName.getText().toString().trim();
        if (name.isEmpty() || selectedSongs.isEmpty()) {
            Toast.makeText(this, "מלא שם ובחר שירים", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> playlistData = new HashMap<>();
        playlistData.put("name", name);
        playlistData.put("songs", selectedSongs);

        db.collection("users").document(userId).collection("Playlists")
                .add(playlistData).addOnSuccessListener(doc -> finish());
    }
}