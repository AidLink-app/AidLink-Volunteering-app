package com.example.welcom

import Organization
import PendingOrganizationsAdapter
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PendingOrganizationsAdapter
    private val organizations = mutableListOf<Organization>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Create an adapter for the pending organizations
        adapter = PendingOrganizationsAdapter(organizations) { organization ->
            approveOrganization(organization)
        }
        recyclerView.adapter = adapter

        fetchPendingOrganizations()
    }

    // Fetch pending organizations where "approved" == false
    private fun fetchPendingOrganizations() {
        db.collection("users")
            .whereEqualTo("approved", false)
            .get()
            .addOnSuccessListener { querySnapshot ->
                organizations.clear()
                for (document in querySnapshot) {
                    val org = document.toObject(Organization::class.java)
                    organizations.add(org)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load organizations: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Approve the organization and update Firestore
    private fun approveOrganization(organization: Organization) {
        db.collection("users").document(organization.email)
            .update("approved", true)
            .addOnSuccessListener {
                Toast.makeText(this, "Organization approved", Toast.LENGTH_SHORT).show()
                fetchPendingOrganizations() // Refresh the list after approval
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to approve organization: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
