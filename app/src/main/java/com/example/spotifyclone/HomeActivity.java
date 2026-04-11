package com.example.spotifyclone;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel; // נוסף
import android.app.NotificationManager; // נוסף
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private ArrayList<Song> songList;
    private ArrayList<Song> fullSongList;
    private TextView welcomeText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SearchView searchView;

    // GPS
    private SwitchMaterial gpsSwitch;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean isGpsModeActive = false;
    private MusicManager musicManager;
    private View miniPlayer;
    private TextView miniTitle, miniArtist;
    private ImageView miniImage;
    private ImageButton miniPlayBtn, miniNextBtn, miniPrevBtn;
    private BatteryReceiver batteryReceiver = new BatteryReceiver();
    private boolean isCurrentlyLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // --- הוספה: יצירת ערוץ התראות (חובה לאנדרואיד 8+) ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "daily_music_channel", "Music Reminder",
                    NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        // --- הוספה: בקשת הרשאה להתראות (חובה לאנדרואיד 13+) ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 102);
            }
        }

        // אתחול משתנים קיימים
        songList = new ArrayList<>();
        fullSongList = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        musicManager = MusicManager.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // בדיקת משתמש מחובר
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) { goToLogin(); return; }

        // קישור רכיבי UI
        welcomeText = findViewById(R.id.welcomeText);
        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchView);
        gpsSwitch = findViewById(R.id.gpsSwitch);

        // אתחול מיני-פלייר
        miniPlayer = findViewById(R.id.miniPlayerContainer);
        if (miniPlayer != null) {
            miniTitle = miniPlayer.findViewById(R.id.miniSongTitle);
            miniArtist = miniPlayer.findViewById(R.id.miniArtistName);
            miniImage = miniPlayer.findViewById(R.id.miniAlbumArt);
            miniPlayBtn = miniPlayer.findViewById(R.id.miniBtnPlayPause);
            miniNextBtn = miniPlayer.findViewById(R.id.miniBtnNext);
            miniPrevBtn = miniPlayer.findViewById(R.id.miniBtnPrev);
        }

        // הגדרת ה-RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        songAdapter = new SongAdapter(this, songList);
        recyclerView.setAdapter(songAdapter);

        // קריאה לפונקציות עזר
        setupGpsSwitch();
        setupSearchListener();
        setupMiniPlayerListeners();
        setupNavigation();
        loadInitialData();

        // רישום מקלט סוללה
        try {
            registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_LOW));
        } catch (Exception e) {
            android.util.Log.e("HomeActivity", "Battery receiver error: " + e.getMessage());
        }

        // --- החלק החדש: הגדרת התראה דרך AlarmReceiver ---
        android.content.SharedPreferences prefs = getSharedPreferences("MusicAppPrefs", MODE_PRIVATE);
        if (!prefs.getBoolean("isDailyAlarmSet", false)) {
            // קודם כל נשמור שביצענו, כדי שגם אם הפונקציה תיכשל זה לא ינסה שוב ושוב
            prefs.edit().putBoolean("isDailyAlarmSet", true).apply();
            setDailyMusicReminder();
        }
    }

    private void setupGpsSwitch() {
        if (gpsSwitch == null) return;
        gpsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isGpsModeActive = isChecked;

            if (isChecked) {
                songList.clear();
                songAdapter.notifyDataSetChanged();

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                } else {
                    startLocationFeature();
                }
            } else {
                stopLocationFeature();
            }
        });
    }

    private void startLocationFeature() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                updateMusicByLocation(location);
            } else {
                Toast.makeText(this, "וודא שה-GPS פועל במכשיר", Toast.LENGTH_SHORT).show();
                gpsSwitch.setChecked(false);
            }
        });
    }

    private void updateMusicByLocation(Location userLoc) {
        ArrayList<Song> nearbySongs = new ArrayList<>();
        float radiusThreshold = 5000; // 5 קילומטרים

        for (Song s : fullSongList) {
            if (s.getLat() != -1 && s.getLon() != -1) {
                Location songLoc = new Location("");
                songLoc.setLatitude(s.getLat());
                songLoc.setLongitude(s.getLon());

                float distance = userLoc.distanceTo(songLoc);

                if (distance <= radiusThreshold) {
                    nearbySongs.add(s);
                }
            }
        }

        runOnUiThread(() -> {
            songList.clear();
            if (nearbySongs.isEmpty()) {
                Toast.makeText(this, "אין שירים מיוחדים ברדיוס 5 ק\"מ", Toast.LENGTH_SHORT).show();
            } else {
                songList.addAll(nearbySongs);
                Toast.makeText(this, "נמצאו " + nearbySongs.size() + " שירים באזור שלך", Toast.LENGTH_SHORT).show();
            }
            songAdapter.notifyDataSetChanged();
            musicManager.currentList = songList; // עדכון הרשימה בנגן
        });
    }

    private void stopLocationFeature() {
        Toast.makeText(this, "חוזר לספרייה האישית שלך", Toast.LENGTH_SHORT).show();
        loadInitialData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationFeature();
            } else {
                gpsSwitch.setChecked(false);
                Toast.makeText(this, "חובה לאשר מיקום", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadInitialData() {
        if (isGpsModeActive) return;
        if (isCurrentlyLoading) return;

        isCurrentlyLoading = true;
        fullSongList.clear();
        songList.clear();
        songAdapter.notifyDataSetChanged();

        String[] mainstreamArtists = {"Tayler, The Creator" , "Kanye West", "Taylor Swift", "Drake", "The Weeknd", "Omer Adam", "Travis Scott"};
        final int[] pendingRequests = {mainstreamArtists.length};
        final ArrayList<Song> tempData = new ArrayList<>();

        for (String artist : mainstreamArtists) {
            RetrofitClient.getApiService().searchSongs(artist, 50, "song").enqueue(new Callback<ITunesResponse>() {
                @Override
                public void onResponse(Call<ITunesResponse> call, Response<ITunesResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        for (ITunesResult result : response.body().getResults()) {
                            tempData.add(result.toSong());
                        }
                    }
                    handleRequestFinished(pendingRequests, tempData);
                }
                @Override public void onFailure(Call<ITunesResponse> call, Throwable t) { handleRequestFinished(pendingRequests, tempData); }
            });
        }
    }

    private void handleRequestFinished(int[] pending, ArrayList<Song> collectedSongs) {
        pending[0]--;
        if (pending[0] <= 0) {
            db.collection("Songs").get().addOnSuccessListener(querySnapshots -> {
                for (QueryDocumentSnapshot doc : querySnapshots) {
                    collectedSongs.add(doc.toObject(Song.class));
                }

                ArrayList<Song> finalCleanList = new ArrayList<>();
                java.util.HashSet<String> seen = new java.util.HashSet<>();

                for (Song s : collectedSongs) {
                    String key = (s.getTitle() + s.getArtist()).toLowerCase().trim();
                    if (!seen.contains(key)) {
                        if (key.contains("paranoid") && key.contains("kanye")) {
                            s.setLat(32.6105);
                            s.setLon(35.1014);
                        }
                        if (key.contains("omer adam")) {
                            s.setLat(32.5000);
                            s.setLon(34.9000);
                        }
                        if (key.contains("the weeknd")) {
                            s.setLat(32.6105);
                            s.setLon(35.1014);
                        }

                        seen.add(key);
                        finalCleanList.add(s);
                    }
                }

                Collections.sort(finalCleanList, (s1, s2) -> {
                    String a1 = s1.getAlbumName() != null ? s1.getAlbumName() : "Unknown";
                    String a2 = s2.getAlbumName() != null ? s2.getAlbumName() : "Unknown";
                    return a1.compareToIgnoreCase(a2);
                });

                runOnUiThread(() -> {
                    fullSongList.clear();
                    fullSongList.addAll(finalCleanList);

                    if (!isGpsModeActive) {
                        songList.clear();
                        songList.addAll(finalCleanList);
                        songAdapter.notifyDataSetChanged();
                        musicManager.currentList = songList;
                    } else {
                        startLocationFeature();
                    }
                    isCurrentlyLoading = false;
                });
            });
        }
    }

    private void setupSearchListener() {
        if (searchView == null) return;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }

            @Override public boolean onQueryTextChange(String newText) {
                if (fullSongList == null || fullSongList.isEmpty()) return false;

                if (newText.isEmpty()) {
                    if (isGpsModeActive) {
                        startLocationFeature();
                    } else {
                        songList.clear();
                        songList.addAll(fullSongList);
                        songAdapter.notifyDataSetChanged();
                    }
                } else {
                    filterSongs(newText);
                }
                return true;
            }
        });
    }

    private void filterSongs(String query) {
        ArrayList<Song> filtered = new ArrayList<>();
        java.util.HashSet<String> seenInSearch = new java.util.HashSet<>();
        String lowerQuery = query.toLowerCase();

        ArrayList<Song> sourceToFilter = isGpsModeActive ? new ArrayList<>(songList) : new ArrayList<>(fullSongList);

        for (Song song : sourceToFilter) {
            String title = (song.getTitle() != null) ? song.getTitle().toLowerCase() : "";
            String artist = (song.getArtist() != null) ? song.getArtist().toLowerCase() : "";

            if (title.contains(lowerQuery) || artist.contains(lowerQuery)) {
                String key = (title + artist).trim();
                if (!seenInSearch.contains(key)) {
                    seenInSearch.add(key);
                    filtered.add(song);
                }
            }
        }

        songList.clear();
        songList.addAll(filtered);
        songAdapter.notifyDataSetChanged();
    }

    private void setupMiniPlayerListeners() {
        if (miniPlayer == null) return;
        miniPlayer.setOnClickListener(v -> startActivity(new Intent(this, PlayerActivity.class)));
        if (miniPlayBtn != null) {
            miniPlayBtn.setOnClickListener(v -> {
                if (musicManager.mediaPlayer != null) {
                    if (musicManager.mediaPlayer.isPlaying()) musicManager.mediaPlayer.pause();
                    else musicManager.mediaPlayer.start();
                    updateMiniPlayerUI();
                }
            });
        }
        if (miniNextBtn != null) { miniNextBtn.setOnClickListener(v -> { musicManager.currentList = songList; musicManager.playNext(this); }); }
        if (miniPrevBtn != null) { miniPrevBtn.setOnClickListener(v -> { musicManager.currentList = songList; musicManager.playPrevious(this); }); }
    }

    public void updateMiniPlayerUI() {
        runOnUiThread(() -> {
            try {
                MusicManager mm = MusicManager.getInstance();
                if (miniPlayer == null) return;
                if (mm.mediaPlayer != null && mm.currentIndex != -1 && mm.currentList != null && !mm.currentList.isEmpty()) {
                    Song current = mm.currentList.get(mm.currentIndex);
                    if (miniTitle != null) miniTitle.setText(current.getTitle());
                    if (miniArtist != null) miniArtist.setText(current.getArtist());
                    if (miniImage != null && current.getImageUrl() != null) {
                        Glide.with(this).load(current.getImageUrl()).placeholder(R.drawable.ic_launcher_background).into(miniImage);
                    }
                    if (miniPlayBtn != null) {
                        miniPlayBtn.setImageResource(mm.mediaPlayer.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
                    }
                    miniPlayer.setVisibility(View.VISIBLE);
                } else { miniPlayer.setVisibility(View.GONE); }
            } catch (Exception e) { android.util.Log.e("MiniPlayerError", "UI Update failed: " + e.getMessage()); }
        });
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) { hideKeyboard(searchView); return true; }
            else if (itemId == R.id.nav_search) { showKeyboard(searchView); return false; }
            else if (itemId == R.id.nav_library) { hideKeyboard(searchView); startActivity(new Intent(this, FavoritesActivity.class)); return true; }
            else if (itemId == R.id.nav_profile) { hideKeyboard(searchView); startActivity(new Intent(this, ProfileActivity.class)); return true; }
            return false;
        });
    }

    private void showKeyboard(SearchView sView) {
        if (sView != null) {
            sView.setIconified(false); sView.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) { imm.showSoftInput(sView, InputMethodManager.SHOW_IMPLICIT); }
        }
    }

    private void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) { imm.hideSoftInputFromWindow(view.getWindowToken(), 0); }
        }
    }

    private void goToLogin() { startActivity(new Intent(this, LoginActivity.class)); finish(); }

    @Override
    protected void onResume() {
        super.onResume();
        musicManager.setListener(() -> runOnUiThread(this::updateMiniPlayerUI));
        updateMiniPlayerUI();
        loadUserName();
    }

    private void loadUserName() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    if (welcomeText != null) welcomeText.setText("שלום, " + name);
                }
            });
        }
    }

    // הפונקציה המעודכנת שעובדת עם AlarmReceiver
    private void setDailyMusicReminder() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction("com.example.spotifyclone.START_ALARM");

        // ביטול התראות קודמות למניעת כפילויות
        PendingIntent oldPendingIntent = PendingIntent.getBroadcast(this, 1, intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (oldPendingIntent != null && alarmManager != null) {
            alarmManager.cancel(oldPendingIntent);
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // בדיקת הרשאה לאנדרואיד 14+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intentPerm = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intentPerm);
                return;
            }
        }

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 20); // שעה 20:00
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);


        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }

        if (alarmManager != null) {
            // שימוש ב-setAlarmClock - השיטה הכי אמינה למניעת הקפצה מיידית
            AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pendingIntent);
            alarmManager.setAlarmClock(info, pendingIntent);

            android.util.Log.d("ALARM_DEBUG", "התראה נקבעה לזמן עתידי: " + calendar.getTime());
        }
    }

    @Override protected void onDestroy() { super.onDestroy(); try { unregisterReceiver(batteryReceiver); } catch (Exception e) {} }
}