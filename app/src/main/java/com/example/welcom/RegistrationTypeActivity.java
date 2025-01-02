package com.example.welcom;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class RegistrationTypeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_type);

        // Initialize existing buttons
        Button volunteerButton = findViewById(R.id.volunteerButton);
        Button organizerButton = findViewById(R.id.organizerButton);

        volunteerButton.setOnClickListener(v -> {
            // Intent to volunteer registration activity
        });

        organizerButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrationTypeActivity.this, OrganizationRegistrationActivity.class);
            startActivity(intent);
        });

        // Add Return Button programmatically
        Button returnButton = new Button(this);
        returnButton.setText("Return");

        // Define layout parameters
        returnButton.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        returnButton.setPadding(16, 16, 16, 16);
        returnButton.setBackgroundColor(getResources().getColor(android.R.color.white));



        // Add the button to the root layout
        ViewGroup rootLayout = findViewById(android.R.id.content);
        if (rootLayout != null) {
            rootLayout.addView(returnButton);
        }

        // Handle Return Button Click
        returnButton.setOnClickListener(v -> {
            finish(); // Close activity and return to the previous screen
        });
    }
}