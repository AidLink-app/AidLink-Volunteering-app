package com.example.welcom

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.IOException

class OrganizationRegistrationActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth  // Declare FirebaseAuth instance

    private lateinit var btnChooseImage: Button
    private lateinit var btnUploadPDF: Button
    private lateinit var etDescription: EditText
    private lateinit var btnSubmit: Button
    private var selectedImageUri: Uri? = null
    private var selectedPDFUri: Uri? = null
    private var imageUriFromFile: Uri? = null

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
            val options = arrayOf("Take Photo", "Choose from Gallery")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Choose an option")
            builder.setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        // Take a photo from the camera
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        if (intent.resolveActivity(packageManager) != null) {
                            try {
                                imageUriFromFile = FileProvider.getUriForFile(
                                    this,
                                    "com.example.welcom.fileprovider", // Match this to your app's package
                                    createImageFile() // Create a new file for storing the image
                                )
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriFromFile) // Pass the URI
                                startActivityForResult(intent, 100)
                            } catch (e: IOException) {
                                Toast.makeText(this, "Failed to create file: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    1 -> {
                        // Choose from gallery
                        val intent = Intent(Intent.ACTION_PICK)
                        intent.type = "image/*"
                        startActivityForResult(intent, 101)
                    }
                }
            }
            Log.d("SelectedImage", "Image URI: $selectedPDFUri")
            builder.show()
        }

        btnUploadPDF.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf"
            Log.d("SelectedPDF", "PDF URI: $selectedPDFUri")
            startActivityForResult(intent, 101)
        }

        btnSubmit.setOnClickListener {
            val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
            val password = findViewById<EditText>(R.id.etPassword).text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && selectedPDFUri != null && description.isNotEmpty()) {
                registerUser(email, password)
                startActivity(Intent(this, PendingApprovalActivity::class.java))
            } else {
                // Notify the user that all fields are required including the PDF and description
                Toast.makeText(this, "Email, password, a PDF, and a description are required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createImageFile(): File {
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${System.currentTimeMillis()}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }


    private fun uploadFileToFirebaseStorage(fileUri: Uri, path: String, callback: (String) -> Unit) {
        val storageReference = FirebaseStorage.getInstance().reference.child(path)
        storageReference.putFile(fileUri)
            .addOnSuccessListener {
                // Get the download URL after the upload completes successfully
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    callback(uri.toString())  // Call the callback with the download URL
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    // Handles image selection from gallery and PDF selection from file storage, initiating upload to Firebase Storage on selection.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                100 -> {
                    // Camera capture result (image saved to file via EXTRA_OUTPUT)
                    val photoUri: Uri? = imageUriFromFile // Use the URI passed with EXTRA_OUTPUT

                    if (photoUri != null) {
                        selectedImageUri = photoUri // Assign the URI to selectedImageUri
                        Log.d("SelectedImage", "Image URI from Camera: $selectedImageUri")
                        uploadFileToFirebaseStorage(photoUri, "images/${photoUri.lastPathSegment}") { imageUrl ->
                            // Handle the image URL callback if needed
                        }
                    } else {
                        Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
                    }
                }
                101 -> {
                    // Handle PDF selection
                    val pdfUri = data?.data
                    if (pdfUri != null) {
                        selectedPDFUri = pdfUri // Assign the URI to selectedPDFUri
                        Log.d("SelectedPDF", "PDF URI from selection: $selectedPDFUri")
                        uploadFileToFirebaseStorage(pdfUri, "pdfs/${pdfUri.lastPathSegment}") { pdfUrl ->
                            // Handle the PDF URL callback if needed
                        }
                    } else {
                        Toast.makeText(this, "Failed to select PDF", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }



    // Registers a new user with email and password, uploads associated image and PDF, and saves user details to Firestore.
    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Now that the Firebase Authentication user is created, save to Firestore
                val defaultImageUrl = "" // Empty string or provide a default image URL
                val description = etDescription.text.toString()

                // Save user details to Firestore immediately
                saveUserDetailsToFirestore(email, defaultImageUrl, "", description)

                // Proceed with uploading files (image and PDF) if provided
                if (selectedImageUri != null) {
                    uploadFileToFirebaseStorage(selectedImageUri!!, "images/${selectedImageUri!!.lastPathSegment}") { imageUrl ->
                        selectedPDFUri?.let { pdfUri ->
                            uploadFileToFirebaseStorage(pdfUri, "pdfs/${pdfUri.lastPathSegment}") { pdfUrl ->
                                // After files are uploaded, update Firestore with the file URLs
                                updateUserFilesInFirestore(email, imageUrl, pdfUrl)
                            }
                        }
                    }
                } else {
                    // If no image, just upload PDF and save URLs
                    selectedPDFUri?.let { pdfUri ->
                        uploadFileToFirebaseStorage(pdfUri, "pdfs/${pdfUri.lastPathSegment}") { pdfUrl ->
                            updateUserFilesInFirestore(email, defaultImageUrl, pdfUrl)
                        }
                    }
                }

                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()

                // Transition to the Pending Approval activity
                startActivity(Intent(this, PendingApprovalActivity::class.java))
            } else {
                Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun saveUserDetailsToFirestore(email: String, imageUrl: String, pdfUrl: String, description: String) {
        val user = hashMapOf(
            "email" to email,
            "imageURL" to imageUrl,
            "pdfURL" to pdfUrl,
            "description" to description,
            "role" to "organization",
            "approved" to false // Organization is unapproved by default
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

    private fun updateUserFilesInFirestore(email: String, imageUrl: String, pdfUrl: String) {
        val userUpdates = hashMapOf<String, Any>(
            "imageURL" to imageUrl,
            "pdfURL" to pdfUrl
        )
        FirebaseFirestore.getInstance().collection("users").document(email)
            .update(userUpdates)
            .addOnSuccessListener {
                Toast.makeText(this, "User files updated in Firestore", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating user files: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Checks if the READ_EXTERNAL_STORAGE permission is granted, and requests it if not. Necessary for API level 23 and above.
    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            val storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

            val permissionsNeeded = mutableListOf<String>()

            if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.CAMERA)
            }
            if (storagePermission != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }

            if (permissionsNeeded.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), 1)
            }
        }
    }

}