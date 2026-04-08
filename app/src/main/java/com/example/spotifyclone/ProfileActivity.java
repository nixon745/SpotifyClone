package com.example.spotifyclone;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private EditText editUsername, editEmail, editPassword;
    private Button btnSave, btnLogout;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        editUsername = findViewById(R.id.editUsername);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnSave = findViewById(R.id.btnSaveProfile);
        btnLogout = findViewById(R.id.btnLogout);

        loadUserData();
        setupNavigation();
        btnSave.setOnClickListener(v -> updateAllData());

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });
    }
    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_profile);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // במקום סתם לעבור, נסגור את הפעילות הזו כדי לחזור לבית המעודכן
                finish();
                return true;
            } else if (itemId == R.id.nav_library) {
                startActivity(new Intent(this, FavoritesActivity.class));
                finish(); // סוגרים כדי לא ליצור ערימת מסכים
                return true;
            }
            return true;
        });
    }
    private void loadUserData() {
        if (user == null) return;

        // נסה למשוך את האימייל מה-Firestore במקום מה-Auth כדי לראות מה נשמר ב-DB
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        editUsername.setText(doc.getString("name"));
                        editEmail.setText(doc.getString("email")); // טעינה מה-DB
                    }
                });
    }

    private void updateAllData() {
        String newName = editUsername.getText().toString().trim();
        String newEmail = editEmail.getText().toString().trim();
        String newPass = editPassword.getText().toString().trim();

        if (user == null) return;

        // 1. עדכון ב-Firestore (השם והאימייל במסד הנתונים)
        Map<String, Object> data = new HashMap<>();
        data.put("name", newName);
        data.put("email", newEmail);

        db.collection("users").document(user.getUid())
                .set(data, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> {

                    // 2. בדיקה: האם האימייל בתיבה שונה מהאימייל הנוכחי?
                    if (!newEmail.equals(user.getEmail())) {
                        user.updateEmail(newEmail)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(this, "אימייל עודכן במערכת!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // כאן בדרך כלל תקבל שגיאה אם לא התחברת לאחרונה
                                        Toast.makeText(this, "שגיאת אבטחה: יש להתנתק ולהתחבר מחדש כדי לשנות אימייל", Toast.LENGTH_LONG).show();
                                    }
                                });
                    }

                    // 3. עדכון סיסמה
                    if (!newPass.isEmpty()) {
                        user.updatePassword(newPass);
                    }

                    Toast.makeText(this, "הנתונים נשמרו!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "שגיאה ב-Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}