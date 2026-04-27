package com.example.spotifyclone;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Map<String, Object>> playlists;

    public PlaylistAdapter(Context context, ArrayList<Map<String, Object>> playlists) {
        this.context = context;
        this.playlists = playlists;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_playlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> playlist = playlists.get(position);
        String name = (String) playlist.get("name");
        holder.nameTv.setText(name);

        holder.itemView.setOnClickListener(v -> {
            // חילוץ רשימת השירים מתוך המפה של Firebase
            ArrayList<HashMap<String, Object>> songMaps = (ArrayList<HashMap<String, Object>>) playlist.get("songs");
            ArrayList<Song> playlistSongs = new ArrayList<>();

            if (songMaps != null) {
                for (HashMap<String, Object> map : songMaps) {
                    Song s = new Song(
                            (String)map.get("title"),
                            (String)map.get("artist"),
                            (String)map.get("imageUrl"),
                            (String)map.get("songUrl"),
                            (String)map.get("genre"),
                            (String)map.get("albumName")
                    );
                    playlistSongs.add(s);
                }
            }

            if (!playlistSongs.isEmpty()) {
                // עדכון הנגן והתחלת נגינה מהשיר הראשון
                MusicManager mm = MusicManager.getInstance();
                mm.currentList = playlistSongs;
                mm.currentIndex = 0;
                mm.playSong(context, playlistSongs.get(0));

                context.startActivity(new Intent(context, PlayerActivity.class));
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTv;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.playlistNameTv);
        }
    }
}