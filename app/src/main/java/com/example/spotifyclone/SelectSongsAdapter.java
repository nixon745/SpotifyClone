package com.example.spotifyclone;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class SelectSongsAdapter extends RecyclerView.Adapter<SelectSongsAdapter.ViewHolder> {

    private ArrayList<Song> songs;
    private ArrayList<Song> selectedSongs;
    private OnSongSelectedListener listener;

    public interface OnSongSelectedListener {
        void onSongSelected(Song song);
    }

    public SelectSongsAdapter(ArrayList<Song> songs, ArrayList<Song> selectedSongs, OnSongSelectedListener listener) {
        this.songs = songs;
        this.selectedSongs = selectedSongs;
        this.listener = listener;
    }

    // פונקציה חדשה לעדכון הרשימה בזמן חיפוש
    public void updateList(ArrayList<Song> newList) {
        this.songs = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.title.setText(song.getTitle());
        holder.artist.setText(song.getArtist());

        Glide.with(holder.itemView.getContext())
                .load(song.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.image);

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(selectedSongs.contains(song));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            listener.onSongSelected(song);
        });

        holder.itemView.setOnClickListener(v -> holder.checkBox.toggle());
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, artist;
        ImageView image;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.selectSongTitle);
            artist = itemView.findViewById(R.id.selectSongArtist);
            image = itemView.findViewById(R.id.selectSongImage);
            checkBox = itemView.findViewById(R.id.songCheckBox);
        }
    }
}