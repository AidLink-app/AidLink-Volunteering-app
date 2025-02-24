package com.example.welcom;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private List<Post> postList;
    private EditText searchEditText;
    private List<Post> filteredList = new ArrayList<>(); // List for search filtering
    private FirebaseFirestore db;
    private User user;
    private String userRole;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();

        setupRecyclerView(view);
        setupSearchEditText(view);

        postList = new ArrayList<>();
        user = UserSession.getUser();
        userRole = user.getRole();
        adapter = new PostAdapter(filteredList, post -> {
            // Remove the post from both postList and filteredList
            postList.remove(post);
            filteredList.remove(post);
            adapter.notifyDataSetChanged();
        }, userRole);

        recyclerView.setAdapter(adapter);

        fetchPostsFromFirestore();

        return view;
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupSearchEditText(View view) {
        searchEditText = view.findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPosts(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchPostsFromFirestore() {
        db.collection("posts")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    postList.clear();

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Object dateField = document.get("date");


                        // Skip posts with missing or incorrect date formats
                        if (dateField == null) {
                            Log.e("Firestore", "Skipping post: date is null in document " + document.getId());
                            continue;
                        }

                        if (dateField instanceof String) {
                            Log.e("Firestore", "Skipping post: date is String instead of Timestamp in document " + document.getId());
                            continue;
                        }

                        if (!(dateField instanceof com.google.firebase.Timestamp)) {
                            Log.e("Firestore", "Skipping post: date is not a valid Timestamp in document " + document.getId());
                            continue;
                        }

                        com.google.firebase.Timestamp ts = (com.google.firebase.Timestamp) dateField;

                        // Ignore posts with past events
                        if (ts.toDate().before(new Date())) {
                            Log.e("Firestore", "Skipping post: date is in the past for document " + document.getId());
                            continue;
                        }

                        // Parse the post normally
                        Post post = document.toObject(Post.class);
                        post.setDate(ts); // Set correct Timestamp
                        post.setCreatorEmail("OrganizationEmail");
                        // If we reached here, we add the post
                        postList.add(post);
                    }

                    filteredList.clear();
                    filteredList.addAll(postList);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching posts", e);
                });
    }

    private void filterPosts(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(postList); // Show all posts if search query is empty
        } else {
            for (Post post : postList) {
                if (post.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        post.getDescription().toLowerCase().contains(query.toLowerCase()) ||
                        post.getCategory().toLowerCase().contains(query.toLowerCase()) ||
                        post.getOrganization().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(post);
                }
            }
        }
        adapter.notifyDataSetChanged(); // Refresh RecyclerView with filtered data
    }

    private void onPostInteraction(Post post) {
        postList.remove(post);
        filteredList.remove(post);
        adapter.notifyDataSetChanged();
    }
}
