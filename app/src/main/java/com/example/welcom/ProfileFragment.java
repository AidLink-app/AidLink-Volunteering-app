package com.example.welcom;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        TextView userName = view.findViewById(R.id.tvUserName);
        TextView userEmail = view.findViewById(R.id.tvUserEmail);
        TextView userRole = view.findViewById(R.id.tvUserRole);

        User user = UserSession.getUser();
        userName.setText(user.getName());
        userEmail.setText(user.getEmail());
        userRole.setText(user.getRole());
        return view;
    }
}