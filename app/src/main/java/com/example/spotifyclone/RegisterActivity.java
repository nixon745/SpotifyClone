package com.example.spotifyclone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText email, username, password;
    Button registerBtn;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // הוספת Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // אתחול Firestore

        email = findViewById(R.id.registerEmail);
        username = findViewById(R.id.registerUsername);
        password = findViewById(R.id.registerPassword);
        registerBtn = findViewById(R.id.registerBtn);

        registerBtn.setOnClickListener(v -> checkRegister());
    }

    private void checkRegister() {
        String emailText = email.getText().toString().trim();
        String usernameText = username.getText().toString().trim();
        String passwordText = password.getText().toString().trim();

        if (emailText.isEmpty() || usernameText.isEmpty() || passwordText.isEmpty()) {
            Toast.makeText(this, "יש למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            Toast.makeText(this, "אימייל לא תקין", Toast.LENGTH_SHORT).show();
            return;
        }

        if (passwordText.length() < 6) {
            Toast.makeText(this, "הסיסמה חייבת להכיל לפחות 6 תווים", Toast.LENGTH_SHORT).show();
            return;
        }

        registerBtn.setEnabled(false);
        registerBtn.setText("נרשם...");

        // שלב 1: יצירת משתמש ב-Firebase Authentication
        mAuth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        // שלב 2: עדכון שם המשתמש בפרופיל (Auth)
                        UserProfileChangeRequest profileUpdates =
                                new UserProfileChangeRequest.Builder()
                                        .setDisplayName(usernameText)
                                        .build();

                        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                            // שלב 3: שמירת המשתמש ב-Firestore ומעבר לעמוד ז'אנרים
                            saveUserToFirestore(user.getUid(), usernameText, emailText);
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    registerBtn.setEnabled(true);
                    registerBtn.setText("הירשם");
                    Toast.makeText(this, "שגיאה ברישום: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveUserToFirestore(String uid, String name, String email) {
        // יצירת האובייקט לשמירה
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("email", email);
        userMap.put("favoriteGenres", new ArrayList<String>()); // מערך ריק שיתמלא ב-GenreActivity

        // שמירה ב-Firestore תחת האוסף "users"
        db.collection("users").document(uid)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "User document created successfully");

                    // שלב 4: המעבר לעמוד בחירת הז'אנרים
                    Intent intent = new Intent(RegisterActivity.this, GenreActivity.class);
                    startActivity(intent);
                    finish(); // סוגר את עמוד הרישום כדי שלא יוכלו לחזור אליו
                })
                .addOnFailureListener(e -> {
                    registerBtn.setEnabled(true);
                    registerBtn.setText("הירשם");
                    Log.e("FirestoreError", "Error: " + e.getMessage());
                    Toast.makeText(this, "שגיאה בשמירת נתונים ב-Firestore", Toast.LENGTH_SHORT).show();
                });
    }
}