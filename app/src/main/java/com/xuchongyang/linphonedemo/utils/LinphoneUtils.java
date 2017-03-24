package com.xuchongyang.linphonedemo.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.xuchongyang.linphonedemo.linphone.LinphoneManager;

import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LpConfig;
import org.linphone.mediastream.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Mark Xu on 17/3/11.
 * Linphone 工具类
 */

public class LinphoneUtils {
    public static void copyIfNotExist(Context context, int resourceId, String target) throws IOException {
        File fileToCopy = new File(target);
        if (!fileToCopy.exists()) {
            copyFromPackage(context, resourceId, fileToCopy.getName());
        }
    }

    public static void copyFromPackage(Context context, int resourceId, String target) throws IOException {
        FileOutputStream outputStream = context.openFileOutput(target, 0);
        InputStream inputStream = context.getResources().openRawResource(resourceId);
        int readByte;
        byte[] buff = new byte[8048];
        while ((readByte = inputStream.read(buff)) != -1) {
            outputStream.write(buff, 0, readByte);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }

    public static boolean isCallRunning(LinphoneCall call) {
        if (call == null) {
            return false;
        }
        LinphoneCall.State state = call.getState();
        return state == LinphoneCall.State.Connected
                || state == LinphoneCall.State.CallUpdating
                || state == LinphoneCall.State.CallUpdatedByRemote
                || state == LinphoneCall.State.StreamsRunning
                || state == LinphoneCall.State.Resuming;
    }

    public static boolean isCallEstablished(LinphoneCall call) {
        if (call == null) {
            return false;
        }
        LinphoneCall.State state = call.getState();
        return isCallRunning(call)
                || state == LinphoneCall.State.Paused
                || state == LinphoneCall.State.PausedByRemote
                || state == LinphoneCall.State.Pausing;
    }

    public static List<LinphoneCall> getLinphoneCalls(LinphoneCore lc) {
        return new ArrayList<>(Arrays.asList(lc.getCalls()));
    }

    public static boolean isHighBandwidthConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isConnected() && isConnectionFast(info.getType(), info.getSubtype()));
    }

    private static boolean isConnectionFast(int type, int subType) {
        if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                            return false;
            }
        }
        return true;
    }

    public static LpConfig getConfig(Context context) {
        LinphoneCore lc = getLc();
        if (lc != null) {
            return lc.getConfig();
        }

        if (LinphoneManager.isInstanceiated()) {
            Log.w("LinphoneManager not instanciated yet...");
            return LinphoneCoreFactory.instance().createLpConfig(context.getFilesDir().getAbsolutePath() + "/.linphonerc");
        }

        return LinphoneCoreFactory.instance().createLpConfig(LinphoneManager.getInstance().mLinphoneConfigFile);
    }

    private static LinphoneCore getLc() {
        if (!LinphoneManager.isInstanceiated()) {
            return null;
        }
        return LinphoneManager.getLcIfManagerNotDestroyOrNull();
    }

    public static String getFreeSWITCHServer() {
        return "192.168.9.101";
    }
}
