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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements WardrobeAdapter.OnMessageClickListener {

    private static final int FILTER_REQUEST_CODE = 1;
    private RecyclerView recyclerView;
    private WardrobeAdapter adapter;
    private List<WardrobeItem> itemList;
    private List<WardrobeItem> filteredItemList;
    private DatabaseReference databaseRef;
    private DatabaseReference usersRef;
    private EditText searchEditText;
    private FirebaseUser currentUser;

    private String selectedCategory = "";
    private String selectedSubCategory = "";
    private String selectedSize = "";

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Получаем текущего пользователя
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize components
        recyclerView = root.findViewById(R.id.wardrobeRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        itemList = new ArrayList<>();
        filteredItemList = new ArrayList<>();
        adapter = new WardrobeAdapter(getContext(), filteredItemList, this);
        recyclerView.setAdapter(adapter);

        databaseRef = FirebaseDatabase.getInstance().getReference("wardrobe");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

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
                        // Загружаем информацию о пользователе
                        fetchUserInfo(item);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Не удалось загрузить предметы: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserInfo(WardrobeItem item) {
        if (item.getUserId() != null) {
            usersRef.child(item.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String userName = snapshot.child("name").getValue(String.class);
                        item.setUserName(userName);
                    } else {
                        item.setUserName("Неизвестный пользователь");
                    }

                    // Добавляем предмет после получения информации о пользователе
                    itemList.add(item);
                    applyFiltersAndSearch();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    item.setUserName("Неизвестный пользователь");
                    itemList.add(item);
                    applyFiltersAndSearch();
                }
            });
        } else {
            item.setUserName("Неизвестный пользователь");
            itemList.add(item);
            applyFiltersAndSearch();
        }
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

            // Не показываем собственные предметы пользователя
            boolean isNotOwnItem = currentUser == null || !item.getUserId().equals(currentUser.getUid());

            if (matchesCategory && matchesSubCategory && matchesSize && matchesSearch && isNotOwnItem) {
                filteredItemList.add(item);
            }
        }

        if (filteredItemList.isEmpty() && !itemList.isEmpty()) {
            Toast.makeText(getActivity(), "Нет предметов, соответствующих вашим фильтрам", Toast.LENGTH_SHORT).show();
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

            Toast.makeText(getActivity(), "Применены фильтры:\nКатегория: " + selectedCategory +
                    "\nПодкатегория: " + selectedSubCategory + "\nРазмер: " + selectedSize, Toast.LENGTH_LONG).show();
            applyFiltersAndSearch();
        }
    }

    @Override
    public void onMessageClick(WardrobeItem item) {
        if (item.getUserId() == null || item.getUserId().isEmpty()) {
            Toast.makeText(getActivity(), "Невозможно отправить сообщение. Информация о пользователе отсутствует.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser == null) {
            Toast.makeText(getActivity(), "Пожалуйста, войдите в систему, чтобы отправлять сообщения", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser.getUid().equals(item.getUserId())) {
            Toast.makeText(getActivity(), "Вы не можете отправить сообщение самому себе", Toast.LENGTH_SHORT).show();
            return;
        }

        // Запускаем активность чата
        ChatActivity.start(requireContext(), item.getUserId(), item.getUserName());
    }
}