package com.example.signuploginrealtime;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {
    private String username, name, email, password, location;
    private DatabaseReference userRef;

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

        // Initialize buttons
        Button buttonEditProfile = rootView.findViewById(R.id.editProfile);
        Button buttonDeleteAccount = rootView.findViewById(R.id.deleteAccount);
        Button buttonRateUs = rootView.findViewById(R.id.rateUs);
        Button buttonSupport = rootView.findViewById(R.id.support);

        // Edit Profile Button (from original code)
        buttonEditProfile.setOnClickListener(v -> fetchUserDataAndLaunchEdit());

        // Rate Us Button (from first code)
        buttonRateUs.setOnClickListener(v -> showRatingDialog());

        // Support Button (from first code)
        buttonSupport.setOnClickListener(v -> showSupportDialog());

        // Delete Account Button (from first code)
        buttonDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());

        return rootView;
    }

    // All original methods from second code remain unchanged
    private void fetchUserDataAndLaunchEdit() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String userId = userSnapshot.getKey();
                        if (userSnapshot.child("password").exists()) {
                            password = userSnapshot.child("password").getValue(String.class);
                        }
                        if (userSnapshot.child("location").exists()) {
                            location = userSnapshot.child("location").getValue(String.class);
                        }
                        launchEditProfileActivity(userId);
                        return;
                    }
                } else {
                    Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Database error: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void launchEditProfileActivity(String userId) {
        Intent intent = new Intent(getActivity(), EditProfileActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("username", username);
        intent.putExtra("name", name);
        intent.putExtra("email", email);
        intent.putExtra("password", password);
        intent.putExtra("location", location);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        if (username == null || username.isEmpty()) return;

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        if (userSnapshot.child("name").exists()) {
                            name = userSnapshot.child("name").getValue(String.class);
                            TextView profileName = getView().findViewById(R.id.profileName);
                            profileName.setText(name);
                        }
                        if (userSnapshot.child("email").exists()) {
                            email = userSnapshot.child("email").getValue(String.class);
                            TextView profileEmail = getView().findViewById(R.id.profileEmail);
                            profileEmail.setText(email);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Database error: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Added methods from first code (Rate Us, Support, Delete Account)
    private void showRatingDialog() {
        RatingBar ratingBar = new RatingBar(requireContext(), null, android.R.attr.ratingBarStyle);
        ratingBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1);
        ratingBar.setRating(0);
        ratingBar.setIsIndicator(false);

        try {
            ratingBar.setProgressTintList(getResources().getColorStateList(R.color.rating_bar));
        } catch (Exception e) {
            e.printStackTrace();
        }

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(100, 50, 100, 50);
        layout.addView(ratingBar);

        new AlertDialog.Builder(requireContext())
                .setTitle("Rate Our App")
                .setView(layout)
                .setPositiveButton("Submit", (dialog, which) -> {
                    int rating = (int) ratingBar.getRating();
                    Toast.makeText(requireContext(), "Thanks for your " + rating + " star rating!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showSupportDialog() {
        TextView message = new TextView(requireContext());
        message.setTextSize(16);
        message.setLineSpacing(0, 1.2f);
        message.setPadding(50, 30, 50, 30);
        message.setText(
                "SwapStyle - Sustainable Fashion App\n\n" +
                        "• Home: Browse available clothing items\n" +
                        "• Wardrobe: Manage your listed items\n" +
                        "• Chat: Message with other users\n" +
                        "• Profile: Account settings and info\n\n" +
                        "Contact support: help@swapstyle.com"
        );

        new AlertDialog.Builder(requireContext())
                .setTitle("App Support")
                .setView(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to permanently delete your account?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    Toast.makeText(requireContext(), "Account deleted", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}