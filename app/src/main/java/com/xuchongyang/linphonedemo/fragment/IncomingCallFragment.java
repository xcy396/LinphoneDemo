package com.xuchongyang.linphonedemo.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xuchongyang.linphonedemo.R;
import com.xuchongyang.linphonedemo.event.RefuseCallEvent;
import com.xuchongyang.linphonedemo.linphone.LinphoneManager;
import com.xuchongyang.linphonedemo.linphone.PhoneServiceCallback;
import com.xuchongyang.linphonedemo.linphone.PhoneVoiceUtils;
import com.xuchongyang.linphonedemo.service.LinphoneService;

import org.greenrobot.eventbus.EventBus;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCallParams;
import org.linphone.core.LinphoneCoreException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Mark Xu on 17/3/14.
 * 来电呼入界面
 */
public class IncomingCallFragment extends Fragment {
    private static final String TAG = "IncomingCallFragment";
    @BindView(R.id.calling_in_number)
    TextView mCallingInNumber;

    private static final String TALKING_NUMBER = "talkingNumber";

    private String mTalkingNumber;

    public IncomingCallFragment() {
        // Required empty public constructor
    }

    public static IncomingCallFragment newInstance(String param1) {
        IncomingCallFragment fragment = new IncomingCallFragment();
        Bundle args = new Bundle();
        args.putString(TALKING_NUMBER, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTalkingNumber = getArguments().getString(TALKING_NUMBER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_incoming_call, container, false);
        ButterKnife.bind(this, view);
        LinphoneService.addCallback(new PhoneServiceCallback() {
            @Override
            public void callReleased() {
                super.callReleased();
                getActivity().finish();
            }
        });
        PhoneVoiceUtils.getInstance().toggleSpeaker(true);
        mCallingInNumber.setText(mTalkingNumber);
        return view;
    }

    @OnClick(R.id.accept_call)
    public void acceptCall() {
        answer();
    }

    @OnClick(R.id.refuse_call)
    public void refuseCall() {
        EventBus.getDefault().post(new RefuseCallEvent());
        getActivity().finish();
    }

    private void answer() {
        final LinphoneCallParams remoteParams = LinphoneManager.getLc().getCurrentCall().getRemoteParams();
        try {
            LinphoneManager.getLc().acceptCall(LinphoneManager.getLc().getCurrentCall());
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
        }
        if (remoteParams != null && remoteParams.getVideoEnabled()) {
            Log.e(TAG, "answer: video true");
            switchVideo(true);
        } else {
            Log.e(TAG, "answer: video false");
            switchVideo(false);
        }
    }

    private void switchVideo(boolean displayVideo) {
        final LinphoneCall call = LinphoneManager.getLc().getCurrentCall();
        if (call == null) {
            return;
        }
        if (call.getState() == LinphoneCall.State.CallEnd || call.getState() == LinphoneCall.State.CallReleased) {
            return;
        }
        if (!displayVideo) {
            TalkingFragment fragment = TalkingFragment.newInstance(mTalkingNumber);
            FragmentHelper.replaceFragment(getActivity(), R.id.incoming_call_fragment_container, fragment);
        } else {
            VideoFragment fragment = VideoFragment.newInstance();
            FragmentHelper.replaceFragment(getActivity(), R.id.incoming_call_fragment_container, fragment);
        }
    }
}
