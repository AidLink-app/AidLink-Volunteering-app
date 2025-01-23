package com.example.welcom;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Map;

public class FirestoreServiceImpl implements FirestoreService {
    private final FirebaseFirestore db;

    public FirestoreServiceImpl(FirebaseFirestore db) {
        this.db = db;
    }

    @Override
    public void saveUser(String userId, Map<String, Object> userData, FirestoreCallback callback) {
        db.collection("users").document(userId).set(userData)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void fetchUser(String userId, FirestoreCallback callback) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(document -> callback.onSuccess(document.getData()))
                .addOnFailureListener(callback::onFailure);
    }
}