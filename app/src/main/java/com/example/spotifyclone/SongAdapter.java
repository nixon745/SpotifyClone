package com.example.spotifyclone;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Song> list;
    private MusicManager musicManager;

    public SongAdapter(Context context, ArrayList<Song> list) {
        this.context = context;
        this.list = list;
        this.musicManager = MusicManager.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = list.get(position);

        holder.title.setText(song.getTitle());
        holder.artist.setText(song.getArtist());
        Glide.with(context).load(song.getImageUrl()).into(holder.image);

        checkIfFavorite(song, holder.btnFavorite);

        holder.btnFavorite.setOnClickListener(v -> {
            musicManager.toggleFavorite(song, isFavorite -> {
                if (isFavorite) {
                    holder.btnFavorite.setImageResource(R.drawable.ic_heart_full);
                    holder.btnFavorite.setTag("full");
                } else {
                    holder.btnFavorite.setImageResource(R.drawable.ic_heart_empty);
                    holder.btnFavorite.setTag("empty");
                }
            });
        });

        // --- התיקון כאן: לחיצה על השורה לנגינה ---
        // --- התיקון המעודכן: לחיצה על השורה לנגינה ---
        holder.itemView.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                // 1. עדכון הרשימה והאינדקס ב-MusicManager
                musicManager.currentList = new ArrayList<>(list);
                musicManager.currentIndex = currentPos;

                // 2. פקודה לנגן את השיר
                musicManager.playSong(context, list.get(currentPos));

                // --- כאן התיקון הקריטי שמונע את הבעיה שבתמונה ---
                // אנחנו אומרים ל-HomeActivity לעדכן את המיני-פלייר שלו עכשיו
                if (context instanceof HomeActivity) {
                    ((HomeActivity) context).updateMiniPlayerUI();
                } else if (context instanceof FavoritesActivity) {
                    ((FavoritesActivity) context).updateMiniPlayerUI();
                }
                // ------------------------------------------------

                // 3. מעבר למסך הנגן
                context.startActivity(new Intent(context, PlayerActivity.class));
            }
        });
    }

    // בתוך SongAdapter.java
    private void checkIfFavorite(Song song, ImageButton btn) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        String rawId = song.getTitle() + song.getArtist();
        String docId = String.valueOf(rawId.hashCode());

        FirebaseFirestore.getInstance()
                .collection("users") // שינוי מ-Users ל-users (אות קטנה)
                .document(userId)
                .collection("Favorites")
                .document(docId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        btn.setImageResource(R.drawable.ic_heart_full);
                        btn.setTag("full");
                    } else {
                        btn.setImageResource(R.drawable.ic_heart_empty);
                        btn.setTag("empty");
                    }
                });
    }

    public void setFilteredList(ArrayList<Song> filteredList) {
        this.list = filteredList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, artist;
        ImageView image;
        ImageButton btnFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.songTitle);
            artist = itemView.findViewById(R.id.songArtist);
            image = itemView.findViewById(R.id.songImage);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}