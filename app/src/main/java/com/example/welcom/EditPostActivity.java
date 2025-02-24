package com.example.welcom;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;
import java.util.Locale;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EditPostActivity extends AppCompatActivity {

    private EditText titleField, descriptionField, dateField, locationField, categoryField, imageUrlField;
    private Post post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        titleField = findViewById(R.id.titleField);
        descriptionField = findViewById(R.id.descriptionField);
        dateField = findViewById(R.id.dateField);
        locationField = findViewById(R.id.locationField);
        categoryField = findViewById(R.id.categoryField);
        imageUrlField = findViewById(R.id.imageUrlField);

        post = (Post) getIntent().getSerializableExtra("post");
        populateFields(post);

        Button updatePostButton = findViewById(R.id.updatePostButton);
        updatePostButton.setOnClickListener(v -> updatePost());
    }

    private void populateFields(Post post) {
        titleField.setText(post.getTitle());
        descriptionField.setText(post.getDescription());

        Timestamp timestamp = post.getDate();
        Date date = timestamp.toDate();  // Convert Timestamp to java.util.Date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());  // Define your desired date format
        String formattedDate = sdf.format(date);  // Format the date
        dateField.setText(formattedDate);

        locationField.setText(post.getLocation());
        categoryField.setText(post.getCategory());
        imageUrlField.setText(post.getImageUrl());
    }

    private void updatePost() {
        String title = titleField.getText().toString();
        String description = descriptionField.getText().toString();
        String date = dateField.getText().toString();
        String location = locationField.getText().toString();
        String category = categoryField.getText().toString();
        String imageUrl = imageUrlField.getText().toString();

        if (title.isEmpty() || description.isEmpty() || date.isEmpty() || location.isEmpty() || category.isEmpty() || imageUrl.isEmpty()) {
            Toast.makeText(this, "All fields must be filled out.", Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts").document(post.getPostId())
                .update("title", title, "description", description, "date", date, "location", location, "category", category, "imageUrl", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Post updated successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EditPostActivity.this, DashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update post: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
