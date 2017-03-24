package com.xuchongyang.linphonedemo.linphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.xuchongyang.linphonedemo.service.LinphoneService;


/**
 * Created by Mark Xu on 17/3/13.
 * KeepAliveReceiver
 */

public class KeepAliveReceiver extends BroadcastReceiver {
    private static final String TAG = "KeepAliveReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!LinphoneService.isReady()) {
            Log.e(TAG, "Keep alive broadcast received while Linphone service not ready");
            return;
        } else {
            Log.e(TAG, "KeepAliveReceiver");
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_SCREEN_ON)) {
                Log.e(TAG, "KeepAliveReceiver screen on");
                LinphoneManager.getLc().enableKeepAlive(true);
            } else if (intent.getAction().equalsIgnoreCase(Intent.ACTION_SCREEN_OFF)) {
                Log.e(TAG, "KeepAliveReceiver screen off");
                LinphoneManager.getLc().enableKeepAlive(false);
            }
        }
    }
}
