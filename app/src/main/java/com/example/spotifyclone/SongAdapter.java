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

    // מאפשר לעדכן את הרשימה בזמן חיפוש ב-HomeActivity
    public void setFilteredList(ArrayList<Song> filteredList) {
        this.list = filteredList;
        notifyDataSetChanged();
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

        holder.itemView.setOnClickListener(v -> {
            // תיקון: מקבל את המיקום העדכני ברשימה כפי שהיא מוצגת כרגע
            int currentPos = holder.getAdapterPosition();

            if (currentPos != RecyclerView.NO_POSITION) {
                Intent intent = new Intent(context, PlayerActivity.class);
                // שולח את הרשימה הנוכחית (אם היא מסוננת - שולח רק את התוצאות)
                intent.putExtra("songList", list);
                intent.putExtra("position", currentPos);
                context.startActivity(intent);
            }
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