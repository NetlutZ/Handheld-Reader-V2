package com.example.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.handheld_reader.BuildConfig;
import com.example.handheld_reader.R;
import com.example.model.Device;
import com.example.model.DeviceGroupName;
import com.example.model.HistoryItem;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HistoryDialog extends AppCompatDialogFragment {
    private String date;
    private String time;
    private String status;
    TextView dateView, timeView, statusView;
    ListView listView;
    List<Device> deviceList = new ArrayList<>();
    String URL = BuildConfig.BASE_URL;
    HistoryItem historyItem;
    CustomAdapter customAdapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.history_dialog, null);
        builder.setView(view)
        .setNegativeButton("close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dateView = view.findViewById(R.id.history_dialog_date);
        timeView = view.findViewById(R.id.history_dialog_time);
        statusView = view.findViewById(R.id.history_dialog_status);
        listView = view.findViewById(R.id.history_dialog_list);
        customAdapter = new CustomAdapter(view.getContext());
        listView.setAdapter(customAdapter);

        Bundle bundle = getArguments();
        if (bundle != null) {
            historyItem = (HistoryItem) bundle.getSerializable("historyItem");
            if (historyItem != null) {
                dateView.setText(historyItem.getActivityDate());
                timeView.setText(historyItem.getActivityTime());
                statusView.setText(historyItem.getActivityCode());
                Log.d("HistoryDialog", "historyItem: " + historyItem.getActivityDate());
            }
        }
        new GetDeviceData().execute();

        return builder.create();
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.history_dialog, container, false);
//    }

//    @Override
//    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//
//        dateView = getActivity().findViewById(R.id.history_dialog_date);
//        timeView = getActivity().findViewById(R.id.history_dialog_time);
//        statusView = getActivity().findViewById(R.id.history_dialog_status);
//        listView = getActivity().findViewById(R.id.history_dialog_list);
//        customAdapter = new CustomAdapter(getActivity().getApplicationContext());
//        listView.setAdapter(customAdapter);
//
//
//        Bundle bundle = getArguments();
//        if (bundle != null) {
//            historyItem = (HistoryItem) bundle.getSerializable("historyItem");
//            if (historyItem != null) {
//                Log.d("HistoryDialog", "historyItem: " + historyItem.getActivityDate());
//                dateView.setText(historyItem.getActivityDate());
//                timeView.setText(historyItem.getActivityTime());
//                statusView.setText(historyItem.getActivityCode());
//            }
//        }
//        new GetDeviceData().execute();
//    }

    private class GetDeviceData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            String[] deviceId = historyItem.getDevice().split(",");
            for (String s : deviceId) {
                try {
                    URL url = new URL(URL + "/device/" + s);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    InputStream stream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuffer result = new StringBuffer();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    try {
                        JSONObject jsonObject = new JSONObject(result.toString());
                        Device device = new Device();
                        device.setId(jsonObject.getInt("id"));
                        device.setName(jsonObject.getString("name"));
                        device.setImage(jsonObject.getString("image"));

                        deviceList.add(device);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    customAdapter.setDevices(deviceList);
                }
            });


            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    public class CustomAdapter extends BaseAdapter {
        private List<Device> deviceList = new ArrayList<>();
        private Context context;
        LayoutInflater inflater;

        public CustomAdapter(Context context) {
            this.context = context;
            inflater = LayoutInflater.from(context);
        }

        public void setDevices(List<Device> deviceList) {
            this.deviceList = deviceList;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return deviceList.size();
        }

        @Override
        public Object getItem(int position) {
            return deviceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private class ViewHolder {
            TextView deviceNameView;
            TextView deviceIdView;
            ImageView deviceImage;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            LayoutInflater inflater = LayoutInflater.from(context);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.history_dialog_detail, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.deviceNameView = convertView.findViewById(R.id.dialog_device_name);
                viewHolder.deviceIdView = convertView.findViewById(R.id.dialog_device_id);
                viewHolder.deviceImage = convertView.findViewById(R.id.dialog_device_image);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Device device = deviceList.get(position);
            viewHolder.deviceNameView.setText(device.getName());
            viewHolder.deviceIdView.setText(String.valueOf(device.getId()));
            Picasso.get().load(URL + "/device" + "/image/" + device.getImg()).into(viewHolder.deviceImage);
            return convertView;
        }
    }
}