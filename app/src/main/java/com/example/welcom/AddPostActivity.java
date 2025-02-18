package com.example.welcom;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.app.DatePickerDialog;
import android.widget.DatePicker;
import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

public class AddPostActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextDescription, editTextDate, editTextLocation, editTextCategory, editTextImageUrl, editTextWhatsappLink;
    private Spinner organizationSpinner;
    private Button buttonSubmit, btnReturn;
    private FirebaseFirestore db;

    private List<String> organizationIds = new ArrayList<>();
    private List<String> organizationNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        // Initialize Return Button
        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(v -> finish());

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Link UI elements
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextDate = findViewById(R.id.editTextDate);
        editTextLocation = findViewById(R.id.editTextLocation);
        editTextCategory = findViewById(R.id.editTextCategory);
        editTextImageUrl = findViewById(R.id.editTextImageUrl);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        organizationSpinner = findViewById(R.id.organizationSpinner);
        editTextWhatsappLink = findViewById(R.id.editTextWhatsappLink);

        // Show a DatePickerDialog when user clicks on editTextDate
        editTextDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(
                    AddPostActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        // month is 0-based
                        int realMonth = month + 1;
                        String selectedDate = dayOfMonth + "/" + realMonth + "/" + year;
                        editTextDate.setText(selectedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });

        loadOrganizations(); // Populate the organization spinner

        buttonSubmit.setOnClickListener(v -> submitPost());
    }

    /**
     * Load organizations from Firestore into the Spinner
     */
    private void loadOrganizations() {
        db.collection("organizations")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    organizationIds.clear();
                    organizationNames.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        organizationIds.add(doc.getId());
                        organizationNames.add(doc.getString("name"));
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, organizationNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    organizationSpinner.setAdapter(adapter);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load organizations: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Submit a new post to Firestore
     */
    private void submitPost() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String dateStr = editTextDate.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();
        String category = editTextCategory.getText().toString().trim();
        String imageUrl = editTextImageUrl.getText().toString().trim();
        String whatsappLink = editTextWhatsappLink.getText().toString().trim();


        int selectedOrgIndex = organizationSpinner.getSelectedItemPosition();
        String organizationId = organizationIds.get(selectedOrgIndex); // Get selected organization ID
        String organization = organizationSpinner.getSelectedItem().toString(); // Get selected organization Name


        // Validate all fields
        if (title.isEmpty() || description.isEmpty() || dateStr.isEmpty() || location.isEmpty()
                || organizationId.isEmpty() || category.isEmpty() || imageUrl.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields before submitting!", Toast.LENGTH_SHORT).show();
            return;
        }

        Timestamp dateTimestamp = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date dateObj = sdf.parse(dateStr);
            if (dateObj != null) {
                dateTimestamp = new Timestamp(dateObj);
            } else {
                Toast.makeText(this, "Invalid date format!", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Invalid date format, please use yyyy-MM-dd", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> post = new HashMap<>();
        post.put("title", title);
        post.put("description", description);
        post.put("date", dateTimestamp);
        post.put("location", location);
        post.put("organizationId", organizationId); // Reference to selected organization
        post.put("organization", organization); // Store the organization Name
        post.put("whatsapp_link", whatsappLink);
        post.put("category", category);
        post.put("imageUrl", imageUrl);

        db.collection("posts")
                .add(post)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Post added successfully!", Toast.LENGTH_SHORT).show();
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