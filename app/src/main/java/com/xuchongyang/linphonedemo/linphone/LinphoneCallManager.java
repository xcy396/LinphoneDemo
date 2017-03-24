package com.xuchongyang.linphonedemo.linphone;

import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCallParams;
import org.linphone.core.LinphoneCore;
import org.linphone.mediastream.Log;

/**
 * Created by Mark Xu on 17/3/17.
 */

public class LinphoneCallManager {
    private static volatile LinphoneCallManager sCallManager;

    public static LinphoneCallManager getInstance() {
        if (sCallManager == null) {
            synchronized (LinphoneCallManager.class) {
                if (sCallManager == null) {
                    sCallManager = new LinphoneCallManager();
                }
            }
        }
        return sCallManager;
    }

    private BandwidthManager bm() {
        return BandwidthManager.getInstance();
    }

    public void updateCall() {
        LinphoneCore lc = LinphoneManager.getLc();
        LinphoneCall lCall = lc.getCurrentCall();
        if (lCall == null) {
            Log.e("Trying to updateCall while not in call: doing nothing");
            return;
        }
        LinphoneCallParams params = lCall.getCurrentParamsCopy();
        bm().updateWithProfileSettings(lc, params);
        lc.updateCall(lCall, null);
    }
}