package com.example.welcom;

import java.util.Map;

public interface FirestoreService {
    void saveUser(String userId, Map<String, Object> userData, FirestoreCallback callback);

    void fetchUser(String userId, FirestoreCallback callback);
}