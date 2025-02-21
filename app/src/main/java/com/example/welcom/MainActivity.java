package com.example.welcom;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

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

        TextView forgotPasswordText = findViewById(R.id.forgotPassword);
        forgotPasswordText.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

    }
    private void loginUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        // Create the User object

        if (!email.isEmpty() && !password.isEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            getUserRole(email, new RoleCallback() {
                                @Override
                                public void onRoleFetched(String role) {
                                    // Handle the retrieved role
                                    Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                                    User user = new User(
                                            email, role, "", "", "", "", ""
                                    );
                                    // get token for notifications
                                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                                        if(task.isSuccessful()){
                                            String token = task.getResult();
                                            Log.i("My token:" ,token);
                                            FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getUid()).update("fcmToken",token);
                                            user.setFcmToken(token);

                                        }
                                    });
                                    intent.putExtra("user", user);
                                    startActivity(intent);
                                    finish();
                                }
                                @Override
                                public void onError(String error) {
                                    // Handle any error that occurred
                                    System.err.println("Error fetching role: " + error);
                                    Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(MainActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
        }
    }

    public interface RoleCallback {
        void onRoleFetched(String role);  // Called when the role is successfully fetched
        void onError(String error);      // Called when there's an error
    }

    public void getUserRole(String userEmail, RoleCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userEmail)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        callback.onRoleFetched(role); // Return the role through the callback
                    } else {
                        callback.onError("User not found!"); // Notify if user doesn't exist
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage()); // Notify error
                });
    }
}

