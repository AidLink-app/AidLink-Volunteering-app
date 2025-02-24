package com.example.welcom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddPostFragment extends Fragment {

    private EditText titleField, descriptionField, dateField, locationField, categoryField, imageUrlField;

    public AddPostFragment() {
        // Required empty public constructor
    }

    private User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_post, container, false);

        titleField = view.findViewById(R.id.titleField);
        descriptionField = view.findViewById(R.id.descriptionField);
        dateField = view.findViewById(R.id.dateField);
        locationField = view.findViewById(R.id.locationField);
        categoryField = view.findViewById(R.id.categoryField);
        imageUrlField = view.findViewById(R.id.imageUrlField);

        user = UserSession.getUser();
        Button savePostButton = view.findViewById(R.id.savePostButton);
        savePostButton.setOnClickListener(v -> savePost());

        return view;
    }

    private void savePost() {
        String title = titleField.getText().toString();
        String description = descriptionField.getText().toString();
        String dateStr = dateField.getText().toString().trim();
        String location = locationField.getText().toString();
        String category = categoryField.getText().toString();
        String imageUrl = imageUrlField.getText().toString();

        Timestamp dateTimestamp = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date dateObj = sdf.parse(dateStr);
            if (dateObj != null) {
                dateTimestamp = new Timestamp(dateObj);
            } else {
                Toast.makeText(getActivity(), "Invalid date format!", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Invalid date format, please use yyyy-MM-dd", Toast.LENGTH_SHORT).show();
            return;
        }

        if (title.isEmpty() || description.isEmpty() || location.isEmpty() || category.isEmpty()) {
            Toast.makeText(getActivity(), "All fields are required.", Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> post = new HashMap<>();
        post.put("activeStatus", true);
        post.put("title", title);
        post.put("description", description);
        post.put("date", dateTimestamp);
        post.put("location", location);
        post.put("organizationName", user.getName()); // Store the organization Name
        post.put("organizationEmail", user.getEmail());
        post.put("category", category);
        post.put("imageUrl", imageUrl);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts").add(post)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getActivity(), "Post added successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to add post: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
