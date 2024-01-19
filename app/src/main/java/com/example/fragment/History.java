package com.example.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.handheld_reader.BuildConfig;
import com.example.handheld_reader.R;
import com.example.model.Device;
import com.example.model.DeviceGroupName;
import com.example.model.HistoryItem;
import com.example.session.SessionManagement;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class History extends Fragment {
    CustomListAdapter customListAdapter;
    ListView listView;
    String URL = BuildConfig.BASE_URL;
    private Activity activity = getActivity();
    private String SHARED_PREF_NAME = "session";
    private String SESSION_KEY = "session_user_id";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView = (ListView) getActivity().findViewById(R.id.history_item);
        customListAdapter = new CustomListAdapter(getActivity().getApplicationContext());
        listView.setAdapter(customListAdapter);
        listView.setOnItemClickListener(((parent, view, position, id) -> {
            OpenDialog(position);
//            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, historyDialog).commit();

        }));

        new GetHistoryData().execute();

    }

    public void OpenDialog(int position) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("historyItem", customListAdapter.getItem(position));

        HistoryDialog historyDialog = new HistoryDialog();
        historyDialog.setArguments(bundle);
        historyDialog.show(getActivity().getSupportFragmentManager(), "History Dialog");
    }


    public class GetHistoryData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            SessionManagement sessionManagement = new SessionManagement(getActivity());
            int userId = sessionManagement.getSession();

            // Get Activity of User
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(URL + "/activity/user/" + userId)
                    .get()
                    .build();
            List<HistoryItem> historyItemList = new ArrayList<>();
            try (Response response = client.newCall(request).execute()) {
                String result = null;
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        result = response.body().string();

                        JSONArray jsonArray = new JSONArray(result.toString());
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            if(jsonObject.isNull("activityDate") || jsonObject.isNull("activityTime")){
                                continue;
                            }
                            String activityCode = jsonObject.getString("activityCode");
                            String activityDate = jsonObject.getString("activityDate");
                            String activityTime = jsonObject.getString("activityTime");
//                            int userId = jsonObject.isNull("userId") ? 0 : jsonObject.getInt("userId");
                            String device = jsonObject.getString("device");

                            // Change date format
                            Instant instant = Instant.parse(activityDate);
                            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                            activityDate = localDateTime.format(formatter);


                            // Split device string into array
                            String[] deviceArray = device.split(",");
                            HashMap<String, Integer> deviceDetail = new HashMap<>();    // <DeviceName, Quantity of each activity>

                            for (String deviceId : deviceArray) {
                                OkHttpClient client2 = new OkHttpClient();
                                Request request2 = new Request.Builder()
                                        .url(URL + "/device/" + deviceId)
                                        .get()
                                        .build();
                                try (Response response2 = client2.newCall(request2).execute()) {
                                    // Get device of each activity
                                    String result2 = null;
                                    if (response2.isSuccessful()) {
                                        if (response2.body() != null) {
                                            result2 = response2.body().string();
                                            JSONObject jsonObject2 = new JSONObject(result2);
                                            String deviceName = jsonObject2.getString("name");
                                            if (deviceDetail.containsKey(deviceName)) {
                                                deviceDetail.put(deviceName, deviceDetail.get(deviceName) + 1);
                                            } else {
                                                deviceDetail.put(deviceName, 1);
                                            }
                                        }
                                    } else {

                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            HistoryItem historyItem = new HistoryItem(activityCode, activityDate, activityTime, userId, device, deviceDetail);
                            historyItemList.add(historyItem);
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                customListAdapter.setData(historyItemList);
                            }
                        });
                    }
                } else {

                }
                return null;
            } catch (Exception e) {
//                Toast.makeText(getActivity(), R.string.server_error, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }


    public class CustomListAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private Context mContext;
        private HistoryItem[] historyItem;
        List<HistoryItem> historyItemList = new ArrayList<>();

        public void setData(List<HistoryItem> data) {
            historyItemList = data;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return historyItemList.size();
        }

        @Override
        public HistoryItem getItem(int position) {
            return historyItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public CustomListAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder1 holder1 = null;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.history_item, null);
                holder1 = new ViewHolder1();
                holder1.date = (TextView) convertView.findViewById(R.id.history_date);
                holder1.time = (TextView) convertView.findViewById(R.id.history_time);
                holder1.status = (TextView) convertView.findViewById(R.id.history_status);
                holder1.detail = (ListView) convertView.findViewById(R.id.history_item_detail);
                convertView.setTag(holder1);
            } else {
                holder1 = (ViewHolder1) convertView.getTag();
            }

            HistoryItem currentItem = getItem(position);
            holder1.date.setText(currentItem.getActivityDate().toString());
            holder1.time.setText(currentItem.getActivityTime().toString());
            holder1.status.setText(currentItem.getActivityCode());

//            holder1.detail.setDivider(null);
            holder1.detail.setDividerHeight(0);
            holder1.detail.setNestedScrollingEnabled(true);
            CustomListAdapter2 customListAdapter2;
            customListAdapter2 = new CustomListAdapter2(getActivity().getApplicationContext(), currentItem.getDeviceDetail());
            holder1.detail.setAdapter(customListAdapter2);
            return convertView;
        }

    }

    public class ViewHolder1 {
        TextView date;
        TextView time;
        TextView status;
        ListView detail;
    }

    public class CustomListAdapter2 extends BaseAdapter {
        private LayoutInflater mInflater;
        private Context mContext;
        private HashMap<String, Integer> deviceDetail;

        @Override
        public int getCount() {
            return deviceDetail.size();
        }

        @Override
        public HashMap<String, Integer> getItem(int position) {
            return deviceDetail;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public CustomListAdapter2(Context context, HashMap<String, Integer> deviceDetail) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
            this.deviceDetail = deviceDetail;

        }

        // Disable click on listview detail
        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder2 holder2 = null;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.history_item_detail, null);
                holder2 = new ViewHolder2();
                holder2.name = (TextView) convertView.findViewById(R.id.device_name);
                holder2.quantity = (TextView) convertView.findViewById(R.id.device_quantity);
                convertView.setTag(holder2);
            } else {
                holder2 = (ViewHolder2) convertView.getTag();
            }

            ArrayList<String> keyList = new ArrayList<String>(deviceDetail.keySet());
            ArrayList<Integer> valueList = new ArrayList<Integer>(deviceDetail.values());
            holder2.name.setText(keyList.get(position));
            holder2.quantity.setText(valueList.get(position).toString());

            return convertView;
        }

    }

    public class ViewHolder2 {
        TextView name;
        TextView quantity;
    }
}