package com.example.welcom;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PostActivity extends AppCompatActivity {

    private EditText titleField, descriptionField, dateField, locationField, categoryField, imageUrlField;
    private Button saveButton;

    private boolean isEditMode = false; // Default to "Add Post" mode
    private Post post; // Post object for editing
    private String postId; // Firestore document ID for editing

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize fields
        titleField = findViewById(R.id.titleField);
        descriptionField = findViewById(R.id.descriptionField);
        dateField = findViewById(R.id.dateField);
        locationField = findViewById(R.id.locationField);
        categoryField = findViewById(R.id.categoryField);
        imageUrlField = findViewById(R.id.imageUrlField);
        saveButton = findViewById(R.id.saveButton);

        // Check intent for edit mode
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("post")) {
            isEditMode = true;
            post = (Post) intent.getSerializableExtra("post");
            postId = intent.getStringExtra("postId"); // Firestore document ID
            populateFields(post);
        }

        saveButton.setOnClickListener(view -> savePost());
    }

    private void populateFields(Post post) {
        // Prefill fields with the current post details
        if (post != null) {
            titleField.setText(post.getTitle());
            descriptionField.setText(post.getDescription());
            dateField.setText(post.getDate());
            locationField.setText(post.getLocation());
            categoryField.setText(post.getCategory());
            imageUrlField.setText(post.getImageUrl());
        }
    }

    private void savePost() {
        // Get input values
        String title = titleField.getText().toString();
        String description = descriptionField.getText().toString();
        String date = dateField.getText().toString();
        String location = locationField.getText().toString();
        String category = categoryField.getText().toString();
        String imageUrl = imageUrlField.getText().toString();

        // Validate input fields
        if (title.isEmpty() || description.isEmpty() || date.isEmpty() || location.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Log input data for debugging
        Log.d("PostActivity", "Saving Post: " + title + ", " + description);

        Map<String, Object> postMap = new HashMap<>();
        postMap.put("title", title);
        postMap.put("description", description);
        postMap.put("date", date);
        postMap.put("location", location);
        postMap.put("category", category);
        postMap.put("imageUrl", imageUrl);

        if (isEditMode) {
            // Ensure postId is valid
            if (postId == null || postId.isEmpty()) {
                Log.e("PostActivity", "Post ID is null or empty in edit mode");
                Toast.makeText(this, "Invalid post. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update Firestore document
            db.collection("posts")
                    .document(postId)
                    .update(postMap)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("PostActivity", "Post updated successfully!");
                        Toast.makeText(this, "Post updated successfully!", Toast.LENGTH_SHORT).show();
                        navigateToDashboard();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("PostActivity", "Failed to update post: " + e.getMessage());
                        Toast.makeText(this, "Failed to update post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Add new post to Firestore
            db.collection("posts")
                    .add(postMap)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("PostActivity", "Post added successfully!");
                        Toast.makeText(this, "Post added successfully!", Toast.LENGTH_SHORT).show();
                        navigateToDashboard();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("PostActivity", "Failed to add post: " + e.getMessage());
                        Toast.makeText(this, "Failed to add post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void navigateToDashboard() {
        Log.d("PostActivity", "Navigating to Dashboard");
        Intent intent = new Intent(PostActivity.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("USER", user);
        startActivity(intent);
        finish();
        Log.d("PostActivity", "Finished PostActivity");
    }
}


