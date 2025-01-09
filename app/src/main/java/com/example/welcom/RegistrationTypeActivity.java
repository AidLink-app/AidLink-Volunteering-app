package com.example.welcom;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;

public class RegistrationTypeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_type);

        Button volunteerButton = findViewById(R.id.volunteerButton);
        Button organizerButton = findViewById(R.id.organizerButton);

        volunteerButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrationTypeActivity.this, VolunteerRegistrationActivity.class);
            startActivity(intent);
        });

        organizerButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrationTypeActivity.this, OrganizationRegistrationActivity.class);
            startActivity(intent);
        });
    }
}
