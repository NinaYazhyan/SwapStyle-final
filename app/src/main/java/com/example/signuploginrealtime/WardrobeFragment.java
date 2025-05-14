package com.example.signuploginrealtime;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class WardrobeFragment extends Fragment {
    private static final int UPLOAD_REQUEST_CODE = 100;
    private static final int EDIT_REQUEST_CODE = 101;

    private LinearLayout wardrobeContainer;
    private LinearLayout emptyStateContainer;
    private ImageView uploadIcon;
    private DatabaseReference wardrobeRef;
    private List<WardrobeItem> wardrobeItems = new ArrayList<>();
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wardrode, container, false);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return view;
        }

        wardrobeRef = FirebaseDatabase.getInstance().getReference("wardrobe");

        wardrobeContainer = view.findViewById(R.id.wardrobeContainer);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        uploadIcon = view.findViewById(R.id.uploadIcon);

        // Set up Floating Action Button
        FloatingActionButton fabAddItem = view.findViewById(R.id.fab_add_item);
        fabAddItem.setOnClickListener(v -> {
            startActivityForResult(new Intent(getActivity(), UploadActivity.class), UPLOAD_REQUEST_CODE);
        });

        uploadIcon.setOnClickListener(v -> {
            startActivityForResult(new Intent(getActivity(), UploadActivity.class), UPLOAD_REQUEST_CODE);
        });

        loadWardrobeItems();
        return view;
    }

    private void loadWardrobeItems() {
        wardrobeRef.orderByChild("userId").equalTo(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        wardrobeItems.clear();
                        wardrobeContainer.removeAllViews();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            WardrobeItem item = snapshot.getValue(WardrobeItem.class);
                            if (item != null) {
                                item.setId(snapshot.getKey());
                                wardrobeItems.add(item);
                                addItemToView(item);
                            }
                        }

                        updateEmptyState();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getContext(), "Failed to load items", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addItemToView(WardrobeItem item) {
        View itemView = LayoutInflater.from(getContext())
                .inflate(R.layout.wardrobe_item_layout, wardrobeContainer, false);

        ImageView itemImage = itemView.findViewById(R.id.item_image);
        TextView itemTitle = itemView.findViewById(R.id.item_title);
        TextView itemDetails = itemView.findViewById(R.id.item_details);
        ImageView viewIcon = itemView.findViewById(R.id.view_icon);
        ImageView editIcon = itemView.findViewById(R.id.edit_icon);
        ImageView deleteIcon = itemView.findViewById(R.id.delete_icon);

        Glide.with(this)
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_placeholder)
                .centerCrop()
                .into(itemImage);

        itemTitle.setText(item.getTitle());
        itemDetails.setText(String.format("%s • %s", item.getSize(), item.getCategory()));

        viewIcon.setOnClickListener(v -> showItemDetails(item));
        editIcon.setOnClickListener(v -> editItem(item));
        deleteIcon.setOnClickListener(v -> showDeleteConfirmation(item));

        wardrobeContainer.addView(itemView);
    }

    private void showItemDetails(WardrobeItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle(item.getTitle())
                .setMessage(String.format(
                        "Category: %s\nSubcategory: %s\nSize: %s\n\n%s",
                        item.getCategory(),
                        item.getSubcategory(),
                        item.getSize(),
                        item.getDescription()))
                .setPositiveButton("OK", null)
                .show();
    }

    private void editItem(WardrobeItem item) {
        Intent intent = new Intent(getActivity(), UploadActivity.class);
        intent.putExtra("editMode", true);
        intent.putExtra("itemId", item.getId());
        intent.putExtra("title", item.getTitle());
        intent.putExtra("description", item.getDescription());
        intent.putExtra("category", item.getCategory());
        intent.putExtra("subcategory", item.getSubcategory());
        intent.putExtra("size", item.getSize());
        intent.putExtra("imageUrl", item.getImageUrl());
        startActivityForResult(intent, EDIT_REQUEST_CODE);
    }

    private void showDeleteConfirmation(WardrobeItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("OK", (dialog, which) -> deleteItem(item))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteItem(WardrobeItem item) {
        wardrobeRef.child(item.getId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Item deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to delete item", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateEmptyState() {
        if (wardrobeItems.isEmpty()) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            wardrobeContainer.setVisibility(View.GONE);
            uploadIcon.setVisibility(View.VISIBLE);
        } else {
            emptyStateContainer.setVisibility(View.GONE);
            wardrobeContainer.setVisibility(View.VISIBLE);
            uploadIcon.setVisibility(View.GONE);
        }
    }

    private void redirectToLogin() {
        Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getActivity(), LoginActivity.class));
        requireActivity().finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            loadWardrobeItems();
        }
    }
}