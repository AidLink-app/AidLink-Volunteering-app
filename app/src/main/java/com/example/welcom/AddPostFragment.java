package com.example.welcom;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddPostFragment extends Fragment {

    private EditText titleField, descriptionField, editTextDate, locationField, categoryField, imageUrlField, editTextWhatsappLink, editTextStartTime, editTextEndTime;

    private User user;
    public AddPostFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_post, container, false);

        titleField = view.findViewById(R.id.titleField);
        descriptionField = view.findViewById(R.id.descriptionField);
        editTextDate = view.findViewById(R.id.editTextDate);
        locationField = view.findViewById(R.id.locationField);
        categoryField = view.findViewById(R.id.categoryField);
        imageUrlField = view.findViewById(R.id.imageUrlField);
        editTextWhatsappLink = view.findViewById(R.id.editTextWhatsappLink);
        editTextStartTime = view.findViewById(R.id.editTextStartTime);
        editTextEndTime = view.findViewById(R.id.editTextEndTime);

        user = UserSession.getUser();
        setupDateTimePickers();
        Button savePostButton = view.findViewById(R.id.savePostButton);
        savePostButton.setOnClickListener(v -> savePost());

        return view;
    }

    private void setupDateTimePickers() {
        editTextDate.setOnClickListener(v -> showDatePickerDialog());
        editTextStartTime.setOnClickListener(v -> showTimePickerDialog(editTextStartTime));
        editTextEndTime.setOnClickListener(v -> showTimePickerDialog(editTextEndTime));
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(getActivity(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    editTextDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.getTime()));
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showTimePickerDialog(EditText timeField) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                (timePicker, selectedHour, selectedMinute) -> timeField.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)),
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }

    private void savePost() {
        String title = titleField.getText().toString();
        String description = descriptionField.getText().toString();
        String dateStr = editTextDate.getText().toString().trim();
        String location = locationField.getText().toString();
        String category = categoryField.getText().toString();
        String imageUrl = imageUrlField.getText().toString();
        String startTime = editTextStartTime.getText().toString().trim();
        String endTime = editTextEndTime.getText().toString().trim();
        String whatsappLink = editTextWhatsappLink.getText().toString().trim();


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
        String hours = startTime + " - " + endTime;
        post.put("hours", hours);
        post.put("location", location);
        post.put("organizationName", user.getName()); // Reference to selected organization
        post.put("organizationEmail", user.getEmail()); // Store the organization Name
        post.put("whatsapp_link", whatsappLink);
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
