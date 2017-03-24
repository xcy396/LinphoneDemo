package com.xuchongyang.linphonedemo.linphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


/**
 * Created by Mark Xu on 17/3/13.
 * KeepAliveHandler
 */

public class KeepAliveHandler extends BroadcastReceiver {
    private static final String TAG = "KeepAliveHandler";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "Keep alive handler invoked");
        if (LinphoneManager.getLcIfManagerNotDestroyOrNull() != null) {
            LinphoneManager.getLc().refreshRegisters();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Log.e(TAG, "Cannot sleep for 2s", e);
            }
        }
    }
}
