package com.noti.main.ui.pair;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.noti.main.R;
import com.noti.main.ui.OptionActivity;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@SuppressLint("SetTextI18n")
public class PairMainActivity extends AppCompatActivity {

    SharedPreferences pairPrefs;
    LinearLayout deviceListLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair_main);

        if(Build.VERSION.SDK_INT < 29) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        }

        LinearLayout addNewDevice = findViewById(R.id.addNewDevice);
        LinearLayout connectionPreference = findViewById(R.id.connectionPreference);
        TextView deviceNameInfo = findViewById(R.id.deviceNameInfo);
        deviceListLayout = findViewById(R.id.deviceListLayout);
        pairPrefs = getSharedPreferences("com.noti.main_pair", MODE_PRIVATE);

        deviceNameInfo.setText("Visible as \"" + Build.MODEL + "\" to other devices");
        addNewDevice.setOnClickListener(v -> startActivity(new Intent(this, PairingActivity.class)));
        connectionPreference.setOnClickListener(v -> startActivity(new Intent(this, OptionActivity.class).putExtra("Type", "Pair")));

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener((v) -> this.finish());

        loadDeviceList();
        pairPrefs.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
            if(key.equals("paired_list")) {
                deviceListLayout.removeViews(0, deviceListLayout.getChildCount());
                PairMainActivity.this.runOnUiThread(this::loadDeviceList);
            }
        });
    }

    void loadDeviceList() {
        Set<String> list = pairPrefs.getStringSet("paired_list", new HashSet<>());
        for(String string : list) {
            String[] data = string.split("\\|");
            RelativeLayout layout = (RelativeLayout) View.inflate(PairMainActivity.this, R.layout.cardview_pair_device_setting, null);
            Holder holder = new Holder(layout);

            String[] colorLow = PairMainActivity.this.getResources().getStringArray(R.array.material_color_low);
            String[] colorHigh = PairMainActivity.this.getResources().getStringArray(R.array.material_color_high);
            int randomIndex = new Random(data[0].hashCode()).nextInt(colorHigh.length);

            holder.deviceName.setText(data[0]);
            holder.icon.setImageTintList(ColorStateList.valueOf(Color.parseColor(colorHigh[randomIndex])));
            holder.icon.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(colorLow[randomIndex])));
            holder.setting.setOnClickListener(v -> {
                Intent intent = new Intent(this, PairDetailActivity.class);
                intent.putExtra("device_name", data[0]);
                intent.putExtra("device_id", data[1]);
                startActivity(intent);
            });
            holder.baseLayout.setOnClickListener(v -> {
                Intent intent = new Intent(this, RequestActionActivity.class);
                intent.putExtra("device_name", data[0]);
                intent.putExtra("device_id", data[1]);
                startActivity(intent);
            });

            deviceListLayout.addView(layout);
        }
    }

    static class Holder {
        TextView deviceName;
        TextView pairStatus;
        RelativeLayout baseLayout;
        ImageView icon;
        ImageView setting;

        Holder(View view) {
            deviceName = view.findViewById(R.id.deviceName);
            pairStatus = view.findViewById(R.id.deviceStatus);
            baseLayout = view.findViewById(R.id.baseLayout);
            icon = view.findViewById(R.id.icon);
            setting = view.findViewById(R.id.deviceDetail);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100) {
            for (int foo : grantResults) {
                if (foo != PackageManager.PERMISSION_GRANTED) {
                    finish();
                }
            }
        }
    }
}