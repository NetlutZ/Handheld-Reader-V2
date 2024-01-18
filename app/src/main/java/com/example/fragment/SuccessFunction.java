package com.example.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.handheld_reader.R;

public class SuccessFunction extends Fragment {
    private Button cancelButton, confirmButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_success_function, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View test = getView().findViewById(R.id.bottomBar);
        cancelButton = (Button) test.findViewById(R.id.btnCancel);
        cancelButton.setVisibility(View.GONE);
        confirmButton = (Button) test.findViewById(R.id.btnConfirm);
        confirmButton.setOnClickListener(new ToHome());
    }

    private class ToHome implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Home()).commit();
        }
    }
}