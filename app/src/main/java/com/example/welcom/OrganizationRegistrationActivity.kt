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
import com.google.firebase.firestore.FirebaseFirestore

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
            val description = etDescription.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && selectedPDFUri != null && description.isNotEmpty()) {
                registerUser(email, password)
            } else {
                // Notify the user that all fields are required including the PDF and description
                Toast.makeText(this, "Email, password, a PDF, and a description are required", Toast.LENGTH_SHORT).show()
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
                selectedImageUri?.let {
                    uploadFileToFirebaseStorage(it, "images/${it.lastPathSegment}") { imageUrl ->
                        selectedPDFUri?.let { pdfUri ->
                            uploadFileToFirebaseStorage(pdfUri, "pdfs/${pdfUri.lastPathSegment}") { pdfUrl ->
                                saveUserDetailsToFirestore(email, imageUrl, pdfUrl, etDescription.text.toString())
                            }
                        }
                    }
                }
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
            } else {
                // If sign in fails, display a message to the user.
                Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadFileToFirebaseStorage(fileUri: Uri, path: String, callback: (String) -> Unit) {
        val storageReference = FirebaseStorage.getInstance().reference.child(path)
        storageReference.putFile(fileUri).addOnSuccessListener {
            storageReference.downloadUrl.addOnSuccessListener { uri ->
                callback(uri.toString())  // Callback with the URL of the uploaded file
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserDetailsToFirestore(email: String, imageUrl: String, pdfUrl: String, description: String) {
        val user = hashMapOf(
            "email" to email,
            "imageURL" to imageUrl,
            "pdfURL" to pdfUrl,
            "description" to description,
            "role" to "organization"
        )
        FirebaseFirestore.getInstance().collection("users").document(email)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "User details saved successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving user details: ${e.message}", Toast.LENGTH_SHORT).show()
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
