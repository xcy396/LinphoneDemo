package com.xuchongyang.linphonedemo.linphone;

import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCore;

/**
 * Created by Mark Xu on 17/3/13.
 * 通话状态回调
 */

public abstract class PhoneServiceCallback {
    /**
     * 注册状态
     * @param registrationState
     */
    public void registrationState(LinphoneCore.RegistrationState registrationState) {}

    /**
     * 来电状态
     * @param linphoneCall
     */
    public void incomingCall(LinphoneCall linphoneCall) {}

    /**
     * 电话接通
     */
    public void callConnected() {}

    /**
     * 电话被挂断
     */
    public void callReleased() {}
}
