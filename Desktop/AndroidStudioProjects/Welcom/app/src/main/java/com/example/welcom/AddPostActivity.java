package com.example.welcom;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextDescription, editTextDate, editTextLocation, editTextOrganization, editTextCategory, editTextImageUrl;
    private Button buttonSubmit;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        // Initialize Return Button
        Button btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(v -> {
            finish(); // Go back to the previous activity
        });

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Link UI elements
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextDate = findViewById(R.id.editTextDate);
        editTextLocation = findViewById(R.id.editTextLocation);
        editTextOrganization = findViewById(R.id.editTextOrganization);
        editTextCategory = findViewById(R.id.editTextCategory);
        editTextImageUrl = findViewById(R.id.editTextImageUrl);
        buttonSubmit = findViewById(R.id.buttonSubmit);

        buttonSubmit.setOnClickListener(v -> submitPost());

    }

    private void submitPost() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();
        String organization = editTextOrganization.getText().toString().trim();
        String category = editTextCategory.getText().toString().trim();
        String imageUrl = editTextImageUrl.getText().toString().trim();

        // Validate all fields
        if (title.isEmpty() || description.isEmpty() || date.isEmpty() || location.isEmpty()
                || organization.isEmpty() || category.isEmpty() || imageUrl.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields before submitting!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> post = new HashMap<>();
        post.put("title", title);
        post.put("description", description);
        post.put("date", date);
        post.put("location", location);
        post.put("organization", organization);
        post.put("category", category);
        post.put("imageUrl", imageUrl);

        db.collection("posts")
                .add(post)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Post added successfully!", Toast.LENGTH_SHORT).show();

                    // Navigate back to Posts Feed
                    Intent intent = new Intent(AddPostActivity.this, DashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}