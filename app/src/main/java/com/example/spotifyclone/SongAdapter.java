package com.example.spotifyclone;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private ArrayList<Song> songList;

    public SongAdapter(ArrayList<Song> songList) {
        this.songList = songList;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.title.setText(song.getTitle());
        holder.image.setImageResource(song.getImageResId());
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.songImage);
            title = itemView.findViewById(R.id.songTitle);
        }
    }
}
