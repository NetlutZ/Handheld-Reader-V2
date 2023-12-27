package com.example.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.handheld_reader.R;
import com.example.model.Device;
import com.google.gson.Gson;
import com.rscja.deviceapi.RFIDWithUHFUART;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class DeviceList extends Fragment {
    Activity activity = getActivity();
    RadioGroup radioGroup;
    SearchView searchView;
    CustomAdapter customAdapter;
    ListView listView;
    Device[] devices;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_device_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        radioGroup = getActivity().findViewById(R.id.device_option);
        radioGroup.setOnCheckedChangeListener(new DeviceOption());

        listView = (ListView) getActivity().findViewById(R.id.deviceList);
        customAdapter = new CustomAdapter(getActivity().getApplicationContext());
        listView.setAdapter(customAdapter);

        searchView = getActivity().findViewById(R.id.searchView);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (TextUtils.isEmpty(s)) {
                    customAdapter.filter("");
                    listView.clearTextFilter();
                }else{

                    customAdapter.filter(s);
                }
                return false;
            }
        });

        Test1 testTask = new Test1();
        testTask.execute();
    }

    public class DeviceOption implements RadioGroup.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if(checkedId == R.id.name_selected){
                Toast.makeText(getActivity(),"name option",Toast.LENGTH_SHORT).show();
            }
            else if(checkedId == R.id.tag_selected){
                Toast.makeText(getActivity(),"tag option",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class Test1 extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                OkHttpClient client = new OkHttpClient();
                String url = "http://10.0.2.2:8080/device/";

                Request request = new Request.Builder()
                        .url(url)
                        .build();

                Response response = client.newCall(request).execute();
                Log.d("Response Success : ",response.peekBody(2048).string());
                // cant use response.body().string() twice so use peekBody() instead : https://stackoverflow.com/questions/60671465/retrofit-java-lang-illegalstateexception-closed
                return response.body().string();
            }catch (Exception e){
                Log.d("Response Error : ",e.toString());
                return null;
            }

        }
        @Override
        protected  void onPostExecute(String s){
            super.onPostExecute(s);
            List<Device> contentList = new ArrayList<>();
            Gson gson = new Gson();
            devices = gson.fromJson(s, Device[].class);
            for (Device device : devices) {
                contentList.add(device);
            }
            customAdapter.setData(contentList);
        }
    }

    public class CustomAdapter extends BaseAdapter {
        public Context mContext;
        public LayoutInflater mInflater;
        List<Device> mData = new ArrayList<>();
        ArrayList<Device> tmpData = new ArrayList<>();
        public  CustomAdapter(Context context){
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
        }
        public void setData(List<Device> data){
            mData = data;
            tmpData.addAll(mData);
            notifyDataSetChanged();
        }
        public void addData(Device data){
            mData.add(data);
            tmpData.add(data);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Device getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if(convertView == null){
                convertView = mInflater.inflate(R.layout.listtag_items_design,null);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.DeviceName);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }

//            holder.name.setText(name[position]);
            Device device = getItem(position);
            holder.name.setText(device.getName());
            return convertView;
        }

        public void filter(String charText){
            charText = charText.toLowerCase();
            mData.clear();
            if(charText.length()==0){
                mData.addAll(tmpData);
            }
            else{
                for(Device device : tmpData){
                    if(device.getName().toLowerCase().contains(charText)){
                        mData.add(device);
                    }
                }
            }
            notifyDataSetChanged();
        }
    }

    public class ViewHolder{
        TextView name;
        TextView tag;
        ImageView img;
    }
}