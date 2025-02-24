package com.example.welcom;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.common.SignInButton;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.google.firebase.auth.FirebaseAuth;

import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;


public class MainActivity extends AppCompatActivity {
    private EditText emailField, passwordField;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 100;
    private CheckBox rememberMeCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        setupGoogleSignIn();

        // Google Sign-In Button
        SignInButton googleSignInButton = findViewById(R.id.google_sign_in_button);
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());

        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);
        rememberMeCheckBox.setChecked(UserSession.isRememberMe());

        UserSession.init(this);

        if (UserSession.isRememberMe() && UserSession.getUser() != null) {
            // User is logged in, redirect to DashboardActivity
            Intent intent = new Intent(this, DashboardActivity.class);
            startActivity(intent);
            finish();
        }

        rememberMeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            UserSession.setRememberMe(isChecked);
        });

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

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))  // Ensure correct Web Client ID
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("Google Sign-In", "Google Sign-In successful. ID Token: " + account.getIdToken());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.e("Google Sign-In", "Google Sign-In failed.", e);
                Toast.makeText(this, "Google Sign-In Failed: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        Log.d("Firebase Auth", "Received ID Token: " + idToken);

        if (idToken == null) {
            Log.e("Firebase Auth", "ID Token is NULL! Check Google Sign-In setup.");
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            String email = firebaseUser.getEmail();
                            String name = firebaseUser.getDisplayName();
                            checkAndStoreUser(email, name);
                        }
                    } else {
                        Log.e("Firebase Auth", "signInWithCredential: FAILURE", task.getException());
                        Toast.makeText(MainActivity.this, "Authentication Failed.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void checkAndStoreUser(String email, String name) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(email).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fcmToken = documentSnapshot.getString("fcmToken");
                        User user = new User(email, "volunteer", "", "", "", name, "");
                        user.setFcmToken(fcmToken);

                        // Store user in session
                        UserSession.setUser(user);

                        Log.d("User Session", "User session updated with existing user details.");
                        updateUI(auth.getCurrentUser());
                    } else {
                        // First-time login, store new user
                        User newUser = new User(email, "volunteer", "", "", "", name, "");
                        db.collection("users").document(email).set(newUser)
                                .addOnSuccessListener(aVoid -> {
                                    UserSession.setUser(newUser);
                                    Log.d("Firebase Firestore", "New Google user added to Firestore.");
                                    updateUI(auth.getCurrentUser());
                                })
                                .addOnFailureListener(e -> Log.e("Firebase Firestore", "Error adding user", e));
                    }
                })
                .addOnFailureListener(e -> Log.e("Firebase Firestore", "Error fetching user", e));
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
                                    UserSession.setUser(user);
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

