package com.example.welcom;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Notifications> notificationList;

    private String userRole;

    private BottomNavigationView bottomNavigationView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.nav_notifications);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if(item.getItemId() == R.id.nav_home) {
                Intent intent2 = new Intent(this, DashboardActivity.class);
                startActivity(intent2);
            }
            if(item.getItemId() == R.id.nav_settings) {
                return true;
            }
            if (item.getItemId() == R.id.nav_notifications) {
                return true;
            }
            if(item.getItemId() ==R.id.nav_profile) {
                return true;
            }
            if(item.getItemId() == R.id.nav_addpost) {
                Intent intent = new Intent(NotificationsActivity.this, AddPostActivity.class);
                startActivity(intent);
            }
            return false;
        });

        recyclerView = findViewById(R.id.notifications_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);
        loadNotifications();
    }

    private void loadNotifications() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("notifications")
                    .whereEqualTo("userID", currentUser.getEmail())
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            notificationList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Notifications notification = document.toObject(Notifications.class);
                                notificationList.add(notification);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.e("NotificationsActivity", "Error loading notifications", task.getException());
                        }
                    });
        }
    }
}
