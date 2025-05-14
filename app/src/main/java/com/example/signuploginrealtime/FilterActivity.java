package com.example.signuploginrealtime;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.signuploginrealtime.R;

public class FilterActivity extends AppCompatActivity {


    private Spinner spinnerCategory, spinnerSubCategory, spinnerSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        // Initialize views
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerSubCategory = findViewById(R.id.spinnerSubCategory);
        spinnerSize = findViewById(R.id.spinnerSize);
        Button btnApplyFilter = findViewById(R.id.btnApplyFilter);

        // Set up category spinner
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.categories,
                android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Set up size spinner
        ArrayAdapter<CharSequence> sizeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.sizes,
                android.R.layout.simple_spinner_item);
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSize.setAdapter(sizeAdapter);

        // Handle category changes to update subcategories
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSubcategories(spinnerCategory.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Initialize with default subcategories
        updateSubcategories(spinnerCategory.getSelectedItem().toString());

        btnApplyFilter.setOnClickListener(v -> applyFilters());
    }

    private void updateSubcategories(String category) {
        int subCatArrayId;
        if (category.equals("Women's")) {
            subCatArrayId = R.array.subcategories_women;
        } else if (category.equals("Men's")) {
            subCatArrayId = R.array.subcategories_men;
        } else { // Kids
            subCatArrayId = R.array.subcategories_kids;
        }

        ArrayAdapter<CharSequence> subCatAdapter = ArrayAdapter.createFromResource(
                this,
                subCatArrayId,
                android.R.layout.simple_spinner_item);
        subCatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubCategory.setAdapter(subCatAdapter);
    }

    private void applyFilters() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("category", spinnerCategory.getSelectedItem().toString());
        resultIntent.putExtra("subCategory", spinnerSubCategory.getSelectedItem().toString());
        resultIntent.putExtra("size", spinnerSize.getSelectedItem().toString());
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}