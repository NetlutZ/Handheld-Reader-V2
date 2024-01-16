package com.example.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.handheld_reader.BuildConfig;
import com.example.handheld_reader.MainActivity;
import com.example.handheld_reader.R;
import com.example.model.Device;
import com.example.model.DeviceGroupName;
import com.example.view.UhfLocationCanvasView;
import com.google.gson.Gson;
import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.interfaces.IUHF;
import com.rscja.deviceapi.interfaces.IUHFLocationCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RFIDLocation extends Fragment {
    String TAG = "UHF_LocationFragment";
    private RFIDWithUHFUART mReader;
    private UhfLocationCanvasView llChart;
    private EditText etEPC;
    private Button btStart, btStop;
    private PlaySoundThread playSoundThread;
    private View includeView;
    TextView deviceName, deviceTag;
    Activity activity = getActivity();
    Device[] devices;
    private String name;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rfid_location, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        playSoundThread = new PlaySoundThread();
        playSoundThread.start();

        llChart = getView().findViewById(R.id.llChart);
        etEPC = getView().findViewById(R.id.etEPC);
        btStart = getView().findViewById(R.id.btStart);
        btStop = getView().findViewById(R.id.btStop);
        includeView = getView().findViewById(R.id.deviceLocation);
        includeView.findViewById(R.id.btnDelete).setVisibility(View.GONE);
        includeView.findViewById(R.id.QuantityDevice).setVisibility(View.GONE);

        deviceName = includeView.findViewById(R.id.DeviceName);
        deviceTag = includeView.findViewById(R.id.TvTagUii);

        Bundle bundle = getArguments();
        if (bundle != null) {
            DeviceGroupName deviceGroupName = (DeviceGroupName) bundle.getSerializable("deviceGroupName");
            Device device = (Device) bundle.getSerializable("device");
            if (deviceGroupName != null) {
                name = deviceGroupName.getName();
                GetDevice getDevice = new GetDevice();
                getDevice.execute();
            } else if (device != null) {
                deviceName.setText(device.getName());
                deviceTag.setText(device.getRfid());
            }
        }

        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocation();
            }
        });
        btStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopLocation();
            }
        });

        try {
            mReader = RFIDWithUHFUART.getInstance();
            mReader.init();
        } catch (Exception ex) {
            Log.e("initUHF Error", ex.toString());
            return;
        }
    }

    public class GetDevice extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                OkHttpClient client = new OkHttpClient();
                String url = BuildConfig.BASE_URL + "/device" + "?name=" + name;

                Request request = new Request.Builder()
                        .url(url)
                        .build();

                Response response = client.newCall(request).execute();
                Log.d("Response Success : ", response.peekBody(2048).string());
                // cant use response.body().string() twice so use peekBody() instead : https://stackoverflow.com/questions/60671465/retrofit-java-lang-illegalstateexception-closed
                return response.body().string();
            } catch (Exception e) {
                Log.d("Response Error : ", e.toString());
                return null;
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Gson gson = new Gson();
            devices = gson.fromJson(s, Device[].class);
            if (devices.length > 0) {
                //random in devices and rfidStatus = InStorage
                for (int i = 0; i < devices.length; i++) {
                    if (devices[i].getRfidStatus() != null) {
                        if (devices[i].getRfidStatus().equals("InStorage")) {
                            deviceName.setText(devices[i].getName());
                            deviceTag.setText(devices[i].getRfid());
                            break;
                        }
                    }
                }

            }
        }
    }

    private void startLocation() {
        String epc = etEPC.getText().toString();
        epc = deviceTag.getText().toString();
        if (epc.equals("")) {
            Toast.makeText(activity, R.string.location_fail, Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("EPC", epc.toString());
        boolean result = mReader.startLocation(getActivity(), epc, IUHF.Bank_EPC, 32, new IUHFLocationCallback() {
            @Override
            public void getLocationValue(int i, boolean b) {
                llChart.setData(i);
                Log.i(TAG, "value:" + i);
                if (i <= 10) {
                    playSoundThread.play(Integer.MAX_VALUE);
                } else if (i <= 30) {
                    playSoundThread.play(1600);
                } else if (i <= 50) {
                    playSoundThread.play(1100);
                } else if (i <= 70) {
                    playSoundThread.play(600);
                } else if (i <= 90) {
                    playSoundThread.play(100);
                }
            }

        });
        if (!result) {
            Toast.makeText(activity, R.string.psam_msg_fail, Toast.LENGTH_SHORT).show();
            Log.d("Location", "FAIL");
            return;
        }
        btStart.setEnabled(false);
        etEPC.setEnabled(false);
    }

    public void stopLocation() {
        try {
            mReader.stopLocation();
            btStart.setEnabled(true);
            etEPC.setEnabled(true);
        } catch (Exception e) {
            Log.e("Stop Location Error", e.toString());
        }
    }

    private Object objectLock = new Object();

    private class PlaySoundThread extends Thread {
        private boolean isStop = false;
        int waitTime = Integer.MAX_VALUE;

        @Override
        public void run() {
            while (!isStop) {
                synchronized (objectLock) {
                    try {
                        objectLock.wait(waitTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //播放声音
//                mContext.playSound(1);
            }
        }

        public void play(int waitTime) {
            this.waitTime = waitTime;
            synchronized (objectLock) {
                objectLock.notifyAll();
            }
        }

        public void stopPlay() {
            isStop = true;
            synchronized (objectLock) {
                objectLock.notifyAll();
            }
            interrupt();
        }
    }
}