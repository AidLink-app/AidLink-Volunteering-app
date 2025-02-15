package com.example.welcom;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RegistrationConfirmationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_confirmation);

        // TextView for displaying confirmation message
        TextView confirmationMessage = findViewById(R.id.confirmationMessage);

        // TextView for displaying the WhatsApp link message
        TextView whatsappLinkMessage = findViewById(R.id.whatsappLinkMessage);

        // Button for joining the WhatsApp group
        Button joinWhatsAppButton = findViewById(R.id.joinWhatsAppButton);

        // Receiving the WhatsApp link from the Intent
        Intent intent = getIntent();
        String whatsappLink = intent.getStringExtra("whatsappLink");

        // Check if the WhatsApp link is available
        if (whatsappLink != null && !whatsappLink.isEmpty()) {
            // Set a click listener on the button to open the WhatsApp link
            joinWhatsAppButton.setOnClickListener(v -> {
                // Intent to open the WhatsApp link in the browser or app
                Intent openWhatsAppIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(whatsappLink));
                startActivity(openWhatsAppIntent);
            });
        } else {
            // If no WhatsApp link is available, disable the button and show a message
            whatsappLinkMessage.setText("No WhatsApp link available.");
            joinWhatsAppButton.setEnabled(false);
        }
    }
}
