package com.example.spotifyclone;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class GenreActivity extends AppCompatActivity {

    ChipGroup genreChipGroup;
    Button finishBtn;
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        genreChipGroup = findViewById(R.id.genreChipGroup);
        finishBtn = findViewById(R.id.finishBtn);

        finishBtn.setOnClickListener(v -> saveUserPreferences());
    }

    private void saveUserPreferences() {
        List<String> selectedGenres = new ArrayList<>();

        // מעבר על כל הצ'יפים ובדיקה מה נבחר
        for (int i = 0; i < genreChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) genreChipGroup.getChildAt(i);
            if (chip.isChecked()) {
                selectedGenres.add(chip.getText().toString());
            }
        }

        if (selectedGenres.size() < 1) {
            Toast.makeText(this, "בחר לפחות ז'אנר אחד", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        // עדכון ב-Firestore
        finishBtn.setEnabled(false);
        finishBtn.setText("שומר...");

        db.collection("users").document(uid)
                .update("favoriteGenres", selectedGenres)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "העדפות נשמרו!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(GenreActivity.this, HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    finishBtn.setEnabled(true);
                    finishBtn.setText("סיום והמשך");
                    Toast.makeText(this, "שגיאה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
