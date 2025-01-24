package com.example.welcom;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;

public class UserSession {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_USER = "user";

    private static User user;
    private static SharedPreferences sharedPreferences;
    private static Gson gson = new Gson();

    private UserSession() {}

    // Initialize the UserSession (call this in Application class or MainActivity on app start)
    public static void init(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
    }

    // Get the current user
    public static User getUser() {
        if (user == null) {
            user = getUserFromPreferences();
        }
        return user;
    }

    // Set the current user and save it in SharedPreferences
    public static void setUser(User user) {
        UserSession.user = user;
        saveUserToPreferences(user);
    }

    // Clear the user data from memory and SharedPreferences
    public static void clearUser() {
        user = null;
        if (sharedPreferences != null) {
            sharedPreferences.edit().remove(KEY_USER).apply();
        }
    }

    // Save the user object to SharedPreferences
    private static void saveUserToPreferences(User user) {
        if (sharedPreferences != null) {
            String userJson = gson.toJson(user);
            sharedPreferences.edit().putString(KEY_USER, userJson).apply();
        }
    }

    // Retrieve the user object from SharedPreferences
    private static User getUserFromPreferences() {
        if (sharedPreferences != null) {
            String userJson = sharedPreferences.getString(KEY_USER, null);
            if (userJson != null) {
                return gson.fromJson(userJson, User.class);
            }
        }
        return null;
    }
}

