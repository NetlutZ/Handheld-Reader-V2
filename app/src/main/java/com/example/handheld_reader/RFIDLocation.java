package com.example.handheld_reader;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.view.UhfLocationCanvasView;
import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.interfaces.IUHF;
import com.rscja.deviceapi.interfaces.IUHFLocationCallback;

public class RFIDLocation extends AppCompatActivity {
    String TAG="UHF_LocationFragment";
    private RFIDWithUHFUART mReader;
    private UhfLocationCanvasView llChart;
    private EditText etEPC;
    private Button btStart,btStop;
    private PlaySoundThread playSoundThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfidlocation);

        playSoundThread = new PlaySoundThread();
        playSoundThread.start();

        llChart = findViewById(R.id.llChart);
        etEPC = findViewById(R.id.etEPC);
        btStart = findViewById(R.id.btStart);
        btStop = findViewById(R.id.btStop);

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
            Log.e("initUHF Error",ex.toString());
            return;
        }
    }

    /*
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView");
        stopLocation();
        playSoundThread.stopPlay();
        Log.i(TAG, "onDestroyView end");
    }

    @Override
    public void myOnKeyDwon() {
        if(btStart.isEnabled()) {
            startLocation();
        }else{
            stopLocation();
        }
    }

     */

    private void startLocation(){
        String epc=etEPC.getText().toString();
        if(epc.equals("")){
            Toast.makeText(RFIDLocation.this, R.string.location_fail,Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("EPC",epc.toString());
        boolean result= mReader.startLocation(RFIDLocation.this,epc, IUHF.Bank_EPC,32,new IUHFLocationCallback(){
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
        if(!result){
            Toast.makeText(RFIDLocation.this, R.string.psam_msg_fail,Toast.LENGTH_SHORT).show();
            Log.d("Location","FAIL");
            return;
        }
        btStart.setEnabled(false);
        etEPC.setEnabled(false);
    }

    public void stopLocation(){
        try {
            mReader.stopLocation();
            btStart.setEnabled(true);
            etEPC.setEnabled(true);
        }
        catch (Exception e){
            Log.e("Stop Location Error",e.toString());
        }
    }

    /**
     * 播放声音
     **/
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