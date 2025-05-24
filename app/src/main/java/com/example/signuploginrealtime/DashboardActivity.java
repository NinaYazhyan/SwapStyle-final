package com.example.signuploginrealtime;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class DashboardActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private String username, name, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment);

        // Initialize user data
        username = getIntent().getStringExtra("username");
        name = getIntent().getStringExtra("name");
        email = getIntent().getStringExtra("email");

        initializeViews();
        setupToolbar();
        setupDrawer();
        setupBottomNavigation();

        // Load default fragment
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
        }
    }

    private void initializeViews() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        drawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setCheckedItem(R.id.nav_home);
            setupNavigationItemClickListener(navigationView);
        }
    }

    private void setupNavigationItemClickListener(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            // Add this log to verify clicks are detected
            Log.d("Navigation", "Item clicked: " + item.getTitle());

            if (id == R.id.nav_home) {
                Log.d("Navigation", "Home item selected");
                replaceFragment(new HomeFragment());
            } else if (id == R.id.nav_about) {
                Log.d("Navigation", "About item selected");
                showAboutUsDialog();
            }

            // Make sure this is the correct method to close drawer
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void showAboutUsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("About Us – SwapStyle");

        String message = "SwapStyle is more than just a fashion app — it's a movement. Our mission is simple:\n" +
                "Swap clothes. Save the planet.\n\n" +
                "We believe in sustainable fashion and building a community where everyone can refresh their wardrobe without harming the Earth. With SwapStyle, you can:\n\n" +
                "• Swap or donate clothes easily\n" +
                "• Reduce waste and promote eco-friendly living\n" +
                "• Connect with others who care about sustainability\n" +
                "• Find or share special outfits, including wedding dresses\n" +
                "• Support charitable causes with your donations\n\n" +
                "Together, we're creating a future where fashion is affordable, circular, and kind to the planet.\n\n" +
                "Thank you for being a part of our journey.\n" +
                "Let's swap, share, and save — one outfit at a time.";

        builder.setMessage(message);
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("SwapStyle");
            }
        }
    }

    private void setupDrawer() {
        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                        this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);

                // Add error-catching listener
                drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
                    @Override
                    public void onDrawerSlide(android.view.View drawerView, float slideOffset) {}

                    @Override
                    public void onDrawerOpened(android.view.View drawerView) {
                        Log.d("Drawer", "Drawer opened successfully");
                    }

                    @Override
                    public void onDrawerClosed(android.view.View drawerView) {
                        Log.d("Drawer", "Drawer closed successfully");
                    }

                    @Override
                    public void onDrawerStateChanged(int newState) {}
                });

                drawerLayout.addDrawerListener(toggle);
                toggle.syncState();
            } else {
                Log.e("Drawer", "Toolbar is null");
            }
        } catch (Exception e) {
            Log.e("Drawer", "Error setting up drawer", e);
        }
    }

    private void setupBottomNavigation() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setBackground(null);
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                Fragment selectedFragment = null;

                if (itemId == R.id.home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.wardrobe) {
                    selectedFragment = new WardrobeFragment();
                } else if (itemId == R.id.chat) {
                    selectedFragment = new ChatFragment();
                } else if (itemId == R.id.profile) {
                    selectedFragment = ProfileFragment.newInstance(username, name, email);
                }

                if (selectedFragment != null) {
                    replaceFragment(selectedFragment);
                }
                return true;
            });
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_lay, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        try {
            if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        } catch (Exception e) {
            Log.e("Drawer", "Error in onBackPressed", e);
            super.onBackPressed();
        }
    }
}