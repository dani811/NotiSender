package com.noti.main.ui.pair;

import static com.noti.main.Application.pairingProcessList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.noti.main.Application;
import com.noti.main.R;
import com.noti.main.service.NotiListenerService;
import com.noti.main.service.pair.PairDeviceInfo;
import com.noti.main.ui.receive.ExitActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class PairAcceptActivity extends AppCompatActivity {
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair_accept);
        Intent intent = getIntent();
        MaterialButton AcceptButton = findViewById(R.id.ok);
        MaterialButton CancelButton = findViewById(R.id.cancel);

        String Device_name = intent.getStringExtra("device_name");
        String Device_id = intent.getStringExtra("device_id");

        TextView info = findViewById(R.id.notiDetail);
        info.setText(Html.fromHtml("Are you sure you want to grant the pairing request?<br><b>Requested Device:</b> " + Device_name));

        AcceptButton.setOnClickListener(v -> {
            sendAcceptedMessage(Device_name, Device_id, true, this);
            SharedPreferences prefs = getSharedPreferences("com.noti.main_pair", MODE_PRIVATE);
            boolean isNotRegistered = true;
            String dataToSave = Device_name + "|" + Device_id;

            Set<String> list = new HashSet<>(prefs.getStringSet("paired_list", new HashSet<>()));
            for(String str : list) {
                if(str.equals(dataToSave)) {
                    isNotRegistered = false;
                    break;
                }
            }

            if(isNotRegistered) {
                list.add(dataToSave);
                prefs.edit().putStringSet("paired_list", list).apply();
            }
        });
        CancelButton.setOnClickListener(v -> sendAcceptedMessage(Device_name, Device_id, false, this));
    }

    public static void sendAcceptedMessage(String Device_name, String Device_id, boolean isAccepted, Context context) {
        String Topic = "/topics/" + context.getSharedPreferences("com.noti.main_preferences", MODE_PRIVATE).getString("UID","");
        JSONObject notificationHead = new JSONObject();
        JSONObject notificationBody = new JSONObject();
        try {
            notificationBody.put("type","pair|accept_pair");
            notificationBody.put("device_name", Build.MANUFACTURER  + " " + Build.MODEL);
            notificationBody.put("device_id", NotiListenerService.getUniqueID());
            notificationBody.put("send_device_name", Device_name);
            notificationBody.put("send_device_id", Device_id);
            notificationBody.put("pair_accept", isAccepted);
            notificationHead.put("to",Topic);
            notificationHead.put("priority", "high");
            notificationHead.put("data", notificationBody);
        } catch (JSONException e) {
            Log.e("Noti", "onCreate: " + e.getMessage() );
        }

        NotiListenerService.sendNotification(notificationHead, "pair.func", context);
        ExitActivity.exitApplication(context);

        if(isAccepted) {
            for(PairDeviceInfo info : pairingProcessList) {
                if(info.getDevice_name().equals(Device_name) && info.getDevice_id().equals(Device_id)) {
                    Application.isListeningToPair = false;
                    pairingProcessList.remove(info);
                    break;
                }
            }
        }
    }
}
