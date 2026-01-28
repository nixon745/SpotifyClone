package com.example.spotifyclone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText email, password;
    Button loginBtn;
    TextView goRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();


        // אם המשתמש כבר מחובר → הולך ל-Home
        if (mAuth.getCurrentUser() != null) {
            goToHome();
            return;
        }

        email = findViewById(R.id.loginEmail);
        password = findViewById(R.id.loginPassword);
        loginBtn = findViewById(R.id.loginBtn);
        goRegister = findViewById(R.id.goRegister);

        loginBtn.setOnClickListener(v -> checkLogin());

        goRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    private void checkLogin() {
        String emailText = email.getText().toString().trim();
        String passwordText = password.getText().toString().trim();

        if (emailText.isEmpty() || passwordText.isEmpty()) {
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

        // הצגת אינדיקטור טעינה
        loginBtn.setEnabled(false);
        loginBtn.setText("מתחבר...");

        mAuth.signInWithEmailAndPassword(emailText, passwordText)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(this, "התחברת בהצלחה!", Toast.LENGTH_SHORT).show();
                    goToHome();
                })
                .addOnFailureListener(e -> {
                    loginBtn.setEnabled(true);
                    loginBtn.setText("המשך");

                    String errorMessage = e.getMessage();
                    if (errorMessage != null && errorMessage.contains("no user record")) {
                        Toast.makeText(this, "המשתמש לא קיים במערכת", Toast.LENGTH_LONG).show();
                    } else if (errorMessage != null && errorMessage.contains("password is invalid")) {
                        Toast.makeText(this, "סיסמה שגויה", Toast.LENGTH_LONG).show();
                    } else if (errorMessage != null && errorMessage.contains("network")) {
                        Toast.makeText(this, "בעיית רשת, בדוק את החיבור לאינטרנט", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "שגיאה: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void goToHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}
