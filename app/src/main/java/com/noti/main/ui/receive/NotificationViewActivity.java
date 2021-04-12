package com.noti.main.ui.receive;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.toolbox.JsonObjectRequest;
import com.noti.main.utils.JsonRequest;
import com.noti.main.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class NotificationViewActivity extends Activity {

    void Log(String message) {
        if (getSharedPreferences("com.noti.main_preferences", MODE_PRIVATE).getBoolean("debugInfo", false) ) {
            Log.d("debug",message);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notidetail);

        Intent intent = getIntent();
        String TOPIC = "/topics/" + getSharedPreferences("com.noti.main_preferences",MODE_PRIVATE).getString("UID","");
        String Package = intent.getStringExtra("package");
        Bitmap icon = intent.getParcelableExtra("icon");

        Button OK = findViewById(R.id.ok);
        Button NO = findViewById(R.id.cancel);
        ImageView ICON = findViewById(R.id.iconView);
        TextView DETAIL = findViewById(R.id.notiDetail);

        String APP_NAME = intent.getStringExtra("appname");
        String TITLE = intent.getStringExtra("title");
        String DEVICE_NAME = intent.getStringExtra("device_name");
        String DATE = intent.getStringExtra("date");
        String detail = "";
        detail += "App Name : " + APP_NAME + "\n";
        detail += "Noti Title : " + TITLE + "\n";
        detail += "Device : " + DEVICE_NAME + "\n";
        detail += "Posted Time : " + DATE + "\n";

        if(icon != null) ICON.setImageBitmap(icon);
        else ICON.setVisibility(View.GONE);
        DETAIL.setText(detail);

        OK.setOnClickListener(v -> {
            JSONObject notificationHead = new JSONObject();
            JSONObject notifcationBody = new JSONObject();
            try {
                notifcationBody.put("package", Package);
                notifcationBody.put("type","reception|normal");
                notifcationBody.put("device_name", Build.MANUFACTURER  + " " + Build.MODEL);
                notifcationBody.put("send_device_name",DEVICE_NAME);
                notifcationBody.put("send_device_id",intent.getStringExtra("device_id"));
                notificationHead.put("to", TOPIC);
                notificationHead.put("data", notifcationBody);
            } catch (JSONException e) {
                Log.e("Noti", "onCreate: " + e.getMessage() );
            }
            Log(notificationHead.toString());
            sendNotification(notificationHead);
            ExitActivity.exitApplication(this);
        });

        NO.setOnClickListener(v -> ExitActivity.exitApplication(this));
    }

    private void sendNotification(JSONObject notification) {
        final String FCM_API = "https://fcm.googleapis.com/fcm/send";
        final String serverKey = "key=" + getSharedPreferences("com.noti.main_preferences", MODE_PRIVATE).getString("ApiKey_FCM","");
        final String contentType = "application/json";
        final String TAG = "NOTIFICATION TAG";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                response -> Log.i(TAG, "onResponse: " + response.toString()),
                error -> Toast.makeText(NotificationViewActivity.this, "Failed to send Notification! Please check internet and try again!", Toast.LENGTH_SHORT).show()){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };
        JsonRequest.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }
}