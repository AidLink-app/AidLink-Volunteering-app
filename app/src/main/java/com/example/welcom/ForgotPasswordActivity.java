package com.example.welcom;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.welcom.databinding.ActivityForgotPasswordBinding;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText fpEmail;
    private Button fpSendResetBtn;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Find UI elements
        fpEmail = findViewById(R.id.fpEmail);
        fpSendResetBtn = findViewById(R.id.fpSendResetBtn);

        // Set click listener for reset button
        fpSendResetBtn.setOnClickListener(v -> {
            String email = fpEmail.getText().toString().trim();
            if (!email.isEmpty()) {
                sendPasswordResetEmail(email);
            } else {
                Toast.makeText(ForgotPasswordActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendPasswordResetEmail(String email) {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Reset Email sent! Check your inbox.",
                                Toast.LENGTH_LONG).show();
                        finish(); // Close this activity, return to login
                    } else {
                        String error = (task.getException() != null) ?
                                task.getException().getMessage() : "Unknown error";
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Failed to send reset email: " + error,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
