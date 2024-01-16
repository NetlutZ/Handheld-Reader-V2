package com.example.session;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.fragment.Login;
import com.example.model.User;

public class SessionManagement {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String SHARED_PREF_NAME = "session";
    String SESSION_KEY = "session_user";
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
        }

    }
}
