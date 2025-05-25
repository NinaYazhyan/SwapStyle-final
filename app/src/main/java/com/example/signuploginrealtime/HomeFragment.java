package com.example.signuploginrealtime;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final int FILTER_REQUEST_CODE = 200;

    private RecyclerView recyclerView;
    private WardrobeAdapter adapter;
    private List<WardrobeItem> itemList;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private Button btnFilter;
    private EditText searchEditText;
    private DatabaseReference wardrobeRef;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firebase reference
        wardrobeRef = FirebaseDatabase.getInstance().getReference("wardrobe");

        // Initialize views
        recyclerView = view.findViewById(R.id.wardrobeRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        btnFilter = view.findViewById(R.id.btnFilter);
        searchEditText = view.findViewById(R.id.searchEditText);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        itemList = new ArrayList<>();
        adapter = new WardrobeAdapter(getContext(), itemList, this::startChatWithUser);
        recyclerView.setAdapter(adapter);

        // Set filter button click listener
        btnFilter.setOnClickListener(v -> openFilterActivity());

        // Add text change listener for search functionality
        searchEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                filterBySearch(s.toString());
            }
        });

        loadWardrobeItems();

        return view;
    }

    private void filterBySearch(String searchText) {
        if (itemList == null || itemList.isEmpty()) {
            return;
        }

        List<WardrobeItem> filteredList = new ArrayList<>();

        for (WardrobeItem item : itemList) {
            if (item.getTitle().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(item);
            }
        }

        adapter.updateList(filteredList);

        if (filteredList.isEmpty()) {
            emptyStateText.setText("No items match your search");
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            emptyStateText.setVisibility(View.GONE);
        }
    }

    private void loadWardrobeItems() {
        progressBar.setVisibility(View.VISIBLE);

        wardrobeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                itemList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    WardrobeItem item = snapshot.getValue(WardrobeItem.class);
                    if (item != null) {
                        item.setId(snapshot.getKey());
                        itemList.add(item);
                    }
                }

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                if (itemList.isEmpty()) {
                    emptyStateText.setVisibility(View.VISIBLE);
                } else {
                    emptyStateText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load items", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void startChatWithUser(WardrobeItem item) {
        // Check if user is trying to message themselves
        if (item.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            Toast.makeText(getContext(), "You cannot message yourself", Toast.LENGTH_SHORT).show();
        } else {
            // Create intent with recipient's user ID, username, and product information
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("RECIPIENT_ID", item.getUserId());
            intent.putExtra("RECIPIENT_NAME", item.getUserName());

            // Add product information
            intent.putExtra("PRODUCT_ID", item.getId());
            intent.putExtra("PRODUCT_TITLE", item.getTitle());
            intent.putExtra("PRODUCT_IMAGE_URL", item.getImageUrl());
            intent.putExtra("AUTO_SEND_PRODUCT", true);

            startActivity(intent);
        }
    }

    private void openFilterActivity() {
        Intent intent = new Intent(getActivity(), FilterActivity.class);
        startActivityForResult(intent, FILTER_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILTER_REQUEST_CODE && resultCode == getActivity().RESULT_OK && data != null) {
            String category = data.getStringExtra("category");
            String subCategory = data.getStringExtra("subCategory");
            String size = data.getStringExtra("size");

            applyFilters(category, subCategory, size);
        }
    }

    private void applyFilters(String category, String subCategory, String size) {
        progressBar.setVisibility(View.VISIBLE);

        wardrobeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                itemList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    WardrobeItem item = snapshot.getValue(WardrobeItem.class);

                    if (item != null) {
                        // Apply filters
                        boolean categoryMatch = category.equals("All") || item.getCategory().equals(category);
                        boolean subCategoryMatch = subCategory.equals("All") || item.getSubcategory().equals(subCategory);
                        boolean sizeMatch = size.equals("All") || item.getSize().equals(size);

                        if (categoryMatch && subCategoryMatch && sizeMatch) {
                            item.setId(snapshot.getKey());
                            itemList.add(item);
                        }
                    }
                }

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                if (itemList.isEmpty()) {
                    emptyStateText.setText("No items match your filters");
                    emptyStateText.setVisibility(View.VISIBLE);
                } else {
                    emptyStateText.setVisibility(View.GONE);
                }

                Toast.makeText(getContext(), "Filters applied", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to apply filters", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    // New method for showing image dialog
    public void showImageDialog(ImageView imageView) {
        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_image_view, null);
        builder.setView(dialogView);

        // Get dialog views
        ImageView dialogImageView = dialogView.findViewById(R.id.dialog_image_view);
        Button closeButton = dialogView.findViewById(R.id.dialog_close_button);

        // Set the image from the clicked ImageView
        dialogImageView.setImageDrawable(imageView.getDrawable());

        // Create and show the dialog
        AlertDialog dialog = builder.create();

        // Set up close button
        closeButton.setOnClickListener(v -> dialog.dismiss());

        // Show dialog
        dialog.show();
    }

    // Alternative method that accepts image URL
    public void showImageDialog(String imageUrl) {
        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_image_view, null);
        builder.setView(dialogView);

        // Get dialog views
        ImageView dialogImageView = dialogView.findViewById(R.id.dialog_image_view);
        Button closeButton = dialogView.findViewById(R.id.dialog_close_button);

        // Load the image using Glide
        Glide.with(this)
                .load(imageUrl)
                .into(dialogImageView);

        // Create and show the dialog
        AlertDialog dialog = builder.create();

        // Set up close button
        closeButton.setOnClickListener(v -> dialog.dismiss());

        // Show dialog
        dialog.show();
    }
}