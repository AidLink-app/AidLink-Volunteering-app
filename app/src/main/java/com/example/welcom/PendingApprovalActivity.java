package com.example.welcom;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class PendingApprovalActivity extends AppCompatActivity {
    private Button btnRefresh;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_approval);

        btnRefresh = findViewById(R.id.btnRefresh);
        TextView tvStatus = findViewById(R.id.tvStatus);

        tvStatus.setText("Your organization registration is pending admin approval. Please wait...");

        btnRefresh.setOnClickListener(v -> checkApprovalStatus());
    }

    private void checkApprovalStatus() {
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (email != null) {
            db.collection("users").document(email)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Boolean approved = documentSnapshot.getBoolean("approved");
                            if (approved != null && approved) {
                                Toast.makeText(PendingApprovalActivity.this, "Approved! Redirecting...", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(PendingApprovalActivity.this, DashboardActivity.class));
                                finish();
                            } else {
                                Toast.makeText(PendingApprovalActivity.this, "Still pending approval. Please wait.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(PendingApprovalActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }
}
