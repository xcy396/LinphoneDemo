package com.xuchongyang.linphonedemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.xuchongyang.linphonedemo.R;
import com.xuchongyang.linphonedemo.event.DialEvent;
import com.xuchongyang.linphonedemo.event.LinphoneServiceStartEvent;
import com.xuchongyang.linphonedemo.linphone.PhoneBean;
import com.xuchongyang.linphonedemo.linphone.PhoneServiceCallback;
import com.xuchongyang.linphonedemo.linphone.PhoneVoiceUtils;
import com.xuchongyang.linphonedemo.service.LinphoneService;
import com.xuchongyang.linphonedemo.utils.LinphoneUtils;
import com.xuchongyang.linphonedemo.utils.SharedPreferencesUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Mark Xu on 17/3/14.
 * sip 拨号
 */
public class DialActivity extends AppCompatActivity {
    private static final String TAG = "DialActivity";
    @BindView(R.id.dialing_number)
    EditText mDialingNumberEditText;
    @BindView(R.id.dial_back)
    ImageView mDialBackImage;
    @BindView(R.id.dial)
    ImageView mDialImage;
    @BindView(R.id.sip_status)
    ImageView mSipStatus;

    private ServiceWaitThread mServiceWaitThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sip_dial);
        ButterKnife.bind(this);
        if (LinphoneService.isReady()) {
            mSipStatus.setVisibility(View.VISIBLE);
            login();
        } else {
            mSipStatus.setVisibility(View.GONE);
            mServiceWaitThread = new ServiceWaitThread();
            mServiceWaitThread.start();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void dial(DialEvent event) {
        mDialingNumberEditText.append(event.getNumber());
        mDialBackImage.setVisibility(View.VISIBLE);
    }

    @Subscribe
    public void loginAfterServiceReady() {
        mSipStatus.setVisibility(View.VISIBLE);
        login();
    }

    private void login() {
        LinphoneService.addCallback(new PhoneServiceCallback() {
            @Override
            public void registrationState(LinphoneCore.RegistrationState registrationState) {
                if ("RegistrationOk".equals(registrationState.toString())) {
                    mSipStatus.setImageResource(R.drawable.online);
                } else {
                    mSipStatus.setImageResource(R.drawable.offline);
                }
            }
        });

        String sipAccount = (String) SharedPreferencesUtils.get(this, "sip_account", "");
        String sipPassword = (String) SharedPreferencesUtils.get(this, "sip_password", "");
        String sipServer = (String) SharedPreferencesUtils.get(this, "sip_server", "");
        try {
            PhoneVoiceUtils.getInstance().registerUserAuth(sipAccount, sipPassword, sipServer);
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
            Toast.makeText(this, "LinphoneCoreException: " + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private class ServiceWaitThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!LinphoneService.isReady()) {
                try {
                    sleep(80);
                } catch (InterruptedException e) {
                    throw new RuntimeException("waiting thread sleep() has been interrupted");
                }
            }
            EventBus.getDefault().post(new LinphoneServiceStartEvent());
            mServiceWaitThread = null;
        }
    }

    @OnClick(R.id.one)
    public void dialOne() {
        EventBus.getDefault().post(new DialEvent("1"));
    }

    @OnClick(R.id.two)
    public void dialTwo() {
        EventBus.getDefault().post(new DialEvent("2"));
    }

    @OnClick(R.id.three)
    public void dialThree() {
        EventBus.getDefault().post(new DialEvent("3"));
    }

    @OnClick(R.id.four)
    public void dialFour() {
        EventBus.getDefault().post(new DialEvent("4"));
    }

    @OnClick(R.id.five)
    public void dialFive() {
        EventBus.getDefault().post(new DialEvent("5"));
    }

    @OnClick(R.id.six)
    public void dialSix() {
        EventBus.getDefault().post(new DialEvent("6"));
    }

    @OnClick(R.id.seven)
    public void dialSeven() {
        EventBus.getDefault().post(new DialEvent("7"));
    }

    @OnClick(R.id.eight)
    public void dialEight() {
        EventBus.getDefault().post(new DialEvent("8"));
    }

    @OnClick(R.id.nine)
    public void dialNine() {
        EventBus.getDefault().post(new DialEvent("9"));
    }

    @OnClick(R.id.star)
    public void dialStar() {
        EventBus.getDefault().post(new DialEvent("*"));
    }

    @OnClick(R.id.zero)
    public void dialZero() {
        EventBus.getDefault().post(new DialEvent("0"));
    }

    @OnClick(R.id.sharp)
    public void dialSharp() {
        EventBus.getDefault().post(new DialEvent("#"));
    }

    @OnClick(R.id.dial)
    public void dial() {
        String dialingNumber = mDialingNumberEditText.getText().toString();
        if (!dialingNumber.equals("")) {
            PhoneBean phone = new PhoneBean();
            phone.setUserName(dialingNumber);
            phone.setHost(LinphoneUtils.getFreeSWITCHServer());
            PhoneVoiceUtils.getInstance().startSingleCallingTo(phone);

            Intent intent = new Intent(this, OutCallActivity.class);
            intent.putExtra("callingNumber", dialingNumber);
            intent.putExtra("video", false);
            startActivity(intent);
            mDialingNumberEditText.setText("");
            mDialBackImage.setVisibility(View.INVISIBLE);
        }
    }

    @OnClick(R.id.video_call)
    public void videoCall() {
        String dialingNumber = mDialingNumberEditText.getText().toString();
        if (!dialingNumber.equals("")) {
            PhoneBean phone = new PhoneBean();
            phone.setUserName(dialingNumber);
            phone.setHost(LinphoneUtils.getFreeSWITCHServer());

            PhoneVoiceUtils.getInstance().startVideoCall(phone);

            Intent intent = new Intent(this, OutCallActivity.class);
            intent.putExtra("callingNumber", dialingNumber);
            intent.putExtra("video", true);
            startActivity(intent);
            mDialingNumberEditText.setText("");
            mDialBackImage.setVisibility(View.INVISIBLE);
        }
    }

    @OnClick(R.id.dial_back)
    public void dialBack() {
        int index = mDialingNumberEditText.getSelectionStart();
        if (!mDialingNumberEditText.getText().toString().equals("")) {
            mDialingNumberEditText.getText().delete(index - 1, index);
        }
        if (index == 1) {
            mDialBackImage.setVisibility(View.INVISIBLE);
        }
    }

    @OnClick(R.id.sip_status)
    public void reLogin() {
        login();
    }
}
