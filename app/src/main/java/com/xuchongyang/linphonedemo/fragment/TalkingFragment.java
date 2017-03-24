package com.xuchongyang.linphonedemo.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xuchongyang.linphonedemo.R;
import com.xuchongyang.linphonedemo.linphone.PhoneServiceCallback;
import com.xuchongyang.linphonedemo.linphone.PhoneVoiceUtils;
import com.xuchongyang.linphonedemo.service.LinphoneService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * created by Mark Xu
 * 通话界面
 */
public class TalkingFragment extends Fragment {
    private static final String TAG = "TalkingFragment";
    private static final String TALKING_NUMBER = "talkingNumber";

    @BindView(R.id.calling_number)
    TextView mCallingNumberTextView;
    @BindView(R.id.silent_in_talking)
    ImageView mSilent;
    @BindView(R.id.video_button_in_talking)
    ImageView mVideo;
    @BindView(R.id.voice_out_in_talking)
    ImageView mVoiceOut;

    /**
     * 接收到的要呼叫的号码
     */
    private String mCallingNumber;

    public TalkingFragment() {

    }

    public static TalkingFragment newInstance(String talkingNumber) {
        TalkingFragment fragment = new TalkingFragment();
        Bundle args = new Bundle();
        args.putString(TALKING_NUMBER, talkingNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCallingNumber = getArguments().getString(TALKING_NUMBER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_talking, container, false);
        ButterKnife.bind(this, view);

        mSilent.setTag(false);
        mVoiceOut.setTag(false);
        mCallingNumberTextView.setText(mCallingNumber);
        LinphoneService.addCallback(new PhoneServiceCallback() {
            @Override
            public void callConnected() {
                PhoneVoiceUtils.getInstance().toggleSpeaker(false);
                PhoneVoiceUtils.getInstance().toggleMicro(false);
            }

            @Override
            public void callReleased() {
                Log.e(TAG, "callReleased: ");
                getActivity().finish();
            }
        });

        return view;
    }

    @OnClick(R.id.silent_in_talking)
    public void silent() {
        boolean isPress = (boolean) mSilent.getTag();
        if (isPress) {
            mSilent.setImageResource(R.drawable.silent);
            mSilent.setTag(false);
        } else {
            mSilent.setImageResource(R.drawable.silent_pressed);
            mSilent.setTag(true);
        }
        PhoneVoiceUtils.getInstance().toggleMicro((boolean)mSilent.getTag());
    }

    @OnClick(R.id.video_button_in_talking)
    public void video() {
        VideoFragment fragment = VideoFragment.newInstance();
        if (getActivity().getClass().getSimpleName().equals("OutCallActivity")) {
            FragmentHelper.replaceFragment(getActivity(), R.id.out_call_fragment_container, fragment);
        } else {
            FragmentHelper.replaceFragment(getActivity(), R.id.incoming_call_fragment_container, fragment);
        }
    }

    @OnClick(R.id.voice_out_in_talking)
    public void voiceOut() {
        boolean isPress = (boolean) mVoiceOut.getTag();
        if (isPress) {
            mVoiceOut.setImageResource(R.drawable.voice_out);
            mVoiceOut.setTag(false);
        } else {
            mVoiceOut.setImageResource(R.drawable.voice_out_pressed);
            mVoiceOut.setTag(true);
        }
        PhoneVoiceUtils.getInstance().toggleSpeaker((boolean)mVoiceOut.getTag());
    }

    @OnClick(R.id.voice_hang_image)
    public void hang() {
        PhoneVoiceUtils.getInstance().hangUp();
        getActivity().finish();
    }
}
