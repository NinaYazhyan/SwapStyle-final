package com.example.signuploginrealtime;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class AppController extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
    }
}