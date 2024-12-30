package com.example.welcom

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class OrganizationRegistrationActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth  // Declare FirebaseAuth instance

    private lateinit var btnChooseImage: Button
    private lateinit var btnUploadPDF: Button
    private lateinit var etDescription: EditText
    private lateinit var btnSubmit: Button
    private var selectedImageUri: Uri? = null
    private var selectedPDFUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organization_registration)

        // Initialize FirebaseAuth instance
        auth = FirebaseAuth.getInstance()

        // Initialize UI components
        btnChooseImage = findViewById(R.id.btnChooseImage)
        btnUploadPDF = findViewById(R.id.btnUploadPDF)
        etDescription = findViewById(R.id.etDescription)
        btnSubmit = findViewById(R.id.btnSubmit)

        setupButtonClickListeners()
        checkAndRequestPermissions()
    }

    private fun setupButtonClickListeners() {
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
            val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
            val password = findViewById<EditText>(R.id.etPassword).text.toString().trim()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                registerUser(email, password)
            } else {
                Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadFileToFirebaseStorage(fileUri: Uri, path: String) {
        val storageReference = FirebaseStorage.getInstance().reference.child(path)
        storageReference.putFile(fileUri).addOnSuccessListener {
            Toast.makeText(this, "File uploaded successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                100 -> {
                    selectedImageUri = data?.data
                    selectedImageUri?.let {
                        uploadFileToFirebaseStorage(it, "images/${it.lastPathSegment}")
                    }
                }
                101 -> {
                    selectedPDFUri = data?.data
                    selectedPDFUri?.let {
                        uploadFileToFirebaseStorage(it, "pdfs/${it.lastPathSegment}")
                    }
                }
            }
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Continue with uploading image and PDF
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
            } else {
                // If sign in fails, display a message to the user.
                Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val readStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            if (readStoragePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            }
        }
    }
}
