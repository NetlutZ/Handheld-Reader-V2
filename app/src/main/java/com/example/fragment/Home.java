package com.example.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
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
    Button button;
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
        button = getActivity().findViewById(R.id.button_home);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout(v);
            }
        });
    }

    public void logout(View view) {
        SessionManagement sessionManagement = new SessionManagement(getActivity());
        sessionManagement.removeSession();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new Login())
                .commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        Context context = getActivity();
        SharedPreferences pref = context.getSharedPreferences("session", Context.MODE_PRIVATE);
        int username = pref.getInt("session_user", 0);
        Log.d("Home", "onStart: " + username);

    }

    @Override
    public void onResume() {
        super.onResume();
        SessionManagement sessionManagement = new SessionManagement(getActivity());
        sessionManagement.checkSessionTimeout();
    }


}