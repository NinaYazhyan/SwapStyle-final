package com.example.signuploginrealtime;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;  // Make sure Fragment is imported
import android.content.Intent;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile2, container, false);

        // Find Buttons by ID
        Button buttonMyCloset = rootView.findViewById(R.id.myCloset);
        Button buttonMyActivities = rootView.findViewById(R.id.myActivities);
        Button buttonDeleteAccount = rootView.findViewById(R.id.deleteAccount);
        Button buttonEditProfile = rootView.findViewById(R.id.editProfile);

        // Set onClickListeners for Each Button
        buttonMyCloset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open "My Closet" Activity
                Intent intent = new Intent(getActivity(), MyClosetActivity.class);
                startActivity(intent);
            }
        });

        buttonMyActivities.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open "My Activities" Activity
                Intent intent = new Intent(getActivity(), MyActivitiesActivity.class);
                startActivity(intent);
            }
        });

        buttonDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show a confirmation dialog or delete account logic
                showDeleteAccountDialog();
            }
        });
        buttonEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show a confirmation dialog or delete account logic
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                startActivity(intent);
            }
        });

        return rootView;
    }



    private void showDeleteAccountDialog() {
        // Your dialog implementation
    }
}
