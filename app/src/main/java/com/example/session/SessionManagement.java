package com.example.session;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.fragment.Login;
import com.example.handheld_reader.MainActivity;
import com.example.handheld_reader.R;
import com.example.model.User;

public class SessionManagement {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String SHARED_PREF_NAME = "session";
    String SESSION_KEY = "session_user_id";
    String SESSION_USERNAME = "session_username";
    String SESSION_NAME = "session_name";
    String SESSION_EMAIL = "session_email";
    String SESSION_TIME = "session_time";
    private Context context;

    public SessionManagement(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveSession(User user) {
        // save session of user whenever user is logged in
        int id = user.getId();
        editor.putInt(SESSION_KEY, id);
        editor.putString(SESSION_USERNAME, user.getUsername());
        editor.putString(SESSION_NAME, user.getName());
        editor.putString(SESSION_EMAIL, user.getEmail());
        editor.putLong(SESSION_TIME, System.currentTimeMillis());
        editor.commit();

    }

    public int getSession() {
        // return user id whose session is saved
        return sharedPreferences.getInt(SESSION_KEY, -1);
    }

    public void removeSession() {
        editor.putInt(SESSION_KEY, -1).commit();
    }

    public void checkSessionTimeout() {
        long sessionTime = sharedPreferences.getLong(SESSION_TIME, 0);
        long currentTime = System.currentTimeMillis();
        long timeout = 1000 * 20;

        if (currentTime - sessionTime > timeout) {
            removeSession();
            Toast.makeText(context, "Session has timed out", Toast.LENGTH_SHORT).show();
            MainActivity.isloggedIn = false;
            MainActivity.updateLoginStatus((Activity) context);

            ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Login()).commit();
        }

    }
}
