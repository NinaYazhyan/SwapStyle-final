package com.example.signuploginrealtime;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {
    private String username, name, email,password, location;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String username, String name, String email) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString("username", username);
        args.putString("name", name);
        args.putString("email", email);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile2, container, false);

        if (getArguments() != null) {
            username = getArguments().getString("username");
            name = getArguments().getString("name");
            email = getArguments().getString("email");
        }

        TextView profileName = rootView.findViewById(R.id.profileName);
        TextView profileEmail = rootView.findViewById(R.id.profileEmail);
        profileName.setText(name);
        profileEmail.setText(email);

        // The edit profile button launches EditProfileActivity
        Button buttonEditProfile = rootView.findViewById(R.id.editProfile);
        buttonEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("name", name);
            intent.putExtra("email", email);
            intent.putExtra("password", password);
            intent.putExtra("location", location);
            startActivity(intent);
        });

        return rootView;
    }
}