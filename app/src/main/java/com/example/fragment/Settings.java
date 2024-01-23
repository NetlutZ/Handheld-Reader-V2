package com.example.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.handheld_reader.MainActivity;
import com.example.handheld_reader.R;
import com.google.android.material.navigation.NavigationView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.exception.ConfigurationException;

public class Settings extends Fragment implements View.OnClickListener {
    private RFIDWithUHFUART mContext;

    private Spinner spPower;
    private ArrayAdapter adapter; //频点列表适配器

    private DisplayMetrics metrics;
    private AlertDialog dialog;
    private long[] timeArr;

    private Handler mHandler = new Handler();
    private int arrPow; //输出功率

    String TAG = "UHFSetFragment";
    Button btnGetPower, btnSetPower;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }

        if (getActivity() != null && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Settings");
        }
        DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_settings);

        btnGetPower = getView().findViewById(R.id.btnGetPower1);
        btnSetPower = getView().findViewById(R.id.btnSetPower1);
        spPower = getView().findViewById(R.id.spPower);

        try {
            mContext = RFIDWithUHFUART.getInstance();
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
        if (mContext != null) {
            new Settings.InitTask().execute();
        }

        btnGetPower.setOnClickListener(this);
        btnSetPower.setOnClickListener(this);

        arrPow = R.array.arrayPower;
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(), arrPow, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPower.setAdapter(adapter);
    }

    private class InitTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                return mContext.init();
            } catch (Exception ex) {
                Log.e("InitTask", ex.toString());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (mypDialog != null && mypDialog.isShowing()) {
                mypDialog.dismiss();
            }

            if (!result) {
                Toast.makeText(getActivity(), "Init failed", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            try {
                super.onPreExecute();

                mypDialog = new ProgressDialog(getActivity());
                mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mypDialog.setMessage("init...");
                mypDialog.setCanceledOnTouchOutside(false);
                mypDialog.show();

            } catch (Exception ex) {
                Log.e("InitTask", ex.toString());
                return;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    OnClick_GetPower(null);
                }
            });
        }
    }

    public void OnClick_GetPower(View view) {
        int iPower = mContext.getPower();

        Log.i("UHFSetFragment", "OnClick_GetPower() iPower=" + iPower);

        if (iPower > -1) {
            int position = iPower - 1;
            int count = spPower.getCount();
            spPower.setSelection(position > count - 1 ? count - 1 : position);
            Toast.makeText(getActivity(), "Get Power Success", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "Get Power Fail", Toast.LENGTH_SHORT).show();
        }

    }

    public void OnClick_SetPower(View view) {
        int iPower = spPower.getSelectedItemPosition() + 1;

        Log.i("UHFSetFragment", "OnClick_SetPower() iPower=" + iPower);

        if (mContext.setPower(iPower)) {
            Toast.makeText(getActivity(), "Set Power Success", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "Set Power Fail", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnGetPower1) {
            OnClick_GetPower(null);
        } else if (v.getId() == R.id.btnSetPower1) {
            OnClick_SetPower(null);
        }
    }
}