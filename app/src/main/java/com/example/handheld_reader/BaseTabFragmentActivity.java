package com.example.handheld_reader;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.example.fragment.KeyDwonFragment;
import com.rscja.deviceapi.RFIDWithUHFUART;

public class BaseTabFragmentActivity  extends AppCompatActivity {
    public KeyDwonFragment currentFragment = null;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 139 || keyCode == 280 || keyCode == 293) {
            if (event.getRepeatCount() == 0) {
                if (currentFragment != null) {
                    currentFragment.myOnKeyDwon();
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
