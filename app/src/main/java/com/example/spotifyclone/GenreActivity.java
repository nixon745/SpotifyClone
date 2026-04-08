package com.example.spotifyclone;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class GenreActivity extends AppCompatActivity {

    // הגדרת משתנים התואמים ל-XML
    private GridLayout genreGridLayout;
    private MaterialButton finishBtn;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private static final int MAX_SELECTION = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre);

        // אתחול Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // קישור רכיבים מה-XML (שמות ה-ID תואמים ל-XML למעלה)
        genreGridLayout = findViewById(R.id.genreGridLayout);
        finishBtn = findViewById(R.id.finishBtn);

        setupChipSelectionLogic();

        finishBtn.setOnClickListener(v -> saveUserPreferences());
    }

    private void setupChipSelectionLogic() {
        // הגבלת בחירה ל-3 ז'אנרים בזמן אמת
        for (int i = 0; i < genreGridLayout.getChildCount(); i++) {
            View child = genreGridLayout.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked && getSelectedCount() > MAX_SELECTION) {
                        chip.setChecked(false);
                        Toast.makeText(this, "ניתן לבחור עד " + MAX_SELECTION + " ז'אנרים", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private int getSelectedCount() {
        int count = 0;
        for (int i = 0; i < genreGridLayout.getChildCount(); i++) {
            View v = genreGridLayout.getChildAt(i);
            if (v instanceof Chip && ((Chip) v).isChecked()) {
                count++;
            }
        }
        return count;
    }

    private void saveUserPreferences() {
        List<String> selectedGenres = new ArrayList<>();

        // איסוף הטקסט מהצ'יפים המסומנים
        for (int i = 0; i < genreGridLayout.getChildCount(); i++) {
            View v = genreGridLayout.getChildAt(i);
            if (v instanceof Chip) {
                Chip chip = (Chip) v;
                if (chip.isChecked()) {
                    selectedGenres.add(chip.getText().toString());
                }
            }
        }

        if (selectedGenres.isEmpty()) {
            Toast.makeText(this, "בחר לפחות ז'אנר אחד", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "שגיאה: משתמש לא מחובר", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        // עדכון Firestore
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
                    Toast.makeText(this, "שגיאה בשמירה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}