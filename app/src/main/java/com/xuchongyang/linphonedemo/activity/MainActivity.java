package com.xuchongyang.linphonedemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.xuchongyang.linphonedemo.R;
import com.xuchongyang.linphonedemo.service.LinphoneService;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        startLinphoneService();
    }

    @OnClick(R.id.sip_call_button)
    public void sipCall() {
        startActivity(new Intent(this, DialActivity.class));
    }

    @OnClick(R.id.settings)
    public void settings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void startLinphoneService() {
        if (!LinphoneService.isReady()) {
            startService(new Intent(Intent.ACTION_MAIN).setClass(this, LinphoneService.class));
        }
    }
}
