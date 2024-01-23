package com.example.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
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
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

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
    private ProgressDialog dialog;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }

        if (getActivity() != null && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("History");
        }
        DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        listView = (ListView) getActivity().findViewById(R.id.history_item);
        customListAdapter = new CustomListAdapter(getActivity().getApplicationContext());
        listView.setAdapter(customListAdapter);
        listView.setOnItemClickListener(((parent, view, position, id) -> {
            OpenDialog(position);
//            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, historyDialog).commit();

        }));

        dialog = new ProgressDialog(getActivity());
        dialog.show();
        dialog.setContentView(R.layout.progress_dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false); // Prevent dialog from being dismissed
        TextView dialogText = dialog.findViewById(R.id.dialog_text);
        dialogText.setText("Loading Data...");
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

                        // Sort History by date and time from newest to oldest
                        for (int i = 0; i < historyItemList.size(); i++) {
                            for (int j = i + 1; j < historyItemList.size(); j++) {
                                SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd-MM-yyyy");
                                SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                String rtDate = "";
                                if (!Objects.equals(historyItemList.get(i).getActivityDate(), "") && !Objects.equals(historyItemList.get(j).getActivityDate(), "")) {

                                    try {
                                        rtDate = outputDateFormat.format(inputDateFormat.parse(historyItemList.get(i).getActivityDate()));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    LocalDate date = LocalDate.parse(rtDate);
                                    String rtDate2 = "";
                                    try {
                                        rtDate2 = outputDateFormat.format(inputDateFormat.parse(historyItemList.get(j).getActivityDate()));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    LocalDate date2 = LocalDate.parse(rtDate2);
                                    if (date.isBefore(date2)) {
                                        HistoryItem temp = historyItemList.get(i);
                                        historyItemList.set(i, historyItemList.get(j));
                                        historyItemList.set(j, temp);
                                    } else if (date.isEqual(date2)) {
                                        String time1 = historyItemList.get(i).getActivityTime();
                                        String time2 = historyItemList.get(j).getActivityTime();
                                        LocalTime localTime1 = LocalTime.parse(time1);
                                        LocalTime localTime2 = LocalTime.parse(time2);
                                        if (localTime1.isBefore(localTime2)) {
                                            HistoryItem temp = historyItemList.get(i);
                                            historyItemList.set(i, historyItemList.get(j));
                                            historyItemList.set(j, temp);
                                        }
                                    }
                                }
                            }
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
            dialog.dismiss();
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
                holder1.statusCode = (TextView) convertView.findViewById(R.id.history_status_code);
                holder1.status = (TextView) convertView.findViewById(R.id.history_status);
                holder1.detail = (ListView) convertView.findViewById(R.id.history_item_detail);
                convertView.setTag(holder1);
            } else {
                holder1 = (ViewHolder1) convertView.getTag();
            }

            HistoryItem currentItem = getItem(position);
            holder1.date.setText(currentItem.getActivityDate().toString());
            holder1.time.setText(currentItem.getActivityTime().toString());
            holder1.statusCode.setText("[" + currentItem.getActivityCode() + "]");
            if (currentItem.getActivityCode().charAt(0) == 'B') {
                holder1.status.setText("Borrow ");
                holder1.status.setTextColor(getResources().getColor(R.color.orange1));
                holder1.statusCode.setTextColor(getResources().getColor(R.color.orange1));
            } else if(currentItem.getActivityCode().charAt(0) == 'R'){
                holder1.status.setText("Return ");
                holder1.status.setTextColor(getResources().getColor(R.color.green1));
                holder1.statusCode.setTextColor(getResources().getColor(R.color.green1));
            }

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
        TextView statusCode, status;
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