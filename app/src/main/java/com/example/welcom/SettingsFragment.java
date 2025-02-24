package com.example.welcom;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Button editProfileButton = view.findViewById(R.id.btnEditProfile);
        Switch notificationsSwitch = view.findViewById(R.id.switchNotifications);
        Button changeThemeButton = view.findViewById(R.id.btnChangeTheme);
        Button signOutButton = view.findViewById(R.id.btnSignOut);

        editProfileButton.setOnClickListener(v -> navigateToProfileEdit());
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> toggleNotifications(isChecked));
        changeThemeButton.setOnClickListener(v -> toggleAppTheme());
        signOutButton.setOnClickListener(v -> signOut());

        return view;
    }

    private void navigateToProfileEdit() {
        // Navigate to a profile edit fragment or activity
    }

    private void toggleNotifications(boolean enable) {
        // Enable or disable notifications
    }

    private void toggleAppTheme() {
        // Toggle between light and dark theme
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(getActivity(), "Signed out successfully!", Toast.LENGTH_SHORT).show();
        UserSession.clearUser();
        goToLoginScreen();
    }

    private void goToLoginScreen() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}