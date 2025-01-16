package com.example.welcom;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class OrganizationProfileActivity extends AppCompatActivity {

    private TextView orgName, orgDescription, orgEmail, orgPosts;
    private ImageView orgLogo;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_profile);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Link UI components
        orgLogo = findViewById(R.id.orgLogo);
        orgName = findViewById(R.id.orgName);
        orgDescription = findViewById(R.id.orgDescription);
        orgEmail = findViewById(R.id.orgEmail);
        orgPosts = findViewById(R.id.orgPosts);
        Button btnReturn = findViewById(R.id.btnReturn); // Return button

        // Handle return button click
        btnReturn.setOnClickListener(v -> finish());

        // Get organizationId from the intent
        String organizationId = getIntent().getStringExtra("organizationId");

        if (organizationId != null) {
            fetchOrganizationDetails(organizationId);
        } else {
            Toast.makeText(this, "Organization ID not found!", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchOrganizationDetails(String organizationId) {
        db.collection("organizations")
                .document(organizationId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Populate organization details
                        orgName.setText(documentSnapshot.getString("name"));
                        orgDescription.setText(documentSnapshot.getString("description"));
                        orgEmail.setText(documentSnapshot.getString("email"));

                        // You can also set a logo here if available
                        // orgLogo.setImageResource(R.drawable.placeholder); // Example placeholder
                        // or load it dynamically from a URL if provided in Firestore
                        // Glide, Picasso, or similar libraries can be used for this

                        // Fetch posts by this organization
                        fetchOrganizationPosts(organizationId);
                    } else {
                        Toast.makeText(this, "Organization not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching organization: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchOrganizationPosts(String organizationId) {
        db.collection("posts")
                .whereEqualTo("organizationId", organizationId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    StringBuilder posts = new StringBuilder();
                    queryDocumentSnapshots.forEach(document -> {
                        String title = document.getString("title");
                        posts.append("• ").append(title).append("\n");
                    });
                    orgPosts.setText(posts.toString());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching posts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}