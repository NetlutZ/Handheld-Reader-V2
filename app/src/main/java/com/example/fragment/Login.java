package com.example.fragment;

import android.app.Activity;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.handheld_reader.BuildConfig;
import com.example.handheld_reader.MainActivity;
import com.example.handheld_reader.R;
import com.example.model.User;
import com.example.session.SessionManagement;

import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Login extends Fragment {
    Activity activity = getActivity();
    EditText username, password;
    Button loginButton;
    String URL = BuildConfig.BASE_URL;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }

        if (getActivity() != null && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Login");
        }
        DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        username = getActivity().findViewById(R.id.userId_login);
        password = getActivity().findViewById(R.id.password_login);
        loginButton = getActivity().findViewById(R.id.button_login);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usernameText = username.getText().toString();
                String passwordText = password.getText().toString();

                OkHttpClient client = new OkHttpClient();
                RequestBody formBody = new FormBody.Builder()
                        .add("username", usernameText)
                        .add("password", passwordText)
                        .build();
                Request request = new Request.Builder()
                        .url(URL + "/login")
                        .post(formBody)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String myResponse = response.body().string();
                        JSONObject jsonObject = new JSONObject(myResponse);
                        if (jsonObject.getString("Login").equals("true")) {
                            Toast.makeText(getActivity(), "Login Successful", Toast.LENGTH_SHORT).show();
                            OkHttpClient client2 = new OkHttpClient();
                            Request request2 = new Request.Builder()
                                    .url(URL + "/user/username/" + usernameText)
                                    .get()
                                    .build();
                            try (Response response2 = client2.newCall(request2).execute()) {
                                JSONObject jsonObject2 = new JSONObject(response2.body().string());
                                int id = jsonObject2.getInt("id");
                                String username = jsonObject2.getString("username");
                                String name = jsonObject2.getString("name");
                                String email = jsonObject2.getString("email");
                                User user = new User(id, username, name, email);

                                // Save session
                                SessionManagement sessionManagement = new SessionManagement(getActivity());
                                sessionManagement.saveSession(user);

                                MainActivity.isloggedIn = true;
                                MainActivity.updateLoginStatus(getActivity());

                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Home()).commit();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else {
                            Toast.makeText(getActivity(), jsonObject.getString("error"), Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(getActivity(), R.string.server_error, Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        SessionManagement sessionManagement = new SessionManagement(getActivity());
        int userId = sessionManagement.getSession();

        if (userId != -1) {
            // User is logged in and redirect to home
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Home()).commit();
        } else {
            // User is not logged in
        }
    }
}