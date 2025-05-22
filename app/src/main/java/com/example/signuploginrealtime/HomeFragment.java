package com.example.signuploginrealtime;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

        loadWardrobeItems();

        return view;
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
        if (item.getUserId() != null) {
            ChatActivity.start(requireContext(), item.getUserId(), item.getUserName());
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
}