package com.example.welcom;

public interface FirestoreCallback {
    void onSuccess(Object result);
    void onFailure(Exception e);
}
