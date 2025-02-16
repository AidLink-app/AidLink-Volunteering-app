package com.example.welcom;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.view.View;
import android.widget.Toolbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private List<Post> postList = new ArrayList<>();
    private List<Post> filteredList = new ArrayList<>(); // List for search filtering
    private Map<String, String> organizationMap = new HashMap<>(); // Cache for organization names

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String userRole;
    private FirebaseUser currentUser;

    private Button btnAddPost, btnSignOut; // Add Post and Sign-Out buttons
    private EditText searchEditText; // Search bar reference

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if(item.getItemId() == R.id.nav_home) {
                return true;
            }
            if(item.getItemId() == R.id.nav_settings) {
                return true;
            }
            if (item.getItemId() == R.id.nav_notifications) {
                Intent intent = new Intent(this, NotificationsActivity.class);
                startActivity(intent);
            }
            if(item.getItemId() ==R.id.nav_profile) {
                return true;
            }
            if(item.getItemId() == R.id.nav_addpost && userRole.equals("organization")) {
                Intent intent = new Intent(DashboardActivity.this, AddPostActivity.class);
                startActivity(intent);
            }
            return false;
        });


        // Hide the ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize Firestore and Auth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Intent curr_intent = getIntent();
        User user = (User) curr_intent.getSerializableExtra("user");
        userRole = user.getRole();
        adapter = new PostAdapter(filteredList, post -> {
            // Remove the post from both postList and filteredList
            postList.remove(post);
            filteredList.remove(post);
            adapter.notifyDataSetChanged();
        }, userRole);
        recyclerView.setAdapter(adapter);

        // Initialize Search Bar
        searchEditText = findViewById(R.id.searchEditText);

        // Handle Add Post Button
//        btnAddPost = findViewById(R.id.btnAddPost);
//        btnAddPost.setOnClickListener(v -> {
//            Intent intent = new Intent(DashboardActivity.this, AddPostActivity.class);
//            startActivity(intent);
//        });

        // Handle Sign Out Button
//        Button btnSignOut = findViewById(R.id.btnSignOut);
//        btnSignOut.setOnClickListener(v -> {
//            auth.signOut(); // Sign out the user
//            Toast.makeText(DashboardActivity.this, "Signed out successfully!", Toast.LENGTH_SHORT).show();
//
//            // Redirect to Login Screen
//            Intent intent = new Intent(DashboardActivity.this, MainActivity.class); // Assuming MainActivity is your login screen
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
//            finish();
//        });

        // Fetch Posts and Organizations
        fetchOrganizations();
        fetchPostsFromFirestore();

        // Setup Search Filtering
        setupSearch();
    }


    private String getUserRole(String currentUserEmail) {
        // Fetch user role from Firestore
        db.collection("users").document(currentUserEmail)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        userRole = documentSnapshot.getString("role");
                    } else {
                        Toast.makeText(DashboardActivity.this, "User not found!", Toast.LENGTH_SHORT).show();
                    }
                });
        return userRole; // Return empty string if user role is not found"
    }

    /**
     * Fetch organizations and store them in a map
     */
    private void fetchOrganizations() {
        db.collection("organizations")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String orgId = document.getId();
                        String orgName = document.getString("name");
                        organizationMap.put(orgId, orgName); // Store in cache
                    }
                    fetchPostsFromFirestore(); // Ensure posts are fetched after organizations
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching organizations", e);
                });
    }

    /**
     * Fetch posts from Firestore and map organization names
     */
    private void fetchPostsFromFirestore() {
        db.collection("posts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Post post = document.toObject(Post.class);
                        post.setPostId(document.getId());
                        String organizationId = document.getString("organizationId");

                        if (organizationId != null && organizationMap.containsKey(organizationId)) {
                            post.setOrganization(organizationMap.get(organizationId)); // Set organization name
                        } else {
                            post.setOrganization("Unknown Organization");
                        }

                        postList.add(post);
                    }
                    filteredList.clear();
                    filteredList.addAll(postList);
                    adapter.notifyDataSetChanged(); // Refresh RecyclerView
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching posts", e);
                });
    }

    /**
     * Setup real-time search functionality
     */
    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPosts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * Filter posts based on search query
     */
    private void filterPosts(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(postList); // Show all posts if search query is empty
        } else {
            for (Post post : postList) {
                if (post.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        post.getDescription().toLowerCase().contains(query.toLowerCase()) ||
                        post.getCategory().toLowerCase().contains(query.toLowerCase()) ||
                        post.getOrganization().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(post);
                }
            }
        }
        adapter.notifyDataSetChanged(); // Refresh RecyclerView with filtered data
    }
}