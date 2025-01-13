package com.example.welcom;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class VolunteerRegistrationActivity extends AppCompatActivity {

    private EditText nameField, emailField, passwordField, phoneField;
    private Button submitButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_registration);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Initialize fields
        nameField = findViewById(R.id.name);
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        phoneField = findViewById(R.id.phone);
        submitButton = findViewById(R.id.submit);

        // Set up Submit Button Click Listener
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegistration();
            }
        });
    }

    private void saveUserDetailsToFirestore(User user) {
        // Create a HashMap for user details

        // Get Firestore instance and save the data
        FirebaseFirestore.getInstance().collection("users").document(user.getEmail())
                .set(user)
                .addOnSuccessListener(unused -> {
                    // Success callback
                    Toast.makeText(this, "User details saved successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Failure callback
                    Toast.makeText(this, "Error saving user details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void handleRegistration() {
        // Retrieve user input
        String name = nameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String phone = phoneField.getText().toString().trim();

        User user = new User(email, "volunteer", "", "", "", name, phone);
        // Validate input
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Register with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registration success

                        // Store other user data (name, phone) in Firebase Realtime Database or Firestore
                        // For now, show a success message
                        Toast.makeText(VolunteerRegistrationActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                        saveUserDetailsToFirestore(user);
                        // Navigate to the next activity
                        Intent intent = new Intent(VolunteerRegistrationActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Registration failed
                        Toast.makeText(VolunteerRegistrationActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
