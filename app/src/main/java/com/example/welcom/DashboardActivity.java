package com.example.welcom;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DashboardActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private RecyclerView recyclerView;
    private String userRole;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        User user = UserSession.getUser();
        userRole = user.getRole();
        replaceFragment(new HomeFragment()); // Navigate to home auto
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if(item.getItemId() == R.id.nav_home) {
                replaceFragment(new HomeFragment());
                return true;
            }
            if(item.getItemId() == R.id.nav_settings) {
                replaceFragment(new SettingsFragment());
                return true;
            }
            if(item.getItemId() ==R.id.nav_profile) {
                replaceFragment(new ProfileFragment());
                return true;
            }
            if (item.getItemId() == R.id.nav_addpost && "organization".equals(userRole)) {
                // Start AddPostActivity using an Intent
                Intent intent = new Intent(this, AddPostActivity.class);
                startActivity(intent);
                return true;
            }
            return true;
        });
    }
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}

//        // Handle Add Post Button
////        btnAddPost = findViewById(R.id.btnAddPost);
////        btnAddPost.setOnClickListener(v -> {
////            Intent intent = new Intent(DashboardActivity.this, AddPostActivity.class);
////            startActivity(intent);
////        });
//
//        // Handle Sign Out Button
////        Button btnSignOut = findViewById(R.id.btnSignOut);
////        btnSignOut.setOnClickListener(v -> {
////            auth.signOut(); // Sign out the user
////            Toast.makeText(DashboardActivity.this, "Signed out successfully!", Toast.LENGTH_SHORT).show();
////            UserSession.clearUser();
////            // Redirect to Login Screen
////            Intent intent = new Intent(DashboardActivity.this, MainActivity.class); // Assuming MainActivity is your login screen
////            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
////            startActivity(intent);
////              finish();
////        });