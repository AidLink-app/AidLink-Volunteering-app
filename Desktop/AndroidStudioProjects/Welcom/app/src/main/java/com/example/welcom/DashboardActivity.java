package com.example.welcom;

import android.os.Bundle;
import android.util.Log;

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

    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Set Custom ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setCustomView(R.layout.action_bar_title);
        }

        recyclerView = findViewById(R.id.recyclerView); // Ensure you have a RecyclerView in activity_dashboard.xml
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PostAdapter(postList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        fetchPostsFromFirestore();
    }

    private void fetchPostsFromFirestore() {
        db.collection("posts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postList.clear(); // Clear any existing data
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Post post = document.toObject(Post.class);
                        postList.add(post);
                    }
                    adapter.notifyDataSetChanged(); // Refresh RecyclerView
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching posts", e);
                });
    }
}