package com.example.welcom;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private List<Post> postList = new ArrayList<>();
    private List<Post> filteredList = new ArrayList<>(); // List for search filtering
    private Map<String, String> organizationMap = new HashMap<>(); // Cache for organization names

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private Button btnAddPost, btnSignOut; // Add Post and Sign-Out buttons
    private EditText searchEditText; // Search bar reference

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

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
        adapter = new PostAdapter(filteredList, post -> {
            // Remove the post from both postList and filteredList
            postList.remove(post);
            filteredList.remove(post);
            adapter.notifyDataSetChanged();
        });
        recyclerView.setAdapter(adapter);

        // Initialize Search Bar
        searchEditText = findViewById(R.id.searchEditText);

        // Handle Add Post Button
        btnAddPost = findViewById(R.id.btnAddPost);
        btnAddPost.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, AddPostActivity.class);
            startActivity(intent);
        });


        // Fetch Posts and Organizations
        fetchOrganizations();
        fetchPostsFromFirestore();

        // Setup Search Filtering
        setupSearch();
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