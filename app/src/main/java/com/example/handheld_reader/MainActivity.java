package com.example.handheld_reader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.fragment.DeviceList;
import com.example.fragment.History;
import com.example.fragment.Home;
import com.example.fragment.RFIDLocation;
import com.example.fragment.RFIDScan;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends BaseTabFragmentActivity implements NavigationView.OnNavigationItemSelectedListener{
    DrawerLayout drawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open_nav,R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Home()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        String url = BuildConfig.BASE_URL;
        Log.d("URL", url);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_menu,menu);
        return true;

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.nav_home){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new RFIDScan()).commit();
            getSupportActionBar().setTitle("Home");
//            startActivity(new Intent(this, RFIDScan.class));
        }
        else if(item.getItemId() == R.id.nav_location) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new RFIDLocation()).commit();
            getSupportActionBar().setTitle("Location");
//            startActivity(new Intent(this, RFIDLocation.class));
        }
        else if(item.getItemId() == R.id.nav_devicelist) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DeviceList()).commit();
            getSupportActionBar().setTitle("Device List");
        }
        else if(item.getItemId() == R.id.nav_settings) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new History()).commit();
            getSupportActionBar().setTitle("Settings");
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }

    }
}