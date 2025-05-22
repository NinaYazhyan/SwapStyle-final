package com.example.signuploginrealtime;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

public class UploadActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    private Uri imageUri;
    private ImageView imgPreview;
    private Button btnAddPhotos, btnUpload, btnDiscard;
    private EditText inputTitle, inputDescription;
    private Spinner spinnerCategory, spinnerSubcategory;
    private RadioGroup sizeGroup;
    private boolean isEditMode = false;
    private String itemId;
    private DatabaseReference wardrobeRef;
    private StorageReference storageRef;
    private FirebaseUser currentUser;
    private File photoFile;

    private final Map<String, String[]> categoriesMap = new HashMap<String, String[]>() {{
        put("Women's", new String[]{"Swap", "Donation", "Wedding dress"});
        put("Men's", new String[]{"Swap", "Donation", "Formal Wear"});
        put("Kids", new String[]{"Swap", "Donation", "Special Occasion"});
    }};

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    imgPreview.setImageURI(imageUri);
                }
            });

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && imageUri != null) {
                    imgPreview.setImageURI(imageUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        wardrobeRef = FirebaseDatabase.getInstance().getReference("wardrobe");
        storageRef = FirebaseStorage.getInstance().getReference("wardrobe_images");

        initializeViews();
        setupSpinners();
        setClickListeners();

        isEditMode = getIntent().getBooleanExtra("editMode", false);
        if (isEditMode) {
            setTitle("Edit Item");
            itemId = getIntent().getStringExtra("itemId");
            populateFields();
        } else {
            setTitle("Add New Item");
        }
    }

    private void initializeViews() {
        imgPreview = findViewById(R.id.img_add_photo);
        btnAddPhotos = findViewById(R.id.btn_add_photos);
        btnUpload = findViewById(R.id.btn_upload);
        btnDiscard = findViewById(R.id.btn_discard);
        inputTitle = findViewById(R.id.input_title);
        inputDescription = findViewById(R.id.input_description);
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerSubcategory = findViewById(R.id.spinner_subcategory);
        sizeGroup = findViewById(R.id.size_group);
    }

    private void setupSpinners() {
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new ArrayList<>(categoriesMap.keySet()));
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSubcategories(parent.getItemAtPosition(position).toString());
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateSubcategories(String category) {
        String[] subcategories = categoriesMap.get(category);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, subcategories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubcategory.setAdapter(adapter);
    }

    private void setClickListeners() {
        btnAddPhotos.setOnClickListener(v -> showPhotoSourceDialog());
        btnUpload.setOnClickListener(v -> {
            if (validateInputs()) {
                if (isEditMode) {
                    updateItem();
                } else {
                    uploadItem();
                }
            }
        });
        btnDiscard.setOnClickListener(v -> showDiscardConfirmation());
    }

    private void showPhotoSourceDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Add Photo")
                .setItems(new String[]{"Gallery", "Camera"}, (dialog, which) -> {
                    if (which == 0) openGallery();
                    else checkCameraPermission();
                }).show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        try {
            photoFile = createImageFile();
            imageUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", photoFile);
            cameraLauncher.launch(imageUri);
        } catch (IOException e) {
            Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private boolean validateInputs() {
        if (inputTitle.getText().toString().trim().isEmpty()) {
            inputTitle.setError("Title is required");
            return false;
        }
        if (inputDescription.getText().toString().trim().isEmpty()) {
            inputDescription.setError("Description is required");
            return false;
        }
        if (sizeGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select a size", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (imageUri == null) {
            Toast.makeText(this, "Please add a photo", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void uploadItem() {
        StorageReference fileRef = storageRef.child(currentUser.getUid())
                .child(System.currentTimeMillis() + ".jpg");

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        fileRef.getDownloadUrl().addOnSuccessListener(uri ->
                                saveItemToDatabase(uri.toString())))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateItem() {
        if (imageUri.toString().startsWith("http")) {
            saveItemToDatabase(imageUri.toString());
        } else {
            uploadItem();
        }
    }

    // В методе saveItemToDatabase() класса UploadActivity.java добавьте следующий код
    private void saveItemToDatabase(String imageUrl) {
        String title = inputTitle.getText().toString().trim();
        String description = inputDescription.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String subcategory = spinnerSubcategory.getSelectedItem().toString();
        String size = ((RadioButton)findViewById(sizeGroup.getCheckedRadioButtonId())).getText().toString();

        // Получаем имя текущего пользователя
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = "Неизвестный пользователь";
                if (snapshot.exists() && snapshot.child("name").exists()) {
                    userName = snapshot.child("username").getValue(String.class);
                }

                WardrobeItem item = new WardrobeItem();
                item.setTitle(title);
                item.setDescription(description);
                item.setCategory(category);
                item.setSubcategory(subcategory);
                item.setSize(size);
                item.setImageUrl(imageUrl);
                item.setUserId(currentUser.getUid());
                item.setUserName(userName);

                if (isEditMode) {
                    wardrobeRef.child(itemId).setValue(item)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(UploadActivity.this, "Предмет обновлен", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(UploadActivity.this, "Ошибка обновления", Toast.LENGTH_SHORT).show());
                } else {
                    String newItemId = wardrobeRef.push().getKey();
                    item.setId(newItemId);
                    wardrobeRef.child(newItemId).setValue(item)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(UploadActivity.this, "Предмет добавлен", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(UploadActivity.this, "Ошибка добавления предмета", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UploadActivity.this, "Ошибка получения данных пользователя", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateFields() {
        if (getIntent().hasExtra("title")) inputTitle.setText(getIntent().getStringExtra("title"));
        if (getIntent().hasExtra("description")) inputDescription.setText(getIntent().getStringExtra("description"));
        if (getIntent().hasExtra("imageUrl")) {
            imageUri = Uri.parse(getIntent().getStringExtra("imageUrl"));
            Glide.with(this).load(imageUri).into(imgPreview);
        }
    }

    private void showDiscardConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Discard Changes")
                .setMessage("Are you sure you want to discard your changes?")
                .setPositiveButton("Discard", (dialog, which) -> finish())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void redirectToLogin() {
        Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}