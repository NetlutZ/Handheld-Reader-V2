package com.example.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.handheld_reader.BuildConfig;
import com.example.handheld_reader.R;
import com.example.model.Device;
import com.example.session.SessionManagement;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.shawnlin.numberpicker.NumberPicker;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DeviceNotReturn extends Fragment {
    private Button cancelButton, confirmButton;
    private ListView deviceList;
    private CustomAdapter customAdapter;
    private String SHARED_PREF_NAME = "session";
    private String SESSION_KEY = "session_user_id";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_device_not_return, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }

        if (getActivity() != null && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Your Devices Borrowed");
        }
        DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        View test = getView().findViewById(R.id.bottomBar);
        cancelButton = (Button) test.findViewById(R.id.btnCancel);
        cancelButton.setOnClickListener(new ToHome());
        confirmButton = (Button) test.findViewById(R.id.btnConfirm);
        confirmButton.setVisibility(View.GONE);

        deviceList = getView().findViewById(R.id.deviceList);
        customAdapter = new CustomAdapter(getActivity().getApplicationContext());
        deviceList.setAdapter(customAdapter);

        GetDevice getDevice = new GetDevice();
        getDevice.execute();
    }

    private class ToHome implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Home()).commit();
        }
    }

    private class GetDevice extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            SessionManagement sessionManagement = new SessionManagement(getActivity());
            SharedPreferences pref = getActivity().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
            int userId = pref.getInt(SESSION_KEY, 0);
            try {
                OkHttpClient client = new OkHttpClient();
                String url = BuildConfig.BASE_URL + "/device/?userId=" + userId;

                Request request = new Request.Builder()
                        .url(url)
                        .build();

                Response response = client.newCall(request).execute();
                String myResponse = response.body().string();

                return myResponse;
            } catch (Exception e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), R.string.server_error, Toast.LENGTH_SHORT).show();
                    }
                });
                return null;
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s == null) {
                Toast.makeText(getActivity(), "No device found / Server Error", Toast.LENGTH_SHORT).show();
            } else {
                List<Device> contentList = new ArrayList<>();
                Gson gson = new Gson();
                Device[] devices = gson.fromJson(s, Device[].class);
                for (Device device : devices) {
                    contentList.add(device);
                }


                // Sort by return date

                for (int i = 0; i < contentList.size(); i++) {
                    for (int j = i + 1; j < contentList.size(); j++) {
                        SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd-MM-yyyy");
                        SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String rtDate = "";
                        if (!Objects.equals(contentList.get(i).getReturnDate(), "") && !Objects.equals(contentList.get(j).getReturnDate(), "")) {

                            try {
                                rtDate = outputDateFormat.format(inputDateFormat.parse(contentList.get(i).getReturnDate()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            LocalDate date = LocalDate.parse(rtDate);
                            String rtDate2 = "";
                            try {
                                rtDate2 = outputDateFormat.format(inputDateFormat.parse(contentList.get(j).getReturnDate()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            LocalDate date2 = LocalDate.parse(rtDate2);
                            if (date.isAfter(date2)) {
                                Device temp = contentList.get(i);
                                contentList.set(i, contentList.get(j));
                                contentList.set(j, temp);
                            }
                        }
                    }
                }

                customAdapter.setData(contentList);
            }
        }
    }

    private class CustomAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mInflater;
        private List<Device> mData = new ArrayList<>();

        private CustomAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
        }

        private void setData(List<Device> data) {
            mData = data;
            notifyDataSetChanged();
        }


        private void addData(Device data) {
            mData.add(data);
            notifyDataSetChanged();
        }

        private void removeData(int position) {
            mData.remove(position);
            notifyDataSetChanged();
        }

        private void clearData() {
            mData.clear();
            notifyDataSetChanged();
        }

        private class ViewHolder {
            TextView name, tag, quantity, maxBorrowDate, returnDate, rfidConst, quantityConst, MaxBorrowDate_const, MaxBorrowDate_const2;
            ImageView img, deleteButton;
            NumberPicker datePicker;
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

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.listtag_items_design, null);
                holder = new ViewHolder();
                holder.deleteButton = convertView.findViewById(R.id.btnDelete);
                holder.deleteButton.setVisibility(View.GONE);
                holder.name = convertView.findViewById(R.id.DeviceName);
                holder.tag = convertView.findViewById(R.id.TvTagUii);
                holder.quantity = convertView.findViewById(R.id.QuantityDevice);
                holder.quantity.setVisibility(View.GONE);
                holder.img = convertView.findViewById(R.id.DeviceImage);
                holder.maxBorrowDate = convertView.findViewById(R.id.MaxBorrowDate);
                holder.rfidConst = convertView.findViewById(R.id.rfid_const);
                holder.quantityConst = convertView.findViewById(R.id.quantity_const);
                holder.quantityConst.setVisibility(View.GONE);
                holder.MaxBorrowDate_const = convertView.findViewById(R.id.MaxBorrowDate_const);
                holder.MaxBorrowDate_const2 = convertView.findViewById(R.id.MaxBorrowDate_const2);
                holder.datePicker = convertView.findViewById(R.id.picker_date);
                holder.datePicker.setVisibility(View.GONE);
                holder.returnDate = convertView.findViewById(R.id.return_date);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Device device = mData.get(position);
            String imgUrl = BuildConfig.BASE_URL + "/device" + "/image/" + device.getImg();
            Picasso.get().load(imgUrl).into(holder.img);
            holder.quantity.setVisibility(View.GONE);
            holder.name.setText(device.getName());
            holder.tag.setText(device.getRfid());
            holder.maxBorrowDate.setText(String.valueOf(device.getMaxBorrowDays()));
            holder.returnDate.setText(device.getReturnDate());

            return convertView;
        }
    }

}