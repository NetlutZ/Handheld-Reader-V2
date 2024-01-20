package com.example.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.handheld_reader.MainActivity;
import com.example.handheld_reader.R;
import com.example.session.SessionManagement;

public class Home extends Fragment {
    Button borrowButton, returnButton, deviceListButton, findDeviceButton;
    Activity activity = getActivity();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }

        if (getActivity() != null && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Home");
        }
        DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        borrowButton = getView().findViewById(R.id.home_borrow_button);
        returnButton = getView().findViewById(R.id.home_return_button);
        deviceListButton = getView().findViewById(R.id.home_device_list_button);
        findDeviceButton = getView().findViewById(R.id.home_find_device_button);

        borrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("function", "borrow");
                RFIDScan rfidScanFragment = new RFIDScan();
                rfidScanFragment.setArguments(bundle);
                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, rfidScanFragment).commit();
            }
        });

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("function", "return");
                RFIDScan rfidScanFragment = new RFIDScan();
                rfidScanFragment.setArguments(bundle);
                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, rfidScanFragment).commit();
            }
        });

        deviceListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DeviceList()).commit();
            }
        });

        findDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new RFIDLocation()).commit();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Context context = getActivity();
        SharedPreferences pref = getActivity().getSharedPreferences("session", Context.MODE_PRIVATE);
        int username = pref.getInt("session_user_id", 0);
        Log.d("Home", "onStart: " + username);

    }




}