package com.example.spotifyclone;

import android.content.*;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    Context context;
    ArrayList<Song> list;

    public SongAdapter(Context context, ArrayList<Song> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Song song = list.get(position);

        holder.title.setText(song.getTitle());
        holder.artist.setText(song.getArtist());

        Glide.with(context)
                .load(song.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.image);

        // כאן השינוי המשולב: במקום לשלוח String בודד, שולחים את הרשימה והאינדקס
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlayerActivity.class);
            intent.putExtra("songList", list);   // מעביר את כל הרשימה
            intent.putExtra("position", position); // מעביר את המיקום שנבחר
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, artist;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.songTitle);
            artist = itemView.findViewById(R.id.songArtist);
            image = itemView.findViewById(R.id.songImage);
        }
    }
}