package com.example.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.handheld_reader.BuildConfig;
import com.example.handheld_reader.MainActivity;
import com.example.handheld_reader.R;
import com.example.model.Device;
import com.example.model.DeviceGroupName;
import com.example.session.SessionManagement;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RFIDScan extends KeyDwonFragment {
    private boolean loopFlag = false;
    private int inventoryFlag = 1;
    private Handler handler;
    private TextView tv_count;

    private RadioGroup RgInventory;
    private RadioButton RbInventorySingle;
    private RadioButton RbInventoryLoop;

    private Button BtClear;
    private Button BtInventory;

    private ListView LvTags;
    private RFIDWithUHFUART mReader;

    private String fCurFilePath = "";
    private boolean fIsEmulator = false;

    private String grnId = "";
    private String productId = "";
    private boolean isMapping = false;
    public MainActivity mContext;
    Activity activity = getActivity();
    private String function = "";
    CustomAdapter customAdapter;
    private Button cancelButton, confirmButton;
    private String SHARED_PREF_NAME = "session";
    private String SESSION_KEY = "session_user_id";
    private String URL = BuildConfig.BASE_URL;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rfidscan, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = (MainActivity) getActivity();
        mContext.currentFragment = this;

        BtClear = (Button) getView().findViewById(R.id.BtClear);
        tv_count = (TextView) getView().findViewById(R.id.tv_count);
        RgInventory = (RadioGroup) getView().findViewById(R.id.RgInventory);
        RbInventorySingle = (RadioButton) getView().findViewById(R.id.RbInventorySingle);
        RbInventoryLoop = (RadioButton) getView().findViewById(R.id.RbInventoryLoop);
        BtInventory = (Button) getView().findViewById(R.id.BtInventory);
        LvTags = (ListView) getView().findViewById(R.id.LvTags);

        View test = getView().findViewById(R.id.bottomBar);
        cancelButton = (Button) test.findViewById(R.id.btnCancel);
        cancelButton.setOnClickListener(new ToHome());
        confirmButton = (Button) test.findViewById(R.id.btnConfirm);
        confirmButton.setOnClickListener(new Confirm());


        /*
        Bundle bundle = activity.getIntent().getExtras();
        if (bundle != null) {
            productId = bundle.getString("product_id");
            grnId = bundle.getString("grn_id");
            isMapping = bundle.getBoolean("is_mapping");
        }
         */

        customAdapter = new CustomAdapter(getActivity().getApplicationContext());
        LvTags.setAdapter(customAdapter);

        BtClear.setOnClickListener(new RFIDScan.BtClearClickListener());
        RgInventory.setOnCheckedChangeListener(new RFIDScan.RgInventoryCheckedListener());
        BtInventory.setOnClickListener(new RFIDScan.BtInventoryClickListener());

        // FOR TEST LOCATION // TODO - DELETE THIS
        /*
        LvTags.setOnItemClickListener((parent, view, position, id) -> {
            HashMap<String, String> map = (HashMap<String, String>) LvTags.getItemAtPosition(position);
            Device device = new Device(0, "", "", map.get("tagUii"), "", "", "", "", 0, 0, "");
            Bundle bundle = new Bundle();
            bundle.putSerializable("device", device);

            // Create a new instance of the RFIDLocation fragment
            RFIDLocation fragment = new RFIDLocation();
            fragment.setArguments(bundle);

            // Move to RFIDLocation fragment with data
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        });
         */


        clearData();

        handler = new Handler() {
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(Message msg) {
                String result = msg.obj + "";
                String[] strs = result.split("@");
                if (addEPCToList(strs[0], strs[1])) {
//                    UIHelper.playSoundSuccess();
                }

            }
        };

        Bundle bundle = getArguments();
        if (bundle != null) {
            String fnBundle = bundle.getString("function");
            if (fnBundle != null) {
                function = fnBundle;
            }
        }

//        fIsEmulator = UIHelper.isEmulator();
//        UIHelper.initSound(RFIDScan.this);
        initUHF();

        //TODO - DELETE THIS
        GetTmpDevice getTmpDevice = new GetTmpDevice();
        getTmpDevice.execute();
        // tmpData();
    }

    //TODO - DELETE THIS
    private void tmpData() {
        /*
        List<Device> listDevice = new ArrayList<>();
        listDevice.add(new Device(1, "AA1", "", "E2000016911800611910C0F0", getString(R.string.Borrowed), "", "","", 0, 0, 0, ""));
        listDevice.add(new Device(2, "AA2", "", "E2000016911800611910C0F1", "", "", "", "", 0, 3, 0, ""));
        listDevice.add(new Device(3, "AA3", "", "E2000016911800611910C0F2", "", "", "", "", 0, 0, 0, ""));
        listDevice.add(new Device(4, "AA4", "", "E2000016911800611910C0F3", "", "", "", "", 0, 3, 0, ""));
        listDevice.add(new Device(5, "AA5", "", "E2000016911800611910C0F4", "", "", "", "", 0, 3, 0, ""));
        listDevice.add(new Device(6, "AA6", "", "E2000016911800611910C0F5", "", "", "", "", 0, 3, 0, ""));
        customAdapter.setData(listDevice);

         */
    }

    //TODO - DELETE THIS
    public class GetTmpDevice extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                OkHttpClient client = new OkHttpClient();
                String url = BuildConfig.BASE_URL + "/device";

                Request request = new Request.Builder()
                        .url(url)
                        .build();

                Response response = client.newCall(request).execute();
                String myResponse = response.body().string();
                JsonArray jsonArray = new Gson().fromJson(myResponse, JsonArray.class);
                String tmp = "[" + jsonArray.get(0).toString();
                for (int i = 1; i < 6; i++) {
                    tmp += "," + jsonArray.get(i).toString();

                }
                tmp += "]";
                return tmp;
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
                List<Device> contentList = new ArrayList<>();
                Gson gson = new Gson();
                Device[] devices = gson.fromJson(s, Device[].class);
                for (Device device : devices) {
                    contentList.add(device);
                }
                customAdapter.setData(contentList);
            }
        }
    }

    private class ToHome implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mContext.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Home()).commit();
        }
    }

    private class Confirm implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // Check session and get userId
            SessionManagement sessionManagement = new SessionManagement(getActivity());
            sessionManagement.checkSessionTimeout();
            boolean canFunction = true;
            if(sessionManagement.getSession() == -1) {
                canFunction = false;
            }
            SharedPreferences pref = getActivity().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
            int userId = pref.getInt(SESSION_KEY, 0);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String currentDateTime = dtf.format(now);

            if (function.equals("borrow")) {
                for (int i = 0; i < customAdapter.getCount(); i++) {
                    Device device = customAdapter.getItem(i);
                    if (device.getRfidStatus() != null) {
                        if (device.getRfidStatus().equals(getString(R.string.Borrowed))) {
                            Toast.makeText(getActivity(), "Cannot borrow device because " + device.getRfid() + " is Borrowed", Toast.LENGTH_SHORT).show();
                            canFunction = false;
                            break;
                        }
                    }
                }

                if (customAdapter.getCount() == 0) {
                    canFunction = false;
                    Toast.makeText(getActivity(), "Cannot borrow device because no device is scanned", Toast.LENGTH_SHORT).show();
                }

                if (canFunction) {
                    List<Integer> listDeviceId = new ArrayList<>();
                    String activityDeviceId = "";
                    String lastBorrowActivityCode;

                    for (int i = 0; i < customAdapter.getCount(); i++) {
                        Device device = customAdapter.getItem(i);
                        Log.d("RFIDScan Borrow", "device: " + device.getRfid());
                        listDeviceId.add(device.getId());
                        if (i == 0) {
                            activityDeviceId += device.getId();
                        } else {
                            activityDeviceId += "," + device.getId();
                        }
                    }

                    //get last borrow activityCode
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(URL + "/activity/lastest/borrow")
                            .get()
                            .build();
                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful()) {
                            String myResponse = response.body().string();
                            if (myResponse.equals("null")) {
                                lastBorrowActivityCode = "B-1";
                            } else {
                                JSONObject jsonObject = new JSONObject(myResponse);
                                lastBorrowActivityCode = jsonObject.getString("activityCode");
                            }
                            int lastBorrowActivityId = Integer.parseInt(lastBorrowActivityCode.split("B")[1]);
                            lastBorrowActivityId++;


                            //insert new activity
                            String activityCode = "B" + lastBorrowActivityId;

                            OkHttpClient client2 = new OkHttpClient();
                            RequestBody formBody = new FormBody.Builder()
                                    .add("activityCode", activityCode)
                                    .add("userId", String.valueOf(userId))
                                    .add("device", activityDeviceId)
                                    .add("activityDate", currentDateTime)
                                    .add("activityTime", currentDateTime.substring(11, 19))
                                    .build();
                            Request request2 = new Request.Builder()
                                    .url(URL + "/activity")
                                    .post(formBody)
                                    .build();

                            try (Response response2 = client2.newCall(request2).execute()) {
                                if (response2.isSuccessful()) {
                                    String myResponse2 = response2.body().string();
                                    JSONObject jsonObject2 = new JSONObject(myResponse2);
                                    int activityId = jsonObject2.getInt("id");

                                    // Update device rfidStatus to Borrowed, userId and activityId
                                    for (int i = 0; i < listDeviceId.size(); i++) {
                                        OkHttpClient client3 = new OkHttpClient();
                                        RequestBody formBody3 = new FormBody.Builder()
                                                .add("rfidStatus", getString(R.string.Borrowed))
                                                .add("userId", String.valueOf(userId))
                                                .add("activityId", String.valueOf(activityId))
                                                .build();
                                        Request request3 = new Request.Builder()
                                                .url(URL + "/device/" + listDeviceId.get(i))
                                                .put(formBody3)
                                                .build();
                                        try (Response response3 = client3.newCall(request3).execute()) {
                                            if (response3.isSuccessful()) {
                                                String myResponse3 = response3.body().string();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SuccessFunction()).commit();
                }
            } else if (function.equals("return")) {
                for (int i = 0; i < customAdapter.getCount(); i++) {
                    Device device = customAdapter.getItem(i);
                    if (device.getUserId() != userId) {
                        Toast.makeText(getActivity(), "Cannot return device because " + device.getRfid() + " is not borrowed by you", Toast.LENGTH_SHORT).show();
                        canFunction = false;
                        break;
                    }
                }

                if (customAdapter.getCount() == 0) {
                    canFunction = false;
                    Toast.makeText(getActivity(), "Cannot return device because no device is scanned", Toast.LENGTH_SHORT).show();
                }

                if (canFunction) {
                    List<Integer> listDeviceId = new ArrayList<>();
                    String activityDeviceId = "";
                    String lastReturnActivityCode;

                    for (int i = 0; i < customAdapter.getCount(); i++) {
                        Device device = customAdapter.getItem(i);
                        Log.d("RFIDScan Return", "device: " + device.getRfid());
                        listDeviceId.add(device.getId());
                        if (i == 0) {
                            activityDeviceId += device.getId();
                        } else {
                            activityDeviceId += "," + device.getId();
                        }
                    }

                    //get last return activityCode
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(URL + "/activity/lastest/return")
                            .get()
                            .build();
                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful()) {
                            String myResponse = response.body().string();
                            if (myResponse.equals("null")) {
                                lastReturnActivityCode = "R-1";
                            } else {
                                JSONObject jsonObject = new JSONObject(myResponse);
                                lastReturnActivityCode = jsonObject.getString("activityCode");
                            }
                            int lastReturnActivityId = Integer.parseInt(lastReturnActivityCode.split("R")[1]);
                            lastReturnActivityId++;

                            //insert new activity
                            String activityCode = "R" + lastReturnActivityId;

                            OkHttpClient client2 = new OkHttpClient();
                            RequestBody formBody = new FormBody.Builder()
                                    .add("activityCode", activityCode)
                                    .add("userId", String.valueOf(userId))
                                    .add("device", activityDeviceId)
                                    .add("activityDate", currentDateTime)
                                    .add("activityTime", currentDateTime.substring(11, 19))
                                    .build();
                            Request request2 = new Request.Builder()
                                    .url(URL + "/activity")
                                    .post(formBody)
                                    .build();

                            try (Response response2 = client2.newCall(request2).execute()) {
                                if (response2.isSuccessful()) {
                                    String myResponse2 = response2.body().string();
                                    JSONObject jsonObject2 = new JSONObject(myResponse2);
                                    int activityId = jsonObject2.getInt("id");

                                    // Update device rfidStatus to InStorage, userId and activityId
                                    for (int i = 0; i < listDeviceId.size(); i++) {
                                        OkHttpClient client3 = new OkHttpClient();
                                        String json = "{ \"rfidStatus\": \"" + getString(R.string.InStorage) + "\", \"userId\": null, \"activityId\": null }";
                                        RequestBody requestBody = RequestBody.create(json, MediaType.parse("application/json"));
                                        Request request3 = new Request.Builder()
                                                .url(URL + "/device/" + listDeviceId.get(i))
                                                .put(requestBody)
                                                .build();
                                        try (Response response3 = client3.newCall(request3).execute()) {
                                            if (response3.isSuccessful()) {
                                                String myResponse3 = response3.body().string();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();

                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SuccessFunction()).commit();
                }
            }
        }
    }

    /*
    @Override
    public void onResume() {
        super.onResume();
        SessionManagement sessionManagement = new SessionManagement(getActivity());
        sessionManagement.checkSessionTimeout();
    }

     */


    public void initUHF() {
        try {
            mReader = RFIDWithUHFUART.getInstance();
        } catch (Exception ex) {
            Log.e("initUHF Error", ex.toString());
            return;
        }

        if (mReader != null) {
            new RFIDScan.InitTask().execute();
        }
    }

    public class InitTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                return mReader.init();
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
                Toast.makeText(activity, "Init failed", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            try {
                super.onPreExecute();

                mypDialog = new ProgressDialog(activity);
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
    public void onPause() {
        super.onPause();
        stopInventory();
    }


    @Override
    public void myOnKeyDwon() {
        readTag();
    }


    private void stopInventory() {
        if (loopFlag) {
            loopFlag = false;
            setViewEnabled(true);
            if (mReader.stopInventory()) {
                BtInventory.setText(getString(R.string.btInventory));
            } else {
                Toast.makeText(activity, R.string.uhf_msg_inventory_stop_fail, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setViewEnabled(boolean enabled) {
        RbInventorySingle.setEnabled(enabled);
        RbInventoryLoop.setEnabled(enabled);
        BtClear.setEnabled(enabled);
    }

    private boolean addEPCToList(String epc, String rssi) {
        if (!TextUtils.isEmpty(epc)) {
            int index = checkIsExist(epc);

            Device device;
            if (customAdapter.getCount() % 2 == 0) {
                device = new Device(0, "AA", "", epc, getString(R.string.Borrowed), "", "", "", 0, 0, 0, "", "");
            } else {
                device = new Device(0, "AA", "", epc, "", "", "", "", 0, 0, 0, "", "");
            }
            if (index == -1) {
                customAdapter.addData(device);
                tv_count.setText("" + customAdapter.getCount());
            } else {
                device = customAdapter.getItem(index);
                device.setMaxBorrowDate(device.getMaxBorrowDate() + 1);
                customAdapter.notifyDataSetChanged();
            }

            if (index >= 0)
                return false;

            return true;
        }
        return false;
    }

    public int checkIsExist(String strEPC) {
        int existFlag = -1;
        if (strEPC == null || strEPC.length() == 0) {
            return existFlag;
        }
        String tempStr = "";

        for (int i = 0; i < customAdapter.getCount(); i++) {
            Device device = customAdapter.getItem(i);
            tempStr = device.getRfid();
            if (strEPC.equals(tempStr)) {
                existFlag = i;
                break;
            }
        }
        return existFlag;
    }

    private class BtClearClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            clearData();
        }
    }

    private void clearData() {
        tv_count.setText("0");
        customAdapter.clearData();
    }

    public class RgInventoryCheckedListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == RbInventorySingle.getId()) {
                inventoryFlag = 0;
            } else if (checkedId == RbInventoryLoop.getId()) {
                inventoryFlag = 1;
            }
        }
    }

    public class BtInventoryClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            readTag();
        }
    }

    private void readTag() {
        if (BtInventory.getText().equals(getString(R.string.btInventory))) {
//            if (mReader == null && false) {
            if (mReader == null) {
                Toast.makeText(activity, R.string.uhf_msg_sdk_open_fail, Toast.LENGTH_SHORT);
                return;
            }

            switch (inventoryFlag) {
                case 0: // Single
                {
                    UHFTAGInfo strUII = mReader.inventorySingleTag();
                    if (strUII != null) {
                        String strEPC = strUII.getEPC();
                        addEPCToList(strEPC, strUII.getRssi());
//                        UIHelper.playSoundSuccess();
                        tv_count.setText("" + customAdapter.getCount());
                    } else {
                        Toast.makeText(activity, R.string.uhf_msg_inventory_fail, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
                case 1: // Loop Scan
                {
                    if (mReader.startInventoryTag()) {
//                    if (true) {
                        BtInventory.setText(getString(R.string.title_stop_Inventory));
                        loopFlag = true;
                        setViewEnabled(false);
                        new RFIDScan.TagThread().start();
                    } else {
                        mReader.stopInventory();
                        Toast.makeText(activity, R.string.uhf_msg_inventory_open_fail, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
                default:
                    break;
            }
        } else {
            stopInventory();
        }
    }

    private class TagThread extends Thread {
        public void run() {
            String strTid;
            String strResult;
            UHFTAGInfo res = null;
            while (loopFlag) {
                res = mReader.readTagFromBuffer();
//                if (res != null || true) {
                if (res != null) {
                    strTid = res.getTid();
                    if (strTid.length() != 0 && !strTid.equals("0000000" + "000000000") && !strTid.equals("000000000000000000000000")) {
                        strResult = "TID:" + strTid + "\n";
                    } else {
                        strResult = "";
                    }

                    Message msg = handler.obtainMessage();
                    msg.obj = strResult + res.getEPC() + "@" + res.getRssi();
//                    int random = (int)(Math.random() * 3);
//                    msg.obj = temp[random] + "@" + randomStr(14);
                    handler.sendMessage(msg);
                }
            }
        }
    }

    public class CustomAdapter extends BaseAdapter {
        public Context mContext;
        public LayoutInflater mInflater;
        List<Device> mData = new ArrayList<>();
        ArrayList<Device> tmpData = new ArrayList<>();

        public CustomAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
        }

        public void setData(List<Device> data) {
            mData = data;
            tmpData.addAll(mData);
            notifyDataSetChanged();
        }


        public void addData(Device data) {
            mData.add(data);
            tmpData.add(data);
            notifyDataSetChanged();
        }

        public void removeData(int position) {
            mData.remove(position);
            tmpData.remove(position);
            tv_count.setText("" + getCount());
            notifyDataSetChanged();
        }

        public void clearData() {
            mData.clear();
            tmpData.clear();
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

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.listtag_items_design, null);
                holder = new ViewHolder();
                holder.deleteButton = convertView.findViewById(R.id.btnDelete);
                holder.name = convertView.findViewById(R.id.DeviceName);
                holder.tag = convertView.findViewById(R.id.TvTagUii);
                holder.quantity = convertView.findViewById(R.id.QuantityDevice);
                holder.img = convertView.findViewById(R.id.DeviceImage);
                holder.maxBorrowDate = convertView.findViewById(R.id.MaxBorrowDate);
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
            holder.maxBorrowDate.setText(String.valueOf(device.getMaxBorrowDate()));
            holder.deleteButton.setOnClickListener(v -> {
                removeData(position);
            });


            return convertView;
        }

    }

    public class ViewHolder {
        TextView name;
        TextView tag;
        TextView quantity;
        ImageView img, deleteButton;
        TextView maxBorrowDate;
    }

}