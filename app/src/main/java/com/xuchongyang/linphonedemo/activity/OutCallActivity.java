package com.xuchongyang.linphonedemo.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.xuchongyang.linphonedemo.R;
import com.xuchongyang.linphonedemo.fragment.FragmentHelper;
import com.xuchongyang.linphonedemo.fragment.TalkingFragment;
import com.xuchongyang.linphonedemo.fragment.VideoFragment;

/**
 * Created by Mark Xu on 17/3/14.
 * 去电 activity
 */
public class OutCallActivity extends AppCompatActivity {
    private static final String TAG = "OutCallActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_out_call);

        if (!getIntent().getBooleanExtra("video", false)) {
            Fragment voiceFragment = TalkingFragment.newInstance(getIntent().getStringExtra("callingNumber"));
            FragmentHelper.replaceFragment(this, R.id.out_call_fragment_container, voiceFragment);
        } else {
            Fragment videoFragment = VideoFragment.newInstance();
            FragmentHelper.replaceFragment(this, R.id.out_call_fragment_container, videoFragment);
        }
    }
}
