package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * MainMenuActivity - This activity sets up the main navigation menu
 * and handles displaying user information in the navigation header.
 */
public class MainMenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private FirebaseAuth auth;

    private View headerView;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        NavigationView navigationView = findViewById(R.id.nav_view);

        // Access the header view
        headerView = navigationView.getHeaderView(0); // Get the first header view
        TextView uname = headerView.findViewById(R.id.nameOfUser);

        // Set the username in the navigation bar.
        // NOTE: Consider caching the username to reduce redundant Firestore queries.
        // PROBLEM 1: Firestore queries should be optimized and handled properly.
        //            Firestore calls should be cached (e.g., via SharedPreferences) to avoid unnecessary network requests.
        setUsernameInNavBar(db, currentUser, uname);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Load the default fragment on first creation.
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        // Update profile picture in navigation header.
        setProfilePictureInNavBar();
    }

    /**
     * Sets the username in the navigation header by fetching data from Firestore.
     * 
     * Code Review Notes:
     * - PROBLEM 1: Firestore queries should be optimized and handled properly.
     * - PROBLEM 2: The inline logic for updating the UI based on user role should be extracted
     *              into a separate helper function.
     * - PROBLEM 3: Error handling should be improved to provide better user feedback.
     */
    public void setUsernameInNavBar(FirebaseFirestore db, FirebaseUser currentUser, TextView uname) {
        if (currentUser != null) {
            String userId = currentUser.getUid(); // Get the authenticated user's UID
            Log.d("DEBUG", "User ID: " + userId);

            // Fetch the user's document from Firestore.
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Retrieve the username from Firestore.
                            String username = documentSnapshot.getString("username");
                            boolean isTrainer = Boolean.TRUE.equals(documentSnapshot.getBoolean("isTrainer"));

                            // PROBLEM 2: This inline if-else should ideally be in a helper function that
                            // determines the display name based on user role.
                            if (isTrainer)
                                uname.setText(username + " (Trainer)");
                            else
                                uname.setText(username + " (Training)");
                        } else {
                            Log.d("DEBUG", "Document does not exist for UID: " + userId);
                            uname.setText("not logged in");
                        }
                    })
                    .addOnFailureListener(e -> {
                        // PROBLEM 3: Although we log the error, consider showing a more user-friendly message.
                        Log.e("ERROR", "Failed to fetch user data", e);
                        uname.setText("Error fetching data");
                    });
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Navigation handling could be refactored into a helper method to reduce redundancy.
        if (item.getItemId() == R.id.nav_profile) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
        } else if (item.getItemId() == R.id.nav_home) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        } else if (item.getItemId() == R.id.nav_settings) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();
        } else if (item.getItemId() == R.id.nav_about) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AboutFragment()).commit();
        } else if (item.getItemId() == R.id.nav_logout) {
            auth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        // GravityCompat.START defines which drawer to close (the left-most drawer)
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Sets the profile picture in the navigation header.
     * The image URL is retrieved from SharedPreferences.
     */
    public void setProfilePictureInNavBar() {
        String userID = currentUser.getUid();
        SharedPreferences prefs = getSharedPreferences("profilePictures", Context.MODE_PRIVATE);
        String profilePictureUrl = prefs.getString(userID + "profilePictureUrl", null);
        ImageView profPic = headerView.findViewById(R.id.profilePicNavBar);
        if (profilePictureUrl != null) {
            Glide.with(this)
                    .load(profilePictureUrl)
                    .into(profPic);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
