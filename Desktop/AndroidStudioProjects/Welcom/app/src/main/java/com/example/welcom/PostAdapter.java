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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts;
    private Context context;
    private FirebaseFirestore db;

    public PostAdapter(List<Post> posts) {
        this.posts = posts;
        this.db = FirebaseFirestore.getInstance();
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

        // Edit Post Button
        holder.btnEditPost.setOnClickListener(v -> {
            db.collection("posts")
                    .whereEqualTo("title", post.getTitle()) // Match on unique attributes
                    .whereEqualTo("description", post.getDescription())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();
                            Intent intent = new Intent(context, EditPostActivity.class);
                            intent.putExtra("postId", documentId); // Pass document ID
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
        // 🗑️ **Delete Post Button with Confirmation Dialog**
        holder.btnDeletePost.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(context)
                    .setTitle("Delete Post")
                    .setMessage("Are you sure you want to delete this post?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        db.collection("posts")
                                .whereEqualTo("title", post.getTitle()) // Match unique attributes
                                .whereEqualTo("description", post.getDescription())
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();
                                        db.collection("posts").document(documentId)
                                                .delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(context, "Post Deleted", Toast.LENGTH_SHORT).show();
                                                    posts.remove(position);
                                                    notifyItemRemoved(position);
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

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView title, description;
        Button btnDeletePost, btnEditPost;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.postTitle);
            description = itemView.findViewById(R.id.postDescription);
            btnDeletePost = itemView.findViewById(R.id.btnDeletePost);
            btnEditPost = itemView.findViewById(R.id.btnEditPost);
        }
    }
}