package com.xuchongyang.linphonedemo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


import com.xuchongyang.linphonedemo.R;
import com.xuchongyang.linphonedemo.fragment.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getFragmentManager().beginTransaction()
                .replace(R.id.settings_fragment_container, SettingsFragment.newInstance())
                .commit();
    }
}
