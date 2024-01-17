package com.example.handheld_reader;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.fragment.DeviceList;
import com.example.fragment.History;
import com.example.fragment.Home;
import com.example.fragment.Login;
import com.example.fragment.RFIDLocation;
import com.example.session.SessionManagement;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends BaseTabFragmentActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static boolean isloggedIn = false;
    DrawerLayout drawerLayout;
    String URL = BuildConfig.BASE_URL;
    Menu m;
    View headerView;
    TextView headerUsername, headerEmail, headerName;
    ImageView headerImage;
    String SESSION_KEY = "session_user_id";
    String SESSION_USERNAME = "session_username";
    String SESSION_NAME = "session_name";
    String SESSION_EMAIL = "session_email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        headerView = navigationView.getHeaderView(0);
        headerUsername = (TextView) headerView.findViewById(R.id.header_username);
        headerEmail = (TextView) headerView.findViewById(R.id.header_email);
        headerName = (TextView) headerView.findViewById(R.id.header_name);
        headerImage = (ImageView) headerView.findViewById(R.id.header_image);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Home()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        String url = BuildConfig.BASE_URL;

        SessionManagement sessionManagement = new SessionManagement(MainActivity.this);
        if (sessionManagement.getSession() != -1) {
            isloggedIn = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_menu, menu);
        this.m = menu;
        updateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.user_menu_login) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new Login())
                    .commit();
        } else if (item.getItemId() == R.id.user_menu_account) {

        } else if (item.getItemId() == R.id.user_menu_history) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new History())
                    .commit();
        } else if (item.getItemId() == R.id.user_menu_logout) {
            logout(null);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        updateOptionsMenu(menu);
        if (isloggedIn) {
            SharedPreferences pref = getSharedPreferences("session", Context.MODE_PRIVATE);
            String username = pref.getString(SESSION_USERNAME, "Guest");
            String name = pref.getString(SESSION_NAME, "Name");
            String email = pref.getString(SESSION_EMAIL, "Email");

            menu.findItem(R.id.user_menu_username).setTitle(username);
            headerUsername.setText(username);
            headerEmail.setText(email);
            headerName.setText(name);
        } else {
            menu.findItem(R.id.user_menu_username).setTitle("Guest");
            headerUsername.setText("Guest");
            headerEmail.setText("Email");
            headerName.setText("Name");
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void updateOptionsMenu(Menu menu) {
        // Show/hide menu items based on login status
        menu.findItem(R.id.user_menu_login).setVisible(!isloggedIn);
        menu.findItem(R.id.user_menu_account).setVisible(isloggedIn);
        menu.findItem(R.id.user_menu_history).setVisible(isloggedIn);
        menu.findItem(R.id.user_menu_logout).setVisible(isloggedIn);
    }

    public static void updateLoginStatus(Activity activity) {
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    public void logout(View view) {
        isloggedIn = false;
        invalidateOptionsMenu();

        SessionManagement sessionManagement = new SessionManagement(MainActivity.this);
        sessionManagement.removeSession();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new Login())
                .commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_home) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Home()).commit();
            getSupportActionBar().setTitle("Home");
        } else if (item.getItemId() == R.id.nav_location) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new RFIDLocation()).commit();
            getSupportActionBar().setTitle("Location");
        } else if (item.getItemId() == R.id.nav_devicelist) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DeviceList()).commit();
            getSupportActionBar().setTitle("Device List");
        } else if (item.getItemId() == R.id.nav_settings) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new History()).commit();
            getSupportActionBar().setTitle("Settings");
        } else if (item.getItemId() == R.id.nav_test) {
//            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Login()).commit();
//            getSupportActionBar().setTitle("Test");
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }
}