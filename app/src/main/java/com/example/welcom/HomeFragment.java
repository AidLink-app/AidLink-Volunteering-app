
package com.example.welcom;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
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
private Spinner locationFilterSpinner;
private Spinner categoryFilterSpinner;
private List<Post> filteredList = new ArrayList<>(); // List for search filtering
private Map<String, String> organizationMap = new HashMap<>(); // Cache for organization names
private FirebaseFirestore db;
private String userRole;
@Nullable
@Override
public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_home, container, false);

    db = FirebaseFirestore.getInstance();

    setupRecyclerView(view);
    setupSearchEditText(view);

    postList = new ArrayList<>();
    User user = UserSession.getUser();
    userRole = user.getRole();
    adapter = new PostAdapter(filteredList, post -> {
        // Remove the post from both postList and filteredList
        postList.remove(post);
        filteredList.remove(post);
        adapter.notifyDataSetChanged();
    }, userRole);


    recyclerView.setAdapter(adapter);

    locationFilterSpinner = view.findViewById(R.id.locationFilterSpinner);
    categoryFilterSpinner = view.findViewById(R.id.categoryFilterSpinner);

    // Location filter spinner
    ArrayAdapter<CharSequence> locAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.filter_location_options,
            android.R.layout.simple_spinner_item
    );

    locAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    locationFilterSpinner.setAdapter(locAdapter);

    // Category filter spinner
    ArrayAdapter<CharSequence> catAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.filter_category_options,
            android.R.layout.simple_spinner_item
    );

    locationFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            // Re-filter whenever user picks a new location
            filterPosts(searchEditText.getText().toString());
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) { }
    });

    categoryFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            // Re-filter whenever user picks a new category
            filterPosts(searchEditText.getText().toString());
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) { }
    });


    catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    categoryFilterSpinner.setAdapter(catAdapter);


    fetchOrganizations();
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

    /**
     * Fetch organizations and store them in a map
     */
    private void fetchOrganizations() {
        db.collection("organizations")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String orgId = document.getId();
                        String orgName = document.getString("name");
                        organizationMap.put(orgId, orgName); // Store in cache
                    }
                    fetchPostsFromFirestore(); // Ensure posts are fetched after organizations
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching organizations", e);
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

                        // Match organization ID to name
                        String organizationId = document.getString("organizationId");
                        if (organizationId != null && organizationMap.containsKey(organizationId)) {
                            post.setOrganization(organizationMap.get(organizationId));
                        } else {
                            post.setOrganization("Unknown Organization");
                        }

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

    /**
     * Filter posts based on search query
     */
    private void filterPosts(String query) {
        String selectedLocation = locationFilterSpinner.getSelectedItem().toString();
        String selectedCategory = categoryFilterSpinner.getSelectedItem().toString();

        filteredList.clear();
        for (Post post : postList) {
            // Step 1: Does the text query match (title/description/category/organization)?
            boolean matchesSearchText = query.isEmpty()
                    || post.getTitle().toLowerCase().contains(query.toLowerCase())
                    || post.getDescription().toLowerCase().contains(query.toLowerCase())
                    || (post.getCategory() != null && post.getCategory().toLowerCase().contains(query.toLowerCase()))
                    || (post.getOrganization() != null && post.getOrganization().toLowerCase().contains(query.toLowerCase()));

            // Step 2: Does location match?
            //         We check post.getLocationArea(), which you set as "locationArea" in Firestore.
            //         If selectedLocation is "All", accept it. Otherwise, compare.
            boolean matchesLocation = true;
            if (!selectedLocation.equalsIgnoreCase("All")) {
                // Make sure post.getLocationArea() is not null and compare
                if (post.getLocationArea() == null
                        || !post.getLocationArea().equalsIgnoreCase(selectedLocation)) {
                    matchesLocation = false;
                }
            }

            // Step 3: Does category match?
            boolean matchesCategory = true;
            if (!selectedCategory.equalsIgnoreCase("All")) {
                if (post.getCategory() == null
                        || !post.getCategory().equalsIgnoreCase(selectedCategory)) {
                    matchesCategory = false;
                }
            }

            // If all conditions are true => add it
            if (matchesSearchText && matchesLocation && matchesCategory) {
                filteredList.add(post);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void onPostInteraction(Post post) {
        postList.remove(post);
        filteredList.remove(post);
        adapter.notifyDataSetChanged();
    }
}
