package com.example.handheld_reader;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;

import java.util.ArrayList;
import java.util.HashMap;

public class RFIDScan extends AppCompatActivity {
    private boolean loopFlag = false;
    private int inventoryFlag = 1;
    private Handler handler;
    private ArrayList<HashMap<String, String>> tagList;
    private SimpleAdapter adapter;

    private TextView tv_count;

    private RadioGroup RgInventory;
    private RadioButton RbInventorySingle;
    private RadioButton RbInventoryLoop;

    private Button BtClear;
    private Button BtImport;
    private Button BtInventory;
    private Button BtHome;

    private ListView LvTags;
    private HashMap<String, String> map;
    private RFIDWithUHFUART mReader;

    private String fCurFilePath = "";
    private boolean fIsEmulator = false;

    private String grnId = "";
    private String productId = "";
    private boolean isMapping = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfidscan);

        setTitle(getString(R.string.app_name));

        tagList = new ArrayList<HashMap<String, String>>();
        BtClear = (Button) findViewById(R.id.BtClear);
        BtImport = (Button) findViewById(R.id.BtImport);
        BtHome = (Button) findViewById(R.id.BtHome);
        tv_count = (TextView) findViewById(R.id.tv_count);
        RgInventory = (RadioGroup) findViewById(R.id.RgInventory);
        RbInventorySingle = (RadioButton) findViewById(R.id.RbInventorySingle);
        RbInventoryLoop = (RadioButton) findViewById(R.id.RbInventoryLoop);
        BtInventory = (Button) findViewById(R.id.BtInventory);
        LvTags = (ListView) findViewById(R.id.LvTags);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            productId = bundle.getString("product_id");
            grnId = bundle.getString("grn_id");
            isMapping = bundle.getBoolean("is_mapping");
        }

        adapter = new SimpleAdapter(this, tagList, R.layout.listtag_items,
                new String[]{"tagUii", "tagLen", "tagCount"},
                new int[]{R.id.TvTagUii, R.id.TvTagLen, R.id.TvTagCount});

        BtClear.setOnClickListener(new BtClearClickListener());
//        BtImport.setOnClickListener(new BtImportClickListener());
        RgInventory.setOnCheckedChangeListener(new RgInventoryCheckedListener());
        BtInventory.setOnClickListener(new BtInventoryClickListener());
//        BtHome.setOnClickListener(new BtHomeClickListener());

        LvTags.setAdapter(adapter);
        clearData();

        handler = new Handler() {
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(Message msg) {
                String result = msg.obj + "";
                String[] strs = result.split("@");
                if (addEPCToList(strs[0], strs[1])){
//                    UIHelper.playSoundSuccess();
                }

            }
        };

//        fIsEmulator = UIHelper.isEmulator();
//        UIHelper.initSound(RFIDScan.this);
        initUHF();
    }

    public void initUHF() {
        try {
            mReader = RFIDWithUHFUART.getInstance();
        } catch (Exception ex) {
            Log.e("initUHF Error",ex.toString());
            return;
        }

        if (mReader != null) {
            new InitTask().execute();
        }
    }

    public class InitTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                return mReader.init();
            }
            catch (Exception ex){
                Log.e("InitTask",ex.toString());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mypDialog.cancel();
            if (!result) {
                Toast.makeText(RFIDScan.this, "init fail", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            try {
                super.onPreExecute();

                mypDialog = new ProgressDialog(RFIDScan.this);
                mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mypDialog.setMessage("init...");
                mypDialog.setCanceledOnTouchOutside(false);
                mypDialog.show();

            } catch (Exception ex) {
                Log.e("InitTask",ex.toString());
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 139 || keyCode == 280 || keyCode == 293) {
            if (event.getRepeatCount() == 0) {
                readTag();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void stopInventory() {
        if (loopFlag) {
            loopFlag = false;
            setViewEnabled(true);
            if (mReader.stopInventory()) {
//            if (true) {
                BtInventory.setText(getString(R.string.btInventory));
            } else {
                Toast.makeText(RFIDScan.this, R.string.uhf_msg_inventory_stop_fail,Toast.LENGTH_SHORT).show();
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

            map = new HashMap<String, String>();
            map.put("tagUii", epc);
            map.put("tagCount", String.valueOf(1));
            map.put("tagRssi", rssi);

            if (index == -1) {
                tagList.add(map);
                LvTags.setAdapter(adapter);
                tv_count.setText("" + adapter.getCount());
            } else {
                int tagcount = Integer.parseInt(tagList.get(index).get("tagCount"), 10) + 1;

                map.put("tagCount", String.valueOf(tagcount));
                tagList.set(index, map);
            }

            adapter.notifyDataSetChanged();
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
        for (int i = 0; i < tagList.size(); i++) {
            HashMap<String, String> temp = new HashMap<String, String>();
            temp = tagList.get(i);
            tempStr = temp.get("tagUii");
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
        tagList.clear();

        adapter.notifyDataSetChanged();
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
        if (BtInventory.getText().equals(getString(R.string.btInventory)))
        {
//            if (mReader == null && false) {
            if (mReader == null) {
                Toast.makeText(RFIDScan.this, R.string.uhf_msg_sdk_open_fail,Toast.LENGTH_SHORT).show();
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
                        tv_count.setText("" + adapter.getCount());
                    } else {
                        Toast.makeText(RFIDScan.this, R.string.uhf_msg_inventory_fail,Toast.LENGTH_SHORT).show();
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
                        new TagThread().start();
                    } else {
                        mReader.stopInventory();
                        Toast.makeText(RFIDScan.this, R.string.uhf_msg_inventory_open_fail,Toast.LENGTH_SHORT).show();
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


}