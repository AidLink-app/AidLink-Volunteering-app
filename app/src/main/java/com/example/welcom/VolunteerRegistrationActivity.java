package com.example.welcom;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import android.Manifest;
import java.io.File;
import java.io.IOException;

public class VolunteerRegistrationActivity extends AppCompatActivity {

    private EditText nameField, emailField, passwordField, phoneField;
    private Button submitButton, btnChooseImage;
    private FirebaseAuth mAuth;
    private Uri selectedImageUri;
    private File imageFile;
    private Uri imageUriFromFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_registration);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Initialize fields
        nameField = findViewById(R.id.name);
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        phoneField = findViewById(R.id.phone);
        submitButton = findViewById(R.id.submit);
        btnChooseImage = findViewById(R.id.btnChooseImage);

        // Set up Submit Button Click Listener
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegistration();
            }
        });

        // Set up Choose Image Button
        btnChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a dialog to ask the user whether they want to take a photo or choose from gallery
                showImagePickerDialog();
            }
        });

        checkAndRequestPermissions();
    }

    private void showImagePickerDialog() {
        // Create an array of options for the dialog
        String[] options = {"Take Photo", "Choose from Gallery"};

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an option")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            // User selected "Take Photo"
                            openCamera();
                            break;
                        case 1:
                            // User selected "Choose from Gallery"
                            openGallery();
                            break;
                    }
                })
                .show();
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure the camera intent is available
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            try {
                // Create the image file and get the Uri to save the photo
                File photoFile = createImageFile();
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriFromFile);
                startActivityForResult(cameraIntent, 101); // 101 is the request code for camera
            } catch (IOException e) {
                Toast.makeText(this, "Error creating file for image capture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void openGallery() {
        // Intent to open the gallery to select an image
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, 102); // 102 is the request code for gallery
    }

    private void handleRegistration() {
        // Retrieve user input
        String name = nameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String phone = phoneField.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Register with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registration success
                        Toast.makeText(VolunteerRegistrationActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();

                        // Store additional user data
                        User user = new User(email, "volunteer", "", "", "", name, phone);
                        saveUserDetailsToFirestore(user);

                        // If an image is selected, upload it to Firebase Storage
                        if (selectedImageUri != null) {
                            uploadFileToFirebaseStorage(selectedImageUri, "images/" + selectedImageUri.getLastPathSegment(), imageUrl -> {
                                // Update Firestore with image URL
                                updateImageUrlInFirestore(user.getEmail(), imageUrl);
                            });
                        }

                        // Navigate to the next activity
                        Intent intent = new Intent(VolunteerRegistrationActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Registration failed
                        Toast.makeText(VolunteerRegistrationActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserDetailsToFirestore(User user) {
        // Save user details to Firestore
        FirebaseFirestore.getInstance().collection("users").document(user.getEmail())
                .set(user)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "User details saved successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving user details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateImageUrlInFirestore(String email, String imageUrl) {
        // Update user image URL in Firestore
        FirebaseFirestore.getInstance().collection("users").document(email)
                .update("imageURL", imageUrl)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "User image URL updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadFileToFirebaseStorage(Uri fileUri, String path, FirebaseCallback callback) {
        FirebaseStorage.getInstance().getReference().child(path).putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    taskSnapshot.getStorage().getDownloadUrl()
                            .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(VolunteerRegistrationActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Handle the image selection result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 101) { // Camera result
                if (imageUriFromFile != null) {
                    selectedImageUri = imageUriFromFile;  // Use the URI from the file created for the camera image
                    Log.d("VolunteerRegistration", "Image URI from Camera: " + selectedImageUri);
                }
            } else if (requestCode == 102) { // Gallery result
                selectedImageUri = data.getData();
                Log.d("VolunteerRegistration", "Image URI from Gallery: " + selectedImageUri);
            }
        }
    }


    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            int storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (cameraPermission != PackageManager.PERMISSION_GRANTED || storagePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 1);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create a unique file name and save the image in the external files directory
        String imageFileName = "JPEG_" + System.currentTimeMillis() + ".jpg";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);  // File path for storing image
        imageUriFromFile = FileProvider.getUriForFile(this, "com.example.welcom.fileprovider", imageFile);
        return imageFile;
    }

    // Callback interface for handling upload results
    interface FirebaseCallback {
        void onSuccess(String imageUrl);
    }
}
