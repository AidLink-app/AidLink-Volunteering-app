package com.example.welcom;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts;
    private Context context;
    private FirebaseFirestore db;
    private OnPostDeleteListener deleteListener;

    // Interface for handling post deletions
    public interface OnPostDeleteListener {
        void onPostDeleted(Post post);
    }

    public PostAdapter(List<Post> posts, OnPostDeleteListener deleteListener) {
        this.posts = posts;
        this.db = FirebaseFirestore.getInstance();
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.title.setText(post.getTitle());
        holder.description.setText(post.getDescription());

        // 📝 **View Details Button (Toggle Details Layout)**
        holder.btnViewDetails.setOnClickListener(v -> {
            if (holder.detailsLayout.getVisibility() == View.GONE) {
                holder.detailsLayout.setVisibility(View.VISIBLE);
                holder.btnViewDetails.setText("Hide Details");
                holder.date.setText("Date: " + post.getDate());
                holder.location.setText("Location: " + post.getLocation());
                holder.organization.setText("Organization: " + post.getOrganization());
                holder.category.setText("Category: " + post.getCategory());
                holder.imageUrl.setText("Image URL: " + post.getImageUrl());
            } else {
                holder.detailsLayout.setVisibility(View.GONE);
                holder.btnViewDetails.setText("Details");
            }
        });

        // 🛠️ **Register Button**
        holder.btnRegister.setOnClickListener(v -> {
            db.collection("posts")
                    .whereEqualTo("title", post.getTitle())
                    .whereEqualTo("description", post.getDescription())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();

                            String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                            if (currentUserEmail != null) {
                                if (post.getRegisteredUsers() != null && post.getRegisteredUsers().contains(currentUserEmail)) {
                                    db.collection("posts").document(documentId)
                                            .update("registeredUsers", FieldValue.arrayRemove(currentUserEmail))
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(context, "Successfully Unregistered!", Toast.LENGTH_SHORT).show();
                                                post.getRegisteredUsers().remove(currentUserEmail);
                                                holder.btnRegister.setText("Register");
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(context, "Failed to unregister: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                } else {
                                    db.collection("posts").document(documentId)
                                            .update("registeredUsers", FieldValue.arrayUnion(currentUserEmail))
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(context, "Successfully Registered!", Toast.LENGTH_SHORT).show();
                                                if (post.getRegisteredUsers() == null) {
                                                    post.setRegisteredUsers(new ArrayList<>());
                                                }
                                                post.getRegisteredUsers().add(currentUserEmail);
                                                holder.btnRegister.setText("Unregister");
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
            db.collection("posts")
                    .whereEqualTo("title", post.getTitle())
                    .whereEqualTo("description", post.getDescription())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();
                            Intent intent = new Intent(context, EditPostActivity.class);
                            intent.putExtra("postId", documentId);
                            intent.putExtra("postTitle", post.getTitle());
                            intent.putExtra("postDescription", post.getDescription());
                            intent.putExtra("postDate", post.getDate());
                            intent.putExtra("postLocation", post.getLocation());
                            intent.putExtra("postOrganization", post.getOrganization());
                            intent.putExtra("postCategory", post.getCategory());
                            intent.putExtra("postImageUrl", post.getImageUrl());
                            context.startActivity(intent);
                        } else {
                            Toast.makeText(context, "Failed to find post for editing", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Error retrieving post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
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

        // 🏢 **See Organization Button**
        holder.btnSeeOrganization.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrganizationProfileActivity.class);
            intent.putExtra("organizationId", post.getOrganizationId()); // Pass organizationId
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, date, location, organization, category, imageUrl;
        Button btnDeletePost, btnEditPost, btnViewDetails, btnRegister, btnSeeOrganization;
        View detailsLayout;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.postTitle);
            description = itemView.findViewById(R.id.postDescription);
            btnDeletePost = itemView.findViewById(R.id.btnDeletePost);
            btnEditPost = itemView.findViewById(R.id.btnEditPost);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnRegister = itemView.findViewById(R.id.btnRegister);
            btnSeeOrganization = itemView.findViewById(R.id.btnSeeOrganization);

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
}