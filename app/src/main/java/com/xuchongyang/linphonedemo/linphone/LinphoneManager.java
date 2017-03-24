package com.xuchongyang.linphonedemo.linphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;

import com.xuchongyang.linphonedemo.R;
import com.xuchongyang.linphonedemo.utils.LinphoneUtils;
import com.xuchongyang.linphonedemo.utils.SharedPreferencesUtils;

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCallStats;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatRoom;
import org.linphone.core.LinphoneContent;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneCoreListener;
import org.linphone.core.LinphoneEvent;
import org.linphone.core.LinphoneFriend;
import org.linphone.core.LinphoneFriendList;
import org.linphone.core.LinphoneInfoMessage;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.core.PayloadType;
import org.linphone.core.PublishState;
import org.linphone.core.SubscriptionState;
import org.linphone.core.ToneID;
import org.linphone.mediastream.Log;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Mark Xu on 17/3/11.
 * Linphone 管理器
 */

public class LinphoneManager implements LinphoneCoreListener {
    private static final String TAG = "LinphoneManager";
    private static LinphoneManager instance;
    private Context mServiceContext;
    private LinphoneCore mLc;
    private Timer mTimer;
    private LinphoneCall mCall;
    private static boolean sExited;
    private Resources mR;

    private String mLPConfigXsd = null;
    private String mLinphoneFactoryConfigFile = null;
    public String mLinphoneConfigFile = null;
    private String mLinphoneRootCaFile = null;
    private String mRingSoundFile = null;
    private String mRingBackSoundFile = null;
    private String mPauseSoundFile = null;
    private String mChatDatabaseFile = null;
//    private String mErrorToneFile = null;

    private BroadcastReceiver mKeepAliveReceiver = new KeepAliveReceiver();

    public LinphoneManager(Context serviceContext) {
        mServiceContext = serviceContext;
        LinphoneCoreFactory.instance().setDebugMode(true, "huanyutong");
        sExited = false;

        String basePath = mServiceContext.getFilesDir().getAbsolutePath();
        mLPConfigXsd = basePath + "/lpconfig.xsd";
        mLinphoneFactoryConfigFile = basePath + "/linphonerc";
        mLinphoneConfigFile = basePath + "/.linphonerc";
        mLinphoneRootCaFile = basePath + "/rootca.pem";
        mRingSoundFile = basePath + "/oldphone_mono.wav";
        mRingBackSoundFile = basePath + "/ringback.wav";
        mPauseSoundFile = basePath + "/toy_mono.wav";
        mChatDatabaseFile = basePath + "/linphone-history.db";
//        mErrorToneFile = basePath + "/error.wav";

        mR = serviceContext.getResources();
    }

    public synchronized static final LinphoneManager createAndStart(Context context) {
        if (instance != null) {
            throw new RuntimeException("Linphone Manager is already initialized");
        }
        instance = new LinphoneManager(context);
        instance.startLibLinphone(context);
        return instance;
    }

    public static synchronized LinphoneCore getLcIfManagerNotDestroyOrNull() {
        if (sExited || instance == null) {
            Log.e("Trying to get linphone core while LinphoneManager already destroyed or not created");
            return null;
        }
        return getLc();
    }

    public static final boolean isInstanceiated() {
        return instance != null;
    }

    public static synchronized final LinphoneCore getLc() {
        return getInstance().mLc;
    }

    public static synchronized final LinphoneManager getInstance() {
        if (instance != null) {
            return instance;
        }
        if (sExited) {
            throw new RuntimeException("Linphone Manager was already destroyed. "
                    + "Better use getLcIfManagerNotDestroyed and check returned value");
        }
        throw new RuntimeException("Linphone Manager should be created before accessed");
    }

    private synchronized void startLibLinphone(Context context) {
        try {
            copyAssetsFromPackage();
            mLc = LinphoneCoreFactory.instance().createLinphoneCore(this, mLinphoneConfigFile,
                    mLinphoneFactoryConfigFile, null, context);
            mLc.addListener((LinphoneCoreListener)context);

            try {
                initLibLinphone();
            } catch (LinphoneCoreException e) {
                Log.e(e);
            }

            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    UIThreadDispatcher.dispatch(new Runnable() {
                        @Override
                        public void run() {
                            if (mLc != null) {
                                mLc.iterate();
                            }
                        }
                    });
                }
            };
            mTimer = new Timer("Linphone Scheduler");
            mTimer.schedule(task, 0, 20);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "startLibLinphone: cannot start linphone");
        }
    }

    private synchronized void initLibLinphone() throws LinphoneCoreException {
        mLc.setContext(mServiceContext);
        setUserAgent();
        mLc.setRemoteRingbackTone(mRingSoundFile);
        mLc.setTone(ToneID.CallWaiting, mRingSoundFile);
        mLc.setRing(mRingSoundFile);
        mLc.setRootCA(mLinphoneRootCaFile);
        mLc.setPlayFile(mPauseSoundFile);
        mLc.setChatDatabasePath(mChatDatabaseFile);
//        mLc.setCallErrorTone(Reason.NotFound, mErrorToneFile);//设置呼叫错误播放的铃声

        int availableCores = Runtime.getRuntime().availableProcessors();
        Log.w(TAG, "MediaStreamer : " + availableCores + " cores detected and configured");
        mLc.setCpuCount(availableCores);

        int migrationResult = getLc().migrateToMultiTransport();
        Log.d(TAG, "Migration to multi transport result = " + migrationResult);

        mLc.setNetworkReachable(true);

        //回声消除
        boolean isEchoCancellation = (boolean) SharedPreferencesUtils.get(mServiceContext, "echo_cancellation", true);
        mLc.enableEchoCancellation(isEchoCancellation);

        //自适应码率控制
        boolean isAdaptiveRateControl = (boolean) SharedPreferencesUtils.get(mServiceContext, "adaptive_rate_control", true);
        mLc.enableAdaptiveRateControl(isAdaptiveRateControl);

        //audio 码率设置
        LinphoneUtils.getConfig(mServiceContext).setInt("audio", "codec_bitrate_limit", 128);

        mLc.setPreferredVideoSizeByName("720p");
        mLc.setUploadBandwidth(1536);
        mLc.setDownloadBandwidth(1536);

        mLc.setVideoPolicy(mLc.getVideoAutoInitiatePolicy(), true);
        mLc.setVideoPolicy(true, mLc.getVideoAutoAcceptPolicy());
        mLc.enableVideo(true, true);

        setCodecMime();

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mServiceContext.registerReceiver(mKeepAliveReceiver, filter);
    }

    public void newOutgoingCall(String to, String displayName) {

    }

    private void setCodecMime()
    {
        for (final PayloadType pt : mLc.getVideoCodecs())
        {
            android.util.Log.i(TAG, "setCodecMime = " + pt.getMime());
            if (!pt.getMime().equals("VP8"))
            {
                try
                {
                    android.util.Log.i(TAG, "disable codec " + pt.getMime());
                    mLc.enablePayloadType(pt, false);
                }
                catch (LinphoneCoreException e)
                {
                    Log.e(e);
                }
            }
        }
    }

    private void copyAssetsFromPackage() throws IOException {
        LinphoneUtils.copyIfNotExist(mServiceContext, R.raw.oldphone_mono, mRingSoundFile);
        LinphoneUtils.copyIfNotExist(mServiceContext, R.raw.ringback, mRingBackSoundFile);
        LinphoneUtils.copyIfNotExist(mServiceContext, R.raw.toy_mono, mPauseSoundFile);
        LinphoneUtils.copyIfNotExist(mServiceContext, R.raw.linphonerc_default, mLinphoneConfigFile);
        LinphoneUtils.copyIfNotExist(mServiceContext, R.raw.linphonerc_factory, new File(mLinphoneFactoryConfigFile).getName());
        LinphoneUtils.copyIfNotExist(mServiceContext, R.raw.lpconfig, mLPConfigXsd);
        LinphoneUtils.copyIfNotExist(mServiceContext, R.raw.rootca, mLinphoneRootCaFile);
    }

    private void setUserAgent() {
        try {
            String versionName = mServiceContext.getPackageManager().getPackageInfo(mServiceContext.getPackageName(),
                    0).versionName;
            if (versionName == null) {
                versionName = String.valueOf(mServiceContext.getPackageManager().getPackageInfo(mServiceContext.getPackageName(), 0).versionCode);
            }
            mLc.setUserAgent("Hunayutong", versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void destroy() {
        if (instance == null) {
            return;
        }
        sExited = true;
        instance.doDestroy();
    }

    private void doDestroy() {
        try {
            mTimer.cancel();
            mLc.destroy();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            mLc = null;
            instance = null;
        }
    }

    @Override
    public void authInfoRequested(LinphoneCore linphoneCore, String s, String s1, String s2) {

    }

    @Override
    public void authenticationRequested(LinphoneCore linphoneCore, LinphoneAuthInfo linphoneAuthInfo, LinphoneCore.AuthMethod authMethod) {

    }

    @Override
    public void callStatsUpdated(LinphoneCore linphoneCore, LinphoneCall linphoneCall, LinphoneCallStats linphoneCallStats) {

    }

    @Override
    public void newSubscriptionRequest(LinphoneCore linphoneCore, LinphoneFriend linphoneFriend, String s) {

    }

    @Override
    public void notifyPresenceReceived(LinphoneCore linphoneCore, LinphoneFriend linphoneFriend) {

    }

    @Override
    public void dtmfReceived(LinphoneCore linphoneCore, LinphoneCall linphoneCall, int i) {

    }

    @Override
    public void notifyReceived(LinphoneCore linphoneCore, LinphoneCall linphoneCall, LinphoneAddress linphoneAddress, byte[] bytes) {

    }

    @Override
    public void transferState(LinphoneCore linphoneCore, LinphoneCall linphoneCall, LinphoneCall.State state) {

    }

    @Override
    public void infoReceived(LinphoneCore linphoneCore, LinphoneCall linphoneCall, LinphoneInfoMessage linphoneInfoMessage) {

    }

    @Override
    public void subscriptionStateChanged(LinphoneCore linphoneCore, LinphoneEvent linphoneEvent, SubscriptionState subscriptionState) {

    }

    @Override
    public void publishStateChanged(LinphoneCore linphoneCore, LinphoneEvent linphoneEvent, PublishState publishState) {

    }

    @Override
    public void show(LinphoneCore linphoneCore) {

    }

    @Override
    public void displayStatus(LinphoneCore linphoneCore, String s) {

    }

    @Override
    public void displayMessage(LinphoneCore linphoneCore, String s) {

    }

    @Override
    public void displayWarning(LinphoneCore linphoneCore, String s) {

    }

    @Override
    public void fileTransferProgressIndication(LinphoneCore linphoneCore, LinphoneChatMessage linphoneChatMessage, LinphoneContent linphoneContent, int i) {

    }

    @Override
    public void fileTransferRecv(LinphoneCore linphoneCore, LinphoneChatMessage linphoneChatMessage, LinphoneContent linphoneContent, byte[] bytes, int i) {

    }

    @Override
    public int fileTransferSend(LinphoneCore linphoneCore, LinphoneChatMessage linphoneChatMessage, LinphoneContent linphoneContent, ByteBuffer byteBuffer, int i) {
        return 0;
    }

    @Override
    public void globalState(LinphoneCore linphoneCore, LinphoneCore.GlobalState globalState, String s) {

    }

    @Override
    public void registrationState(LinphoneCore linphoneCore, LinphoneProxyConfig linphoneProxyConfig, LinphoneCore.RegistrationState registrationState, String s) {

    }

    @Override
    public void configuringStatus(LinphoneCore linphoneCore, LinphoneCore.RemoteProvisioningState remoteProvisioningState, String s) {

    }

    @Override
    public void messageReceived(LinphoneCore linphoneCore, LinphoneChatRoom linphoneChatRoom, LinphoneChatMessage linphoneChatMessage) {

    }

    @Override
    public void callState(LinphoneCore linphoneCore, LinphoneCall linphoneCall, LinphoneCall.State state, String s) {

    }

    @Override
    public void callEncryptionChanged(LinphoneCore linphoneCore, LinphoneCall linphoneCall, boolean b, String s) {

    }

    @Override
    public void notifyReceived(LinphoneCore linphoneCore, LinphoneEvent linphoneEvent, String s, LinphoneContent linphoneContent) {

    }

    @Override
    public void isComposingReceived(LinphoneCore linphoneCore, LinphoneChatRoom linphoneChatRoom) {

    }

    @Override
    public void ecCalibrationStatus(LinphoneCore linphoneCore, LinphoneCore.EcCalibratorStatus ecCalibratorStatus, int i, Object o) {

    }

    @Override
    public void uploadProgressIndication(LinphoneCore linphoneCore, int i, int i1) {

    }

    @Override
    public void uploadStateChanged(LinphoneCore linphoneCore, LinphoneCore.LogCollectionUploadState logCollectionUploadState, String s) {

    }

    @Override
    public void friendListCreated(LinphoneCore linphoneCore, LinphoneFriendList linphoneFriendList) {

    }

    @Override
    public void friendListRemoved(LinphoneCore linphoneCore, LinphoneFriendList linphoneFriendList) {

    }



//    private void setFrontCamAsDefault() {
//        int camId = 0;
//        AndroidCameraConfiguration.AndroidCamera[] cameras = AndroidCameraConfiguration.retrieveCameras();
//        for (AndroidCameraConfiguration.AndroidCamera androidCamera :
//                cameras) {
//            if (androidCamera.frontFacing) {
//                camId = androidCamera.id;
//            }
//        }
//        mLc.setVideoDevice(camId);
//    }

//    public void invate(String url, String mOtherName, String mDomain) {
//        LinphoneAddress la = LinphoneCoreFactory.instance().createLinphoneAddress(mOtherName, mDomain,
//                mOtherName + "@" + mDomain);
//        Log.i(TAG, "linphoneaddress is " + la);
//        la.setTransport(LinphoneAddress.TransportType.LinphoneTransportTcp);
//        try {
//            mLc.invite(la);
//        } catch (LinphoneCoreException e) {
//            e.printStackTrace();
//        }
//    }

//    public boolean acceptCall(LinphoneCall call) {
//        try {
//            mLc.acceptCall(call);
//            return true;
//        } catch (LinphoneCoreException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//
//    public boolean acceptCallWithParams(LinphoneCall call, LinphoneCallParams params) {
//        try {
//            mLc.acceptCallWithParams(call, params);
//            return true;
//        } catch (LinphoneCoreException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

//    public void updateNetworkReachability() {
//        ConnectivityManager cm = (ConnectivityManager) mServiceContext.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo eventInfo = cm.getActiveNetworkInfo();
//
//        if (eventInfo == null || eventInfo.getState() == NetworkInfo.State.DISCONNECTED) {
//            Log.i("No connectivity: setting network unreachable");
//            mLc.setNetworkReachable(false);
//        } else if (eventInfo.getState() == NetworkInfo.State.CONNECTED){
//            manageTunnelServer(eventInfo);
//
//            boolean wifiOnly = LinphonePreferences.instance().isWifiOnlyEnabled();
//            if (wifiOnly){
//                if (eventInfo.getType()==ConnectivityManager.TYPE_WIFI)
//                    mLc.setNetworkReachable(true);
//                else {
//                    Log.i("Wifi-only mode, setting network not reachable");
//                    mLc.setNetworkReachable(false);
//                }
//            }else{
//                int curtype=eventInfo.getType();
//
//                if (curtype!=mLastNetworkType){
//                    //if kind of network has changed, we need to notify network_reachable(false) to make sure all current connections are destroyed.
//                    //they will be re-created during setNetworkReachable(true).
//                    Log.i("Connectivity has changed.");
//                    mLc.setNetworkReachable(false);
//                }
//                mLc.setNetworkReachable(true);
//                mLastNetworkType=curtype;
//            }
//        }
//    }
//
//    private void manageTunnelServer(NetworkInfo info) {
//        if (mLc == null) return;
//        if (!mLc.isTunnelAvailable()) return;
//
//        Log.i("Managing tunnel");
//        if (isTunnelNeeded(info)) {
//            Log.i("Tunnel need to be activated");
//            mLc.tunnelSetMode(LinphoneCore.TunnelMode.enable);
//        } else {
//            Log.i("Tunnel should not be used");
//            String pref = mPrefs.getTunnelMode();
//            mLc.tunnelSetMode(LinphoneCore.TunnelMode.disable);
//            if (getString(R.string.tunnel_mode_entry_value_auto).equals(pref)) {
//                mLc.tunnelSetMode(LinphoneCore.TunnelMode.auto);
//            }
//        }
//    }

}
