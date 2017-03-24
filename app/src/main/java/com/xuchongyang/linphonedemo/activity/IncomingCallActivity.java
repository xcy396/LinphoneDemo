package com.xuchongyang.linphonedemo.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.xuchongyang.linphonedemo.R;
import com.xuchongyang.linphonedemo.fragment.FragmentHelper;
import com.xuchongyang.linphonedemo.fragment.IncomingCallFragment;

/**
 * Created by Mark Xu on 17/3/14.
 * 来电 activity
 */

public class IncomingCallActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        String incomingNumber = getIntent().getStringExtra("incomingNumber");
        IncomingCallFragment fragment = IncomingCallFragment.newInstance(incomingNumber);
        FragmentHelper.replaceFragment(this, R.id.incoming_call_fragment_container, fragment);
    }
}
