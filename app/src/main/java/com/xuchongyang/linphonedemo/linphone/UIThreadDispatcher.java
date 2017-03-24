package com.xuchongyang.linphonedemo.linphone;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by Mark Xu on 17/3/13.
 * UIThread 分发任务
 */

public class UIThreadDispatcher {
    private static Handler sHandler = new Handler(Looper.getMainLooper());
    public static void dispatch(Runnable runnable) {
        sHandler.post(runnable);
    }
}
