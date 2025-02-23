package com.example.welcom;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.se.omapi.Session;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import java.net.PasswordAuthentication;
import java.util.Properties;




import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts;
    private Context context;
    private FirebaseFirestore db;
    private OnPostDeleteListener deleteListener;
    private String userRole;


    // Interface for handling post deletions
    public interface OnPostDeleteListener {
        void onPostDeleted(Post post);
    }

    public PostAdapter(List<Post> posts, OnPostDeleteListener deleteListener, String userRole) {
        this.posts = posts;
        this.db = FirebaseFirestore.getInstance();
        this.deleteListener = deleteListener;
        this.userRole = userRole;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view, this.userRole);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.title.setText(post.getTitle());
        holder.description.setText(post.getDescription());

        User currentUser = UserSession.getUser();
        // Get current user's email
        String currentUserEmail = currentUser.getEmail();

        // Display buttons based on whether the current user is the creator of the post
        if (post.getOrganization().equals(currentUser.getEmail())) {
            holder.btnEditPost.setVisibility(View.VISIBLE);
            holder.btnDeletePost.setVisibility(View.VISIBLE);
            holder.btnViewRegistrations.setVisibility(View.VISIBLE);
        } else {
            holder.btnEditPost.setVisibility(View.GONE);
            holder.btnDeletePost.setVisibility(View.GONE);
            if (currentUser.getRole().equals("organization")){
                holder.btnRegister.setVisibility(View.GONE);
            }
        }

        holder.btnViewRegistrations.setOnClickListener(v -> {
            showRegistrationsDialog(post.getRegisteredUsers(), post.getPostId());
        });

        if (!post.isActive()) {
            holder.btnViewRegistrations.setVisibility(View.GONE);
            holder.btnRegister.setVisibility(View.GONE);
            holder.btnEditPost.setVisibility(View.GONE);
            holder.btnDeletePost.setVisibility(View.GONE);
        }

        // 📝 **View Details Button (Toggle Details Layout)**
        holder.btnViewDetails.setOnClickListener(v -> {
            if (holder.detailsLayout.getVisibility() == View.GONE) {
                holder.detailsLayout.setVisibility(View.VISIBLE);
                holder.btnViewDetails.setText("Hide Details");
                // Convert Timestamp to a date string in "dd-MM-yyyy"
                if (post.getDate() != null) {
                    Date dateObj = post.getDate().toDate();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    String dateString = sdf.format(dateObj);
                    holder.date.setText("Date: " + dateString);
                } else {
                    holder.date.setText("Date: N/A");
                }
                holder.location.setText("Location: " + post.getLocation());
                holder.organization.setText("Organization: " + post.getOrganization());
                holder.category.setText("Category: " + post.getCategory());
                holder.imageUrl.setText("Image URL: " + post.getImageUrl());
            } else {
                holder.detailsLayout.setVisibility(View.GONE);
                holder.btnViewDetails.setText("Details");
            }
        });

        // 🛠️ Register Button
        holder.btnRegister.setOnClickListener(v -> {
            db.collection("posts")
                    .whereEqualTo("title", post.getTitle())
                    .whereEqualTo("description", post.getDescription())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();

                            if (currentUserEmail != null) {
                                // Check if the user is already registered
                                if (post.getRegisteredUsers() != null && post.getRegisteredUsers().contains(currentUserEmail)) {
                                    // User is already registered, so Unregister
                                    db.collection("posts").document(documentId)
                                            .update("registeredUsers", FieldValue.arrayRemove(currentUserEmail))
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(context, "Successfully Unregistered!", Toast.LENGTH_SHORT).show();
                                                post.getRegisteredUsers().remove(currentUserEmail); // Update local list
                                                holder.btnRegister.setText("Register"); // Change button text
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(context, "Failed to unregister: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                } else {
                                    // User is not registered, so Register
                                    db.collection("posts").document(documentId)
                                            .update("registeredUsers", FieldValue.arrayUnion(currentUserEmail))
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(context, "Successfully Registered!", Toast.LENGTH_SHORT).show();
                                                holder.btnRegister.setText("Register");
                                                // Update local list
                                                if (post.getRegisteredUsers() == null) {
                                                    post.setRegisteredUsers(new ArrayList<>());
                                                }
                                                post.getRegisteredUsers().add(currentUserEmail); // Add to local list

                                                String notificationTitle = "Registration Successful";
                                                String notificationMessage = "You have successfully registered for " + post.getTitle();
                                                createNotificationForUser(currentUserEmail, notificationTitle, notificationMessage);

//                                                // Navigate to RegistrationConfirmationActivity
//                                                Intent intent = new Intent(context, RegistrationConfirmationActivity.class);
//
//                                                // Pass the WhatsApp link of the post to the next Activity
//                                                intent.putExtra("whatsappLink", post.getWhatsapp_link()); // Ensure Post has a getWhatsappLink() method
//
//                                                // Start the RegistrationConfirmationActivity
//                                                context.startActivity(intent);
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(context, "Failed to register: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                }
                            } else {
                                Toast.makeText(context, "Error: User email not found!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(context, "Post not found!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Error fetching post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });


        // 🛠️ **Edit Post Button**
        holder.btnEditPost.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostActivity.class);
            intent.putExtra("post", post);  // Ensure that Post class implements Serializable or Parcelable
            context.startActivity(intent);
        });

        // 🗑️ **Delete Post Button**
        holder.btnDeletePost.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(context)
                    .setTitle("Delete Post")
                    .setMessage("Are you sure you want to delete this post?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        db.collection("posts")
                                .whereEqualTo("title", post.getTitle())
                                .whereEqualTo("description", post.getDescription())
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();
                                        db.collection("posts").document(documentId)
                                                .delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(context, "Post Deleted", Toast.LENGTH_SHORT).show();
                                                    deleteListener.onPostDeleted(post);
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(context, "Failed to delete post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        Toast.makeText(context, "Failed to find post for deletion", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Error retrieving post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    private void showRegistrationsDialog(List<String> registeredUsers, String postId) {
        boolean[] checkedItems = new boolean[registeredUsers.size()];
        ArrayList<String> selectedUsers = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Registered Volunteers");

        // Convert list to a string array for display in the dialog
        String[] usersArray = registeredUsers.toArray(new String[0]);
        builder.setMultiChoiceItems(usersArray, checkedItems, (dialog, which, isChecked) -> {
            // Update the selected items
            if (isChecked) {
                selectedUsers.add(registeredUsers.get(which));
            } else {
                selectedUsers.remove(registeredUsers.get(which));
            }
        });

        // Add Approve button
        builder.setPositiveButton("Approve", (dialog, which) -> {
            // Call a method to handle approval
            handleApprovals(selectedUsers, postId);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void handleApprovals(List<String> selectedUsers, String postId) {
        // Prepare the updates for Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("approvedUsers", selectedUsers);
        updates.put("activeStatus", false);  // Set the post to inactive

        db.collection("posts").document(postId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Volunteers Approved and Post Deactivated Successfully!", Toast.LENGTH_SHORT).show();
                    // Find the post in the list and update its status
                    for (Post post : posts) {
                        if (post.getPostId().equals(postId)) {
                            post.setActiveStatus(false);
                            break;
                        }
                    }

                    notifyDataSetChanged(); // Notify the adapter to re-bind all views
                    sendNotificationToApprovedUsers(selectedUsers);


                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error approving volunteers: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



    private void sendNotificationToApprovedUsers(List<String> selectedUsers) {
        for (String email : selectedUsers) {
            db.collection("users").document(email).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String fcmToken = documentSnapshot.getString("fcmToken");
                    if (fcmToken != null) {
                        sendFCMNotification(fcmToken, "You have been approved!", "Your volunteer application has been approved.");
                    }
                }
            });
        }
    }


    private void sendFCMNotification(String fcmToken, String title, String message) {
        new Thread(() -> {
            try {
                URL url = new URL("https://fcm.googleapis.com/fcm/send");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "key=BLeDgda1bbA7NAn9il3JAYhcyWVZEFqII4NG1zdeWjtK5tn6y7yAH2G2xOAwsOzF22WZw8tjWl5rwfoWJ_WCxOw");
                conn.setRequestProperty("Content-Type", "application/json");

                JSONObject json = new JSONObject();
                json.put("to", fcmToken);
                JSONObject notification = new JSONObject();
                notification.put("title", title);
                notification.put("body", message);
                json.put("notification", notification);

                OutputStream outputStream = conn.getOutputStream();
                outputStream.write(json.toString().getBytes("UTF-8"));
                outputStream.close();

                int responseCode = conn.getResponseCode();
                System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                System.out.println("Response: " + response.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, date, location, organization, category, imageUrl;
        Button btnDeletePost, btnEditPost, btnViewDetails, btnRegister; // Added btnRegister
        View detailsLayout;
        Button btnViewRegistrations;

        public PostViewHolder(@NonNull View itemView, String userRole) {
            super(itemView);
            title = itemView.findViewById(R.id.postTitle);
            description = itemView.findViewById(R.id.postDescription);
            btnDeletePost = itemView.findViewById(R.id.btnDeletePost);
            btnEditPost = itemView.findViewById(R.id.btnEditPost);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnRegister = itemView.findViewById(R.id.btnRegister); // Initialize btnRegister

            btnViewRegistrations = itemView.findViewById(R.id.btnViewRegistrations);

            if ("organization".equals(userRole)) {
                btnViewRegistrations.setVisibility(View.VISIBLE);
            } else {
                btnViewRegistrations.setVisibility(View.GONE);
            }

            if (userRole.equals("volunteer")) { // Hide for volunteers
                btnDeletePost.setVisibility(View.GONE);
                btnEditPost.setVisibility(View.GONE);
            }
            else if (userRole.equals("organization")) {
                btnDeletePost.setVisibility(View.VISIBLE);
                btnEditPost.setVisibility(View.VISIBLE);
            }
            // Details Section
            detailsLayout = itemView.findViewById(R.id.detailsLayout);
            date = itemView.findViewById(R.id.postDate);
            location = itemView.findViewById(R.id.postLocation);
            organization = itemView.findViewById(R.id.postOrganization);
            category = itemView.findViewById(R.id.postCategory);
            imageUrl = itemView.findViewById(R.id.postImageUrl);

            // Initially hide details
            detailsLayout.setVisibility(View.GONE);
        }
    }

    private void createNotificationForUser(String userId, String title, String message) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("userID", userId);
        notification.put("title", title);
        notification.put("message", message);
        notification.put("timestamp", new Date());
        notification.put("read", false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notifications")
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Notification", "DocumentSnapshot added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.w("Notification", "Error adding document", e);
                });
    }

}