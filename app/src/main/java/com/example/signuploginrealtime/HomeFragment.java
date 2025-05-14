package com.example.signuploginrealtime;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final int FILTER_REQUEST_CODE = 1;
    private RecyclerView recyclerView;
    private WardrobeAdapter adapter;
    private List<WardrobeItem> itemList;
    private List<WardrobeItem> filteredItemList;
    private DatabaseReference databaseRef;
    private EditText searchEditText;

    private String selectedCategory = "";
    private String selectedSubCategory = "";
    private String selectedSize = "";

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize components
        recyclerView = root.findViewById(R.id.wardrobeRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        itemList = new ArrayList<>();
        filteredItemList = new ArrayList<>();
        adapter = new WardrobeAdapter(getContext(), filteredItemList);
        recyclerView.setAdapter(adapter);

        databaseRef = FirebaseDatabase.getInstance().getReference("wardrobe");

        // Initialize search
        searchEditText = root.findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFiltersAndSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Set up Filter button click listener
        Button btnFilter = root.findViewById(R.id.btnFilter);
        btnFilter.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FilterActivity.class);
            startActivityForResult(intent, FILTER_REQUEST_CODE);
        });

        // Fetch data from Firebase
        fetchDataFromFirebase();

        return root;
    }

    private void fetchDataFromFirebase() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    WardrobeItem item = data.getValue(WardrobeItem.class);
                    if (item != null) {
                        itemList.add(item);
                    }
                }
                applyFiltersAndSearch();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Failed to load items: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFiltersAndSearch() {
        filteredItemList.clear();
        String searchQuery = searchEditText.getText().toString().toLowerCase().trim();

        for (WardrobeItem item : itemList) {
            boolean matchesCategory = selectedCategory.isEmpty() ||
                    (item.getCategory() != null && item.getCategory().equalsIgnoreCase(selectedCategory));

            boolean matchesSubCategory = selectedSubCategory.isEmpty() ||
                    (item.getSubcategory() != null && item.getSubcategory().equalsIgnoreCase(selectedSubCategory));

            boolean matchesSize = selectedSize.isEmpty() ||
                    (item.getSize() != null && item.getSize().equalsIgnoreCase(selectedSize));

            boolean matchesSearch = searchQuery.isEmpty() ||
                    (item.getTitle() != null && item.getTitle().toLowerCase().contains(searchQuery)) ||
                    (item.getDescription() != null && item.getDescription().toLowerCase().contains(searchQuery));

            if (matchesCategory && matchesSubCategory && matchesSize && matchesSearch) {
                filteredItemList.add(item);
            }
        }

        if (filteredItemList.isEmpty()) {
            Toast.makeText(getActivity(), "No items match your filters", Toast.LENGTH_SHORT).show();
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILTER_REQUEST_CODE && resultCode == getActivity().RESULT_OK && data != null) {
            selectedCategory = data.getStringExtra("category");
            selectedSubCategory = data.getStringExtra("subCategory");
            selectedSize = data.getStringExtra("size");

            Toast.makeText(getActivity(), "Filters Applied:\nCategory: " + selectedCategory +
                    "\nSubcategory: " + selectedSubCategory + "\nSize: " + selectedSize, Toast.LENGTH_LONG).show();
            applyFiltersAndSearch();
        }
    }
}