package com.example.welcom;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private List<Post> postList = new ArrayList<>();
    private List<Post> filteredList = new ArrayList<>(); // List for search filtering

    private FirebaseFirestore db;
    private Button btnAddPost; // "Add Post" Button reference
    private EditText searchEditText; // Search bar reference

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard); // Ensure your XML is named activity_dashboard.xml

        // Hide the ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PostAdapter(filteredList, post -> {
            // Remove the post from both postList and filteredList
            postList.remove(post);
            filteredList.remove(post);
            adapter.notifyDataSetChanged(); // Refresh RecyclerView
        });        recyclerView.setAdapter(adapter);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Search Bar
        searchEditText = findViewById(R.id.searchEditText);

        // Fetch Posts
        fetchPostsFromFirestore();

        // Handle Add Post Button
        btnAddPost = findViewById(R.id.btnAddPost);
        btnAddPost.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, AddPostActivity.class);
            startActivity(intent);
        });

        // Setup Search Filtering
        setupSearch();
    }

    /**
     * Fetch posts from Firestore
     */
    private void fetchPostsFromFirestore() {
        db.collection("posts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postList.clear(); // Clear any existing data
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Post post = document.toObject(Post.class);
                        postList.add(post);
                    }
                    filteredList.clear();
                    filteredList.addAll(postList); // Initially, show all posts
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
                        post.getCategory().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(post);
                }
            }
        }
        adapter.notifyDataSetChanged(); // Refresh RecyclerView with filtered data
    }
}