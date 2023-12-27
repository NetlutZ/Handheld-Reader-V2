package com.example.fragment;

import android.content.Context;
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

import com.example.handheld_reader.R;
import com.example.model.Device;
import com.example.model.HistoryItem;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class History extends Fragment {
    HistoryItem[] historyItem;
    CustomListAdapter customListAdapter;
    ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Sample data for HistoryItem objects
        HashMap<String, Integer> deviceDetail1 = new HashMap<>();
        deviceDetail1.put("Device1", 5);
        deviceDetail1.put("Device2", 3);
        deviceDetail1.put("Device3", 3);
        LocalDate date1 = LocalDate.of(2023, 12, 15);
        LocalTime time1 = LocalTime.of(10, 30);
        String status1 = "Active";

        HashMap<String, Integer> deviceDetail2 = new HashMap<>();
        deviceDetail2.put("Device3", 7);
        deviceDetail2.put("Device4", 1);
        LocalDate date2 = LocalDate.of(2023, 12, 20);
        LocalTime time2 = LocalTime.of(15, 45);
        String status2 = "Inactive";

        HashMap<String, Integer> deviceDetail3 = new HashMap<>();
        deviceDetail3.put("Device5", 4);
        deviceDetail3.put("Device6", 2);
        deviceDetail3.put("Device7", 5);
        LocalDate date3 = LocalDate.of(2023, 12, 25);
        LocalTime time3 = LocalTime.of(11, 0);
        String status3 = "Active";

        HashMap<String, Integer> deviceDetail4 = new HashMap<>();
        deviceDetail4.put("Device8", 6);
        deviceDetail4.put("Device9", 3);
        LocalDate date4 = LocalDate.of(2023, 12, 30);
        LocalTime time4 = LocalTime.of(9, 15);
        String status4 = "Inactive";

        HashMap<String, Integer> deviceDetail10 = new HashMap<>();
        deviceDetail10.put("Device20", 8);
        deviceDetail10.put("Device21", 5);
        deviceDetail10.put("Device22", 2);
        LocalDate date10 = LocalDate.of(2024, 1, 5);
        LocalTime time10 = LocalTime.of(14, 45);
        String status10 = "Active";

        HashMap<String, Integer> deviceDetail5 = new HashMap<>();
        deviceDetail5.put("Device10", 3);
        deviceDetail5.put("Device11", 6);
        deviceDetail5.put("Device12", 4);
        LocalDate date5 = LocalDate.of(2023, 12, 28);
        LocalTime time5 = LocalTime.of(12, 0);
        String status5 = "Inactive";

        HashMap<String, Integer> deviceDetail6 = new HashMap<>();
        deviceDetail6.put("Device13", 7);
        deviceDetail6.put("Device14", 2);
        LocalDate date6 = LocalDate.of(2023, 12, 31);
        LocalTime time6 = LocalTime.of(16, 20);
        String status6 = "Active";

        HashMap<String, Integer> deviceDetail7 = new HashMap<>();
        deviceDetail7.put("Device15", 5);
        deviceDetail7.put("Device16", 3);
        deviceDetail7.put("Device17", 1);
        LocalDate date7 = LocalDate.of(2024, 1, 3);
        LocalTime time7 = LocalTime.of(9, 45);
        String status7 = "Active";

        HashMap<String, Integer> deviceDetail8 = new HashMap<>();
        deviceDetail8.put("Device18", 4);
        deviceDetail8.put("Device19", 2);
        LocalDate date8 = LocalDate.of(2024, 1, 8);
        LocalTime time8 = LocalTime.of(14, 15);
        String status8 = "Inactive";

        HashMap<String, Integer> deviceDetail9 = new HashMap<>();
        deviceDetail9.put("Device23", 6);
        deviceDetail9.put("Device24", 1);
        LocalDate date9 = LocalDate.of(2024, 1, 12);
        LocalTime time9 = LocalTime.of(10, 30);
        String status9 = "Active";

        // Initialize HistoryItem array with sample data
        historyItem = new HistoryItem[]{
                new HistoryItem(deviceDetail1, date1, time1, status1),
                new HistoryItem(deviceDetail2, date2, time2, status2),
                new HistoryItem(deviceDetail3, date3, time3, status3),
                new HistoryItem(deviceDetail4, date4, time4, status4),
                new HistoryItem(deviceDetail5, date5, time5, status5),
                new HistoryItem(deviceDetail6, date6, time6, status6),
                new HistoryItem(deviceDetail7, date7, time7, status7),
                new HistoryItem(deviceDetail8, date8, time8, status8),
                new HistoryItem(deviceDetail9, date9, time9, status9),
                new HistoryItem(deviceDetail10, date10, time10, status10)
        };

        // Use the historyItems array as needed in your code
        // For example, you can loop through historyItems to access individual HistoryItem objects
        for (HistoryItem item : historyItem) {
            Log.d("HistoryItem","Device Detail: " + item.getDeviceDetail());
            Log.d("HistoryItem","Date: " + item.getDate());
            Log.d("HistoryItem","Time: " + item.getTime());
            Log.d("HistoryItem","Status: " + item.getStatus());
            Log.d("HistoryItem","------------------------");
        }

        listView = (ListView) getActivity().findViewById(R.id.history_item);
        customListAdapter = new CustomListAdapter(getActivity().getApplicationContext());
        listView.setAdapter(customListAdapter);

    }

    public class CustomListAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private Context mContext;
        @Override
        public int getCount() {
            return historyItem.length;
        }

        @Override
        public HistoryItem getItem(int position) {
            return historyItem[position];
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

            if(convertView == null){
                convertView = mInflater.inflate(R.layout.history_item,null);
                holder1 = new ViewHolder1();
                holder1.date = (TextView) convertView.findViewById(R.id.history_date);
                holder1.time = (TextView) convertView.findViewById(R.id.history_time);
                holder1.status = (TextView) convertView.findViewById(R.id.history_status);
                holder1.detail = (ListView) convertView.findViewById(R.id.history_item_detail);
                convertView.setTag(holder1);
            }else{
                holder1 = (ViewHolder1) convertView.getTag();
            }

            HistoryItem currentItem = getItem(position);
            holder1.date.setText(currentItem.getDate().toString());
            holder1.time.setText(currentItem.getTime().toString());
            holder1.status.setText(currentItem.getStatus());

//            holder1.detail.setDivider(null);
            holder1.detail.setDividerHeight(0);
            holder1.detail.setNestedScrollingEnabled(true);
            CustomListAdapter2 customListAdapter2;
            customListAdapter2 = new CustomListAdapter2(getActivity().getApplicationContext(),currentItem.getDeviceDetail());
            holder1.detail.setAdapter(customListAdapter2);
            return convertView;
        }

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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder2 holder2 = null;

            if(convertView == null){
                convertView = mInflater.inflate(R.layout.history_item_detail,null);
                holder2 = new ViewHolder2();
                holder2.name = (TextView) convertView.findViewById(R.id.device_name);
                holder2.quantity = (TextView) convertView.findViewById(R.id.device_quantity);
                convertView.setTag(holder2);
            }else{
                holder2 = (ViewHolder2) convertView.getTag();
            }

//            HistoryItem currentItem = getItem(position);
//            holder2.name.setText(currentItem.getDate().toString());
//            holder2.quantity.setText(currentItem.getTime().toString());
            ArrayList<String> keyList = new ArrayList<String>(deviceDetail.keySet());
            ArrayList<Integer> valueList = new ArrayList<Integer>(deviceDetail.values());
            holder2.name.setText(keyList.get(position));
            holder2.quantity.setText(valueList.get(position).toString());


            return convertView;
        }

    }

    public class ViewHolder1{
        TextView date;
        TextView time;
        TextView status;
        ListView detail;
    }
    public class ViewHolder2{
        TextView name;
        TextView quantity;

    }
}