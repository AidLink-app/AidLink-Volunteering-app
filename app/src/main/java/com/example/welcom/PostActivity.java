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

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
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

        // Initialize fields
        EditText titleField = findViewById(R.id.titleField);
        EditText descriptionField = findViewById(R.id.descriptionField);
        EditText dateField = findViewById(R.id.dateField);
        EditText locationField = findViewById(R.id.locationField);
        EditText categoryField = findViewById(R.id.categoryField);
        EditText imageUrlField = findViewById(R.id.imageUrlField);
        Button saveButton = findViewById(R.id.saveButton);

        // Check if editing
        Post post = (Post) getIntent().getSerializableExtra("post");
        if (post != null) {
            // Populate the fields
            titleField.setText(post.getTitle());
            descriptionField.setText(post.getDescription());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            dateField.setText(sdf.format(post.getDate().toDate()));
            locationField.setText(post.getLocation());
            categoryField.setText(post.getCategory());
            imageUrlField.setText(post.getImageUrl());
        }

        // Setup save button click listener
        saveButton.setOnClickListener(view -> {
            if (post != null) {
                updatePost(post);
            } else {
                createPost();
            }
        });
    }

    private void createPost() {
        Map<String, Object> postMap = new HashMap<>();
        postMap.put("title", titleField.getText().toString());
        postMap.put("description", descriptionField.getText().toString());
        postMap.put("date", descriptionField.getText().toString());
        postMap.put("location", descriptionField.getText().toString());
        postMap.put("category", descriptionField.getText().toString());
        postMap.put("imageUrl", descriptionField.getText().toString());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts").add(postMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Post added successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updatePost(Post post) {
        Map<String, Object> postMap = new HashMap<>();
        postMap.put("title", titleField.getText().toString());
        postMap.put("description", descriptionField.getText().toString());
        postMap.put("date", descriptionField.getText().toString());
        postMap.put("location", descriptionField.getText().toString());
        postMap.put("category", descriptionField.getText().toString());
        postMap.put("imageUrl", descriptionField.getText().toString());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts").document(post.getPostId())
                .update(postMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Post updated successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}


