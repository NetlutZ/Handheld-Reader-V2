package com.example.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.handheld_reader.BuildConfig;
import com.example.handheld_reader.R;
import com.example.model.Device;
import com.example.model.DeviceGroupName;
import com.google.gson.Gson;
import com.rscja.deviceapi.RFIDWithUHFUART;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

        // Click Device and go to FindDeviceLocation
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (radioGroup.getCheckedRadioButtonId() == R.id.name_selected) {
                if (customAdapter.getGroupItem(position).getQuantity() == 0) {
                    Toast.makeText(getActivity(), "No device in storage", Toast.LENGTH_SHORT).show();
                    return;
                }

                DeviceGroupName deviceGroupName = customAdapter.getGroupItem(position);
                Bundle bundle = new Bundle();
                bundle.putSerializable("deviceGroupName", deviceGroupName);

                // Create a new instance of the RFIDLocation fragment
                RFIDLocation fragment = new RFIDLocation();
                fragment.setArguments(bundle);

                // Move to RFIDLocation fragment with data
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();

            } else if (radioGroup.getCheckedRadioButtonId() == R.id.tag_selected) {
                if (!Objects.equals(customAdapter.getItem(position).getRfidStatus(), "InStorage")) {
                    Toast.makeText(getActivity(), "No device in storage", Toast.LENGTH_SHORT).show();
                    return;
                }

                Device device = customAdapter.getItem(position);
                Bundle bundle = new Bundle();
                bundle.putSerializable("device", device);

                // Create a new instance of the RFIDLocation fragment
                RFIDLocation fragment = new RFIDLocation();
                fragment.setArguments(bundle);

                // Move to RFIDLocation fragment with data
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
        });

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
                } else {
                    customAdapter.filter(s);
                }
                return false;
            }
        });

        GetDevice getDevice = new GetDevice();
        getDevice.execute();
    }

    private void GroupDevice() {
        List<DeviceGroupName> contentList = new ArrayList<>();
        for (Device device : devices) {
            // Group device by name and set quantity
            boolean isExist = false;
            if (device.getRfidStatus() != null && device.getName() != null) {
                for (DeviceGroupName deviceGroupName : contentList) {
                    if (device.getName().equals(deviceGroupName.getName())) {
                        if (device.getRfidStatus().equals("InStorage")) {
                            deviceGroupName.setQuantity(deviceGroupName.getQuantity() + 1);
                        }
                        isExist = true;
                    }
                }
                if (!isExist) {
                    if (device.getRfidStatus().equals("InStorage")) {
                        contentList.add(new DeviceGroupName(device.getId(), device.getName(), 1, device.getMaxBorrowDays(), device.getImg()));
                    } else {
                        contentList.add(new DeviceGroupName(device.getId(), device.getName(), 0, device.getMaxBorrowDays(), device.getImg()));
                    }
                }
            }
        }
        customAdapter.setGroupData(contentList);
    }

    public class DeviceOption implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            searchView.setQuery("", false);
            searchView.clearFocus();

            if (checkedId == R.id.name_selected) {
                Toast.makeText(getActivity(), "name option", Toast.LENGTH_SHORT).show();
                GroupDevice();
            } else if (checkedId == R.id.tag_selected) {
                List<Device> contentList = new ArrayList<>();
                for (Device device : devices) {
                    contentList.add(device);
                }
                customAdapter.setData(contentList);
                Toast.makeText(getActivity(), "tag option", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class GetDevice extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                OkHttpClient client = new OkHttpClient();
                String url = BuildConfig.BASE_URL + "/device";

                Request request = new Request.Builder()
                        .url(url)
                        .build();

                Response response = client.newCall(request).execute();
                Log.d("Response Success : ", response.peekBody(2048).string());
                // cant use response.body().string() twice so use peekBody() instead : https://stackoverflow.com/questions/60671465/retrofit-java-lang-illegalstateexception-closed
                return response.body().string();
            } catch (Exception e) {
                Toast.makeText(getActivity(), R.string.server_error, Toast.LENGTH_SHORT).show();
                return null;
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s == null) {
                Toast.makeText(getActivity(), "No device found / Server Error", Toast.LENGTH_SHORT).show();
            } else {
                Gson gson = new Gson();
                devices = gson.fromJson(s, Device[].class);
                GroupDevice();
            }
        }
    }

    public class CustomAdapter extends BaseAdapter {
        public Context mContext;
        public LayoutInflater mInflater;
        List<Device> mData = new ArrayList<>();
        ArrayList<Device> tmpData = new ArrayList<>();
        List<DeviceGroupName> mGroupData = new ArrayList<>();
        ArrayList<DeviceGroupName> tmpGroupData = new ArrayList<>();

        public CustomAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
        }

        public void setData(List<Device> data) {
            mGroupData.clear();
            tmpGroupData.clear();
            mData = data;
            tmpData.addAll(mData);
            notifyDataSetChanged();
        }

        public void setGroupData(List<DeviceGroupName> data) {
            mData.clear();
            tmpData.clear();
            mGroupData = data;
            tmpGroupData.addAll(mGroupData);
            notifyDataSetChanged();
        }

        public void addData(Device data) {
            mData.add(data);
            tmpData.add(data);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if (radioGroup.getCheckedRadioButtonId() == R.id.name_selected) {
                return mGroupData.size();
            } else if (radioGroup.getCheckedRadioButtonId() == R.id.tag_selected) {
                return mData.size();
            }
            return 0;
        }

        @Override
        public Device getItem(int position) {
            return mData.get(position);
        }

        public DeviceGroupName getGroupItem(int position) {
            return mGroupData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.listtag_items_design, null);
                convertView.findViewById(R.id.btnDelete).setVisibility(View.GONE);
                holder = new ViewHolder();
                holder.name = convertView.findViewById(R.id.DeviceName);
                holder.tag = convertView.findViewById(R.id.TvTagUii);
                holder.quantity = convertView.findViewById(R.id.QuantityDevice);
                holder.img = convertView.findViewById(R.id.DeviceImage);
                holder.maxBorrowDate = convertView.findViewById(R.id.MaxBorrowDate);
                holder.rfidConst = convertView.findViewById(R.id.rfid_const);
                holder.quantityConst = convertView.findViewById(R.id.quantity_const);
                holder.line4 = convertView.findViewById(R.id.line4);
                holder.line4.setVisibility(View.GONE);
                holder.MaxBorrowDate_const = convertView.findViewById(R.id.MaxBorrowDate_const);
                holder.MaxBorrowDate_const2 = convertView.findViewById(R.id.MaxBorrowDate_const2);
                holder.datePicker = convertView.findViewById(R.id.picker_date);
                holder.datePicker.setVisibility(View.GONE);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (radioGroup.getCheckedRadioButtonId() == R.id.name_selected) {
                holder.quantity.setVisibility(View.VISIBLE);
                holder.quantityConst.setVisibility(View.VISIBLE);
                holder.tag.setVisibility(View.INVISIBLE);
                holder.rfidConst.setVisibility(View.INVISIBLE);

                DeviceGroupName deviceGroupName = getGroupItem(position);
                String imgUrl = BuildConfig.BASE_URL + "/device" + "/image/" + deviceGroupName.getImg();
                holder.name.setText(deviceGroupName.getName());
                holder.quantity.setText(String.valueOf(deviceGroupName.getQuantity()));
                Picasso.get().load(imgUrl).into(holder.img);
                holder.maxBorrowDate.setText(String.valueOf(deviceGroupName.getMaxBorrowDays()));

                // if no device in storage, set color to transparent
                if (deviceGroupName.getQuantity() == 0) {
                    holder.name.setTextColor(ContextCompat.getColor(mContext, R.color.transparent));
                    holder.quantity.setTextColor(ContextCompat.getColor(mContext, R.color.transparent));
                    holder.quantityConst.setTextColor(ContextCompat.getColor(mContext, R.color.transparent));
                    holder.img.setAlpha(0.35f);
                    holder.maxBorrowDate.setTextColor(ContextCompat.getColor(mContext, R.color.transparent));
                    holder.MaxBorrowDate_const.setTextColor(ContextCompat.getColor(mContext, R.color.transparent));
                    holder.MaxBorrowDate_const2.setTextColor(ContextCompat.getColor(mContext, R.color.transparent));
                }
            } else if (radioGroup.getCheckedRadioButtonId() == R.id.tag_selected) {
                holder.quantity.setVisibility(View.INVISIBLE);
                holder.quantityConst.setVisibility(View.INVISIBLE);
                holder.tag.setVisibility(View.VISIBLE);
                holder.rfidConst.setVisibility(View.VISIBLE);

                Device device = getItem(position);
                String imgUrl = BuildConfig.BASE_URL + "/device" + "/image/" + device.getImg();
                holder.name.setText(device.getName());
                holder.tag.setText(device.getRfid());
                Picasso.get().load(imgUrl).into(holder.img);
                holder.maxBorrowDate.setText(String.valueOf(device.getMaxBorrowDays()));

                // if no device in storage, set color to transparent
                if (!Objects.equals(device.getRfidStatus(), "InStorage")) {
                    holder.name.setTextColor(ContextCompat.getColor(mContext, R.color.transparent));
                    holder.tag.setTextColor(ContextCompat.getColor(mContext, R.color.transparent));
                    holder.rfidConst.setTextColor(ContextCompat.getColor(mContext, R.color.transparent));
                    holder.img.setAlpha(0.35f);
                    holder.maxBorrowDate.setTextColor(ContextCompat.getColor(mContext, R.color.transparent));
                    holder.MaxBorrowDate_const.setTextColor(ContextCompat.getColor(mContext, R.color.transparent));
                    holder.MaxBorrowDate_const2.setTextColor(ContextCompat.getColor(mContext, R.color.transparent));
                }
            }

            return convertView;
        }

        public void filter(String charText) {
            charText = charText.toLowerCase();

            if (radioGroup.getCheckedRadioButtonId() == R.id.name_selected) {
                mGroupData.clear();
                if (charText.length() == 0) {
                    mGroupData.addAll(tmpGroupData);
                    notifyDataSetChanged();
                } else {
                    for (DeviceGroupName deviceGroupName : tmpGroupData) {
                        if (deviceGroupName.getName().toLowerCase().contains(charText)) {
                            mGroupData.add(deviceGroupName);
                        }
                        notifyDataSetChanged();
                    }

                }
            } else if (radioGroup.getCheckedRadioButtonId() == R.id.tag_selected) {
                mData.clear();
                if (charText.length() == 0) {
                    mData.addAll(tmpData);
                    notifyDataSetChanged();
                } else {
                    for (Device device : tmpData) {
                        if (device.getName().toLowerCase().contains(charText)) {
                            mData.add(device);
                        }
                        notifyDataSetChanged();
                    }
                }
            }

        }
    }

    public class ViewHolder {
        TextView name, tag, quantity, maxBorrowDate, rfidConst, quantityConst, MaxBorrowDate_const, MaxBorrowDate_const2;
        ImageView img;
        LinearLayout line3, line4;
        NumberPicker datePicker;
    }
}