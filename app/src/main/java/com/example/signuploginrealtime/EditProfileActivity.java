package com.example.signuploginrealtime;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditProfileActivity extends AppCompatActivity {

    EditText editLocation, editEmail, editUsername, editPassword;
    Button saveButton;
    String locationUser, emailUser, usernameUser, passwordUser;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_profile);
        reference = FirebaseDatabase.getInstance().getReference("usera");
        editLocation = findViewById(R.id.editLoaction);
        editEmail = findViewById(R.id.editEmail);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPpassword);
        saveButton = findViewById(R.id.saveButton);

        showData();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if(isLocationChanged() || isEmailChanged() || isPasswordChanged()){
                   Toast.makeText(EditProfileActivity.this,"Save", Toast.LENGTH_SHORT).show();
               }else{
                   Toast.makeText(EditProfileActivity.this,"No change found", Toast.LENGTH_SHORT).show();
               }

            }
        });
    }
    public boolean isLocationChanged(){
        if(!locationUser.equals((editLocation.getText().toString()))){
        reference.child(usernameUser).child("Location").setValue(editLocation.getText().toString());
        locationUser = editLocation.getText().toString();
        return true;
        }else{
            return false;
        }
    }
    public boolean isEmailChanged(){
        if(!emailUser.equals((editEmail.getText().toString()))){
            reference.child(usernameUser).child("Email").setValue(editEmail.getText().toString());
          emailUser = editEmail.getText().toString();
            return true;
        }else{
            return false;
        }
    }
    public boolean isPasswordChanged(){
        if(!passwordUser.equals((editPassword.getText().toString()))){
            reference.child(usernameUser).child("Password").setValue(editPassword.getText().toString());
        passwordUser= editPassword.getText().toString();
            return true;
        }else{
            return false;
        }
    }
    public  void showData(){
        Intent intent = getIntent();

        locationUser = intent.getStringExtra("Location");
        emailUser = intent.getStringExtra("Email");
        usernameUser = intent.getStringExtra("Username");
         passwordUser = intent.getStringExtra("Password");

         editLocation.setText(locationUser);
         editEmail.setText(emailUser);
         editUsername.setText(usernameUser);
         editPassword.setText(passwordUser);




    }
}
