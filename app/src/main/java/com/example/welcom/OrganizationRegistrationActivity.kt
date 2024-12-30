package com.example.welcom

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class OrganizationRegistrationActivity : AppCompatActivity() {
    private lateinit var btnChooseImage: Button
    private lateinit var btnUploadPDF: Button
    private lateinit var etDescription: EditText
    private lateinit var btnSubmit: Button
    private var selectedImageUri: Uri? = null
    private var selectedPDFUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organization_registration)

        btnChooseImage = findViewById(R.id.btnChooseImage)
        btnUploadPDF = findViewById(R.id.btnUploadPDF)
        etDescription = findViewById(R.id.etDescription)
        btnSubmit = findViewById(R.id.btnSubmit)

        btnChooseImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        btnUploadPDF.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf"
            startActivityForResult(intent, 101)
        }

        btnSubmit.setOnClickListener {
            val description = etDescription.text.toString()
            if (selectedImageUri != null && selectedPDFUri != null && description.isNotEmpty()) {
                // Send data to server
                Toast.makeText(this, "Data submitted for approval!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                100 -> {
                    selectedImageUri = data?.data
                    Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show()
                }
                101 -> {
                    selectedPDFUri = data?.data
                    Toast.makeText(this, "PDF attached", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
