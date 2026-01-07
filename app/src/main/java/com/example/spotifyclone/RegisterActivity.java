package com.example.spotifyclone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    EditText email, username, password;
    Button registerBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();

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

        if (usernameText.length() < 3) {
            Toast.makeText(this, "שם משתמש קצר מדי", Toast.LENGTH_SHORT).show();
            return;
        }

        if (passwordText.length() < 6) {
            Toast.makeText(this, "הסיסמה חייבת להכיל לפחות 6 תווים", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(this, "נרשמת בהצלחה!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "שגיאה: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
        Toast.makeText(this, "נרשמת בהצלחה!", Toast.LENGTH_SHORT).show();
        finish(); // חוזר למסך Login
    }

}
