package com.example.welcom;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
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

public class AddPostFragment extends Fragment {

    private EditText editTextTitle, editTextDescription, editTextDate, editTextLocation, editTextWhatsappLink, editTextStartTime, editTextEndTime;
    private Spinner organizationSpinner, locationSpinner, categorySpinner;
    private ImageButton buttonImagePicker;
    private Button buttonSubmit;
    private FirebaseFirestore db;
    private StorageReference storageRef;

    private Uri selectedImageUri;
    private static final int REQUEST_IMAGE_CAPTURE = 100;
    private static final int REQUEST_IMAGE_GALLERY = 101;

    public AddPostFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_post, container, false);

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        // Initialize UI Components
        editTextTitle = view.findViewById(R.id.editTextTitle);
        editTextDescription = view.findViewById(R.id.editTextDescription);
        editTextDate = view.findViewById(R.id.editTextDate);
        editTextStartTime = view.findViewById(R.id.editTextStartTime);
        editTextEndTime = view.findViewById(R.id.editTextEndTime);
        editTextLocation = view.findViewById(R.id.editTextLocation);
        editTextWhatsappLink = view.findViewById(R.id.editTextWhatsappLink);
        locationSpinner = view.findViewById(R.id.locationSpinner);
        categorySpinner = view.findViewById(R.id.categorySpinner);
        buttonSubmit = view.findViewById(R.id.buttonSubmit);
        buttonImagePicker = view.findViewById(R.id.buttonImagePicker);

        // Set up date and time pickers
        setupDateTimePickers();

        // Set up button listeners
        buttonSubmit.setOnClickListener(v -> submitPost());
        buttonImagePicker.setOnClickListener(v -> selectImage());

        // Populate Spinners
        loadSpinners();

        return view;
    }

    private void setupDateTimePickers() {
        editTextDate.setOnClickListener(v -> showDatePickerDialog());
        editTextStartTime.setOnClickListener(v -> showTimePickerDialog(editTextStartTime));
        editTextEndTime.setOnClickListener(v -> showTimePickerDialog(editTextEndTime));
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(getContext(), this::onDateSet, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        month = month + 1; // Calendar month is zero-based
        String formattedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month, dayOfMonth);
        editTextDate.setText(formattedDate);
    }

    private void showTimePickerDialog(EditText timeField) {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(getContext(), (tp, hourOfDay, minute) -> {
            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            timeField.setText(formattedTime);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void loadSpinners() {
        ArrayAdapter<CharSequence> locationAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.location_options,
                android.R.layout.simple_spinner_item
        );
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(locationAdapter);

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.category_options,
                android.R.layout.simple_spinner_item
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
    }

    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Photo!");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Take Photo")) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
                } else {
                    openCamera();
                }
            } else if (options[item].equals("Choose from Gallery")) {
                openGallery();
            } else if (options[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(getContext(), "Error occurred while creating the file", Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(), "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                "JPEG_" + timeStamp,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file path for use with ACTION_VIEW intents
        return image;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_GALLERY) {
                selectedImageUri = data.getData();
                buttonImagePicker.setImageURI(selectedImageUri);
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // handle camera image
                buttonImagePicker.setImageURI(selectedImageUri);
            }
        }
    }

    private void submitPost() {
        String title = editTextTitle.getText().toString();
        String description = editTextDescription.getText().toString();
        String date = editTextDate.getText().toString();
        String startTime = editTextStartTime.getText().toString();
        String endTime = editTextEndTime.getText().toString();
        String location = editTextLocation.getText().toString();
        String category = categorySpinner.getSelectedItem().toString();
        String whatsappLink = editTextWhatsappLink.getText().toString();

        if (title.isEmpty() || description.isEmpty() || date.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || location.isEmpty() || category.isEmpty() || whatsappLink.isEmpty()) {
            Toast.makeText(getContext(), "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> post = new HashMap<>();
        post.put("title", title);
        post.put("description", description);
        post.put("date", date);
        post.put("startTime", startTime);
        post.put("endTime", endTime);
        post.put("location", location);
        post.put("category", category);
        post.put("whatsappLink", whatsappLink);
        post.put("imageUri", selectedImageUri != null ? selectedImageUri.toString() : "");

        db.collection("posts").add(post)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Post added successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to add post: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
