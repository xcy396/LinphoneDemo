package com.xuchongyang.linphonedemo.linphone;

import android.util.Log;

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCallParams;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneProxyConfig;

/**
 * Created by Mark Xu on 17/3/13.
 * 语音通话工具类
 */

public class PhoneVoiceUtils {
    private static final String TAG = "PhoneVoiceUtils";
    private static volatile PhoneVoiceUtils sPhoneVoiceUtils;
    private LinphoneCore mLinphoneCore = null;

    public static PhoneVoiceUtils getInstance() {
        if (sPhoneVoiceUtils == null) {
            synchronized (PhoneVoiceUtils.class) {
                if (sPhoneVoiceUtils == null) {
                    sPhoneVoiceUtils = new PhoneVoiceUtils();
                }
            }
        }
        return sPhoneVoiceUtils;
    }

    private PhoneVoiceUtils() {
        mLinphoneCore = LinphoneManager.getLc();
        mLinphoneCore.enableEchoCancellation(true);
        mLinphoneCore.enableEchoLimiter(true);
    }

    /**
     * 注册到服务器
     * @param name
     * @param password
     * @param host
     * @throws LinphoneCoreException
     */
    public void registerUserAuth(String name, String password, String host) throws LinphoneCoreException {
        Log.e(TAG, "registerUserAuth name = " + name);
        Log.e(TAG, "registerUserAuth pw = " + password);
        Log.e(TAG, "registerUserAuth host = " + host);
        String identify = "sip:" + name + "@" + host;
        String proxy = "sip:" + host;
        LinphoneAddress proxyAddr = LinphoneCoreFactory.instance().createLinphoneAddress(proxy);
        LinphoneAddress identifyAddr = LinphoneCoreFactory.instance().createLinphoneAddress(identify);
        LinphoneAuthInfo authInfo = LinphoneCoreFactory.instance().createAuthInfo(name, null, password,
                null, null, host);
        LinphoneProxyConfig prxCfg = mLinphoneCore.createProxyConfig(identifyAddr.asString(),
                proxyAddr.asStringUriOnly(), proxyAddr.asStringUriOnly(), true);
        prxCfg.enableAvpf(false);
        prxCfg.setAvpfRRInterval(0);
        prxCfg.enableQualityReporting(false);
        prxCfg.setQualityReportingCollector(null);
        prxCfg.setQualityReportingInterval(0);
        prxCfg.enableRegister(true);
        mLinphoneCore.addProxyConfig(prxCfg);
        mLinphoneCore.addAuthInfo(authInfo);
        mLinphoneCore.setDefaultProxyConfig(prxCfg);
    }

    public LinphoneCall startSingleCallingTo(PhoneBean bean) {
        LinphoneAddress address;
        LinphoneCall call = null;
        try {
            address = mLinphoneCore.interpretUrl(bean.getUserName() + "@" + bean.getHost());
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
            Log.e("startSingleCallingTo", " LinphoneCoreException0:" + e.toString());
            return null;
        }
//        address.setDisplayName(bean.getDisplayName());
        LinphoneCallParams params = mLinphoneCore.createCallParams(null);
        params.setVideoEnabled(false);
        try {
            call = mLinphoneCore.inviteAddressWithParams(address, params);
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
            Log.e("startSingleCallingTo", " LinphoneCoreException1:" + e.toString());
        }
        return call;
    }

    public LinphoneCall startVideoCall(PhoneBean bean) {
        LinphoneAddress address;
        LinphoneCall call = null;
        try {
            address = mLinphoneCore.interpretUrl(bean.getUserName() + "@" + bean.getHost());
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
            Log.e("startVideoCall ", " LinphoneCoreException0:" + e.toString());
            return null;
        }
//        address.setDisplayName(bean.getDisplayName());
        LinphoneCallParams params = mLinphoneCore.createCallParams(null);
        params.setVideoEnabled(true);
        params.enableLowBandwidth(false);
        try {
            call = mLinphoneCore.inviteAddressWithParams(address, params);
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
            Log.e("startVideoCall ", " LinphoneCoreException1:" + e.toString());
        }
        return call;
    }

    /**
     * 挂断电话
     */
    public void hangUp() {
        LinphoneCall currentCall = mLinphoneCore.getCurrentCall();
        if (currentCall != null) {
            mLinphoneCore.terminateCall(currentCall);
        } else if (mLinphoneCore.isInConference()) {
            mLinphoneCore.terminateConference();
        } else {
            mLinphoneCore.terminateAllCalls();
        }
    }

    /**
     * 是否静音
     * @param isMicMuted
     */
    public void toggleMicro(boolean isMicMuted) {
        mLinphoneCore.muteMic(isMicMuted);
    }

    /**
     * 是否外放
     * @param isSpeakerEnabled
     */
     public void toggleSpeaker(boolean isSpeakerEnabled) {
         mLinphoneCore.enableSpeaker(isSpeakerEnabled);
     }
}
