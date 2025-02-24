package com.example.welcom;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.Timestamp;  // <-- IMPORTANT: Use Firebase Timestamp, not java.security!
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.io.File;

import android.Manifest;

public class AddPostActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextDescription, editTextDate, editTextLocation, editTextWhatsappLink;
    private Spinner organizationSpinner;
    private Button buttonSubmit, btnReturn;
    private FirebaseFirestore db;

    private List<String> organizationIds = new ArrayList<>();
    private List<String> organizationNames = new ArrayList<>();
    private Spinner locationSpinner, categorySpinner;
    private static final int REQUEST_IMAGE_GALLERY = 100;
    private static final int REQUEST_IMAGE_CAMERA = 101;

    private StorageReference storageRef;
    private Uri selectedImageUri;



    private ImageButton buttonImagePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

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
        editTextWhatsappLink = findViewById(R.id.editTextWhatsappLink);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        organizationSpinner = findViewById(R.id.organizationSpinner);

        locationSpinner = findViewById(R.id.locationSpinner);
        categorySpinner = findViewById(R.id.categorySpinner);

// Use ArrayAdapter with the arrays you defined in strings.xml
        ArrayAdapter<CharSequence> locationAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.location_options,
                android.R.layout.simple_spinner_item
        );
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(locationAdapter);

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.category_options,
                android.R.layout.simple_spinner_item
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        // Image picker button
        buttonImagePicker = findViewById(R.id.buttonImagePicker);
        buttonImagePicker.setOnClickListener(v -> openGallery());

        // Date Picker Dialog
        editTextDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(
                    AddPostActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        int realMonth = month + 1; // month is zero-based
                        String selectedDate = dayOfMonth + "/" + realMonth + "/" + year;
                        editTextDate.setText(selectedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });

        // Load organizations from Firestore
        loadOrganizations();
        checkPermissions();

        buttonSubmit.setOnClickListener(v -> submitPost());
    }

    private Uri photoUri;


    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_IMAGE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAMERA) {
                // The camera wrote the image to photoUri;
                // there's NO need to check data.getExtras() for "data"
                selectedImageUri = photoUri;
                Toast.makeText(this, "Camera image selected.", Toast.LENGTH_SHORT).show();
            } else if (requestCode == REQUEST_IMAGE_GALLERY && data != null) {
                selectedImageUri = data.getData();
                Toast.makeText(this, "Gallery image selected.", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private Uri getImageUriFromBitmap(Bitmap bitmap) {
        File imagesFolder = new File(getCacheDir(), "images");
        imagesFolder.mkdirs();
        File file = new File(imagesFolder, System.currentTimeMillis() + ".jpg");

        try {
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return FileProvider.getUriForFile(
                this,
                getApplicationContext().getPackageName() + ".provider",
                file
        );
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

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_item,
                            organizationNames
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    organizationSpinner.setAdapter(adapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load organizations: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Submit a new post to Firestore
     */
    private void submitPost() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String dateStr = editTextDate.getText().toString().trim();  // e.g. "24/02/2025"
        String locationText = editTextLocation.getText().toString().trim();
        String location = locationSpinner.getSelectedItem().toString();
        String category = categorySpinner.getSelectedItem().toString();
        String whatsappLink = editTextWhatsappLink.getText().toString().trim();

        int selectedOrgIndex = organizationSpinner.getSelectedItemPosition();
        // Safety check in case the list is empty
        if (organizationIds.isEmpty() || selectedOrgIndex < 0 || selectedOrgIndex >= organizationIds.size()) {
            Toast.makeText(this, "No organizations found!", Toast.LENGTH_SHORT).show();
            return;
        }
        String organizationId = organizationIds.get(selectedOrgIndex);
        String organization = organizationSpinner.getSelectedItem().toString();

        // Basic validation for empty fields
        if (title.isEmpty() || description.isEmpty() || dateStr.isEmpty() || locationText.isEmpty() || location.isEmpty()
                || organizationId.isEmpty() || category.isEmpty() || whatsappLink.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields before submitting!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1) Parse the user-entered date into a Firestore-friendly Timestamp
        // but first, MAKE SURE YOU USE com.google.firebase.Timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date parsedDate;
        try {
            parsedDate = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Invalid date format!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (parsedDate == null) {
            Toast.makeText(this, "Could not parse the date!", Toast.LENGTH_SHORT).show();
            return;
        }

        Timestamp dateTimestamp = new Timestamp(parsedDate);

//        // 2) Create the post object map
//        Map<String, Object> post = new HashMap<>();
//        post.put("activeStatus", true);
//        post.put("title", title);
//        post.put("description", description);
//        // Store the date as a real Firebase Timestamp
//        post.put("date", dateTimestamp);
//        post.put("locationArea", location);
//        post.put("location", locationText);
//        post.put("organizationId", organizationId);
//        post.put("organization", organization);
//        post.put("whatsapp_link", whatsappLink);
//        post.put("category", category);
//
//        // 3) Add to Firestore
//        db.collection("posts")
//                .add(post)
//                .addOnSuccessListener(documentReference -> {
//                    Toast.makeText(this, "Post added successfully!", Toast.LENGTH_SHORT).show();
//                    startActivity(new Intent(AddPostActivity.this, DashboardActivity.class)
//                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
//                    finish();
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(this, "Failed to add post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//
//        if (selectedImageUri == null) {
//            Toast.makeText(this, "Please select an image first!", Toast.LENGTH_SHORT).show();
//            return;
//        }

        uploadImageAndSavePost(title, description, dateTimestamp, locationText, location, organizationId,
                organization, category, whatsappLink);

    }

    private void uploadImageAndSavePost(String title, String description, Timestamp dateTimestamp,
                                        String locationText, String locationArea, String organizationId,
                                        String organization, String category, String whatsappLink) {

        // We'll store images in a folder named "post_images/"
        // The file name can be: currentTime + .jpg
        String fileName = "post_images/" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storageRef.child(fileName);

        // Now upload:
        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Once the image is uploaded, we get the download URL
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // "uri" is the public download URL
                        String imageUrl = uri.toString();

                        // Now we can add to Firestore, including imageUrl
                        savePostToFirestore(
                                title, description, dateTimestamp, locationText, locationArea,
                                organizationId, organization, category, whatsappLink, imageUrl
                        );
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Image upload failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void savePostToFirestore(String title, String description, Timestamp dateTimestamp, String locationText, String locationArea, String organizationId, String organization, String category, String whatsappLink, String imageUrl) {

        // 1) Build your map
        Map<String, Object> post = new HashMap<>();
        post.put("activeStatus", true);
        post.put("title", title);
        post.put("description", description);
        post.put("date", dateTimestamp);
        post.put("locationArea", locationArea);
        post.put("location", locationText);
        post.put("organizationId", organizationId);
        post.put("organization", organization);
        post.put("category", category);
        post.put("whatsapp_link", whatsappLink);
        post.put("imageUrl", imageUrl);  // <--- store the image URL from Storage

        // 2) Add to Firestore
        db.collection("posts")
                .add(post)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Post added successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AddPostActivity.this, DashboardActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (storagePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

}
