package com.example.welcom;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Firebase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.google.firebase.auth.FirebaseAuth;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private EditText emailField, passwordField;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.login);

        loginButton.setOnClickListener(v -> loginUser());
        Button registerButton = findViewById(R.id.register);

        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegistrationTypeActivity.class);
            startActivity(intent);
        });
    }
    private void loginUser() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString().trim();

        if (!email.isEmpty() && !password.isEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                            startActivity(intent);
                            finish();  // End this activity so the user can't go back to it
                        } else {
                            Toast.makeText(MainActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
        }
    }
}

