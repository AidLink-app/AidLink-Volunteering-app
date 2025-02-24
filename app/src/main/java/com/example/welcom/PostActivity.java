package com.example.welcom;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PostActivity extends AppCompatActivity {

    private EditText titleField, descriptionField, dateField, locationField, categoryField, imageUrlField;
    private Button saveButton;
    private Post post; // This will be used if editing an existing post
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        user = UserSession.getUser();
        titleField = findViewById(R.id.titleField);
        descriptionField = findViewById(R.id.descriptionField);
        dateField = findViewById(R.id.dateField);
        locationField = findViewById(R.id.locationField);
        categoryField = findViewById(R.id.categoryField);
        imageUrlField = findViewById(R.id.imageUrlField);
        saveButton = findViewById(R.id.saveButton);

        // Check if this activity was started with a Post object to edit
        Intent intent = getIntent();
        post = (Post) intent.getSerializableExtra("post");
        if (post != null) {
            populateFields(post);
        }

        saveButton.setOnClickListener(view -> {
            if (post != null) {
                updatePost(post);
            } else {
                createPost();
            }
        });
    }

    private void populateFields(Post post) {
        titleField.setText(post.getTitle());
        descriptionField.setText(post.getDescription());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateField.setText(sdf.format(post.getDate().toDate()));
        locationField.setText(post.getLocation());
        categoryField.setText(post.getCategory());
        imageUrlField.setText(post.getImageUrl());
    }

    private void createPost() {
        Map<String, Object> postMap = new HashMap<>();
        postMap.put("title", titleField.getText().toString());
        postMap.put("description", descriptionField.getText().toString());
        postMap.put("date", dateField.getText().toString());
        postMap.put("location", locationField.getText().toString());
        postMap.put("category", categoryField.getText().toString());
        postMap.put("imageUrl", imageUrlField.getText().toString());

        FirebaseFirestore.getInstance()
                .collection("posts")
                .add(postMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(PostActivity.this, "Post added successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PostActivity.this, "Failed to add post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updatePost(Post post) {
        Map<String, Object> postMap = new HashMap<>();
        postMap.put("title", titleField.getText().toString());
        postMap.put("description", descriptionField.getText().toString());
        postMap.put("date", dateField.getText().toString());
        postMap.put("location", locationField.getText().toString());
        postMap.put("category", categoryField.getText().toString());
        postMap.put("imageUrl", imageUrlField.getText().toString());

        FirebaseFirestore.getInstance()
                .collection("posts")
                .document(post.getPostId())
                .update(postMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(PostActivity.this, "Post updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PostActivity.this, "Failed to update post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
