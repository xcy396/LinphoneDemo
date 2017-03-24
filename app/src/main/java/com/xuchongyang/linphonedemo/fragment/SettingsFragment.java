package com.xuchongyang.linphonedemo.fragment;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

import com.xuchongyang.linphonedemo.R;
import com.xuchongyang.linphonedemo.linphone.LinphoneManager;
import com.xuchongyang.linphonedemo.utils.LinphoneUtils;

import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.PayloadType;
import org.linphone.mediastream.Log;
import org.linphone.mediastream.Version;


/**
 * Created by Mark Xu on 2017/03/18
 * SettingsFragment
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "SettingsFragment";
    private static final String SIP_ACCOUNT = "sip_account";
    private static final String SIP_PASSWORD = "sip_password";
    private static final String SIP_SERVER = "sip_server";
    private static final String VIDEO_SIZE = "video_size";
    private static final String VIDEO_CODECS = "video_codecs";
    private static final String ECHO_CANCELLATION = "echo_cancellation";
    private static final String ADAPTIVE_RATE_CONTROL = "adaptive_rate_control";
    private static final String BITRATE_LIMIT = "bitrate_limit";
    private static final String AUDIO_CODECS = "audio_codecs";

    public SettingsFragment() {}

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        initSipAccount();
        initAudioSetting();
        initVideoSetting();
        initAudioCodecsSetting();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference connectionPref = findPreference(key);
        if (key.equals(SIP_ACCOUNT)) {
            String sipAccount = sharedPreferences.getString(key, "");
            connectionPref.setSummary(sipAccount);
        }
        if (key.equals(SIP_PASSWORD)) {
            String password = sharedPreferences.getString(key, "");
            connectionPref.setSummary(password);
        }
        if (key.equals(SIP_SERVER)) {
            String server = sharedPreferences.getString(key, "");
            connectionPref.setSummary(server);
        }
    }

    private LinphoneCore getLc() {
        if (!LinphoneManager.isInstanceiated()) {
            return null;
        }
        return LinphoneManager.getLcIfManagerNotDestroyOrNull();
    }

    private void initSipAccount() {
        EditTextPreference sipAccount = (EditTextPreference) findPreference(SIP_ACCOUNT);
        sipAccount.setSummary(sipAccount.getSharedPreferences().getString(SIP_ACCOUNT, ""));
        if (sipAccount.getSummary().equals("")) {
            sipAccount.setSummary("请输入sip 账号");
        }

        EditTextPreference sipPassword = (EditTextPreference) findPreference(SIP_PASSWORD);
        sipPassword.setSummary(sipPassword.getSharedPreferences().getString(SIP_PASSWORD, ""));
        if (sipPassword.getSummary().equals("")) {
            sipPassword.setSummary("请输入密码");
        }

        EditTextPreference sipServer = (EditTextPreference) findPreference(SIP_SERVER);
        sipServer.setSummary(sipServer.getSharedPreferences().getString(SIP_SERVER, ""));
        if (sipServer.getSummary().equals("")) {
            sipServer.setSummary("请输入服务器 IP 地址");
        }
    }

    private void initVideoSetting() {
        ListPreference videoSize = (ListPreference) findPreference(VIDEO_SIZE);
        if (videoSize.getValue() == null) {
            videoSize.setValueIndex(0);
        }
        videoSize.setSummary(videoSize.getSharedPreferences().getString(VIDEO_SIZE, ""));
        videoSize.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                setListPreferenceSummary(preference, newValue);
                switch ((String)newValue) {
                    case "720p":
                        getLc().setPreferredVideoSizeByName("720p");
                        setBandwidthLimint(1536);
                        break;
                    case "VGA":
                        getLc().setPreferredVideoSizeByName("vga");
                        setBandwidthLimint(660);
                        break;
                    case "CIF":
                        getLc().setPreferredVideoSizeByName("cif");
                        setBandwidthLimint(512);
                        break;
                    case "QVGA":
                        getLc().setPreferredVideoSizeByName("qvga");
                        setBandwidthLimint(380);
                        break;
                    case "QCIF":
                        getLc().setPreferredVideoSizeByName("qcif");
                        setBandwidthLimint(256);
                        break;
                    default:
                        setBandwidthLimint(512);
                        break;
                }
                return true;
            }
        });

        PreferenceCategory codecs = (PreferenceCategory) findPreference(VIDEO_CODECS);
        codecs.removeAll();

        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyOrNull();
        for (final PayloadType pt : lc.getVideoCodecs()) {
            CheckBoxPreference codec = new CheckBoxPreference(getActivity());
            codec.setTitle(pt.getMime());

            if (!pt.getMime().equals("VP8")) {
                if (getResources().getBoolean(R.bool.disable_all_patented_codecs_for_markets)) {
                    continue;
                } else {
                    if (!Version.hasFastCpuWithAsmOptim() && pt.getMime().equals("H264")) {
                        Log.e("CPU does not have asm optimisations available, disabling H264");
                        continue;
                    }
                }
            }
            codec.setChecked(lc.isPayloadTypeEnabled(pt));

            codec.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean enable = (Boolean) newValue;
                    try {
                        LinphoneManager.getLcIfManagerNotDestroyOrNull().enablePayloadType(pt, enable);
                    } catch (LinphoneCoreException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            });
            codecs.addPreference(codec);
        }
    }

    private void initAudioSetting() {
        ListPreference bitrate_limit = (ListPreference) findPreference(BITRATE_LIMIT);
        android.util.Log.e(TAG, "initAudioSetting: bitrate value is " + bitrate_limit.getValue());
        if (bitrate_limit.getValue() == null) {
            bitrate_limit.setValueIndex(5);
        }
        bitrate_limit.setSummary(bitrate_limit.getSharedPreferences().getString(BITRATE_LIMIT, "") + " kbits/s");
        bitrate_limit.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                setListPreferenceSummary(preference, newValue);
                int bitrate = Integer.valueOf(String.valueOf(newValue));
                LinphoneUtils.getConfig(getActivity()).setInt("audio", "codec_bitrate_limit", bitrate);
                return true;
            }
        });

        SwitchPreference echoCancellation = (SwitchPreference) findPreference(ECHO_CANCELLATION);
        echoCancellation.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean checked = Boolean.valueOf(newValue.toString());
                if (checked) {
                    getLc().enableEchoCancellation(true);
                } else {
                    getLc().enableEchoCancellation(false);
                }
                return true;
            }
        });

        SwitchPreference adaptiveRateControl = (SwitchPreference) findPreference(ADAPTIVE_RATE_CONTROL);
        adaptiveRateControl.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean checked = Boolean.valueOf(newValue.toString());
                if (checked) {
                    getLc().enableAdaptiveRateControl(true);
                } else {
                    getLc().enableAdaptiveRateControl(false);
                }
                return true;
            }
        });
    }

    private void initAudioCodecsSetting() {
        PreferenceCategory codecs = (PreferenceCategory) findPreference(AUDIO_CODECS);
        codecs.removeAll();

        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyOrNull();
        for (final PayloadType pt : lc.getAudioCodecs()) {
            CheckBoxPreference codec = new CheckBoxPreference(getActivity());
            codec.setTitle(pt.getMime());

            if (pt.getMime().equals("mpeg4-generic")) {
                if (Build.VERSION.SDK_INT < 16) {
                    try {
                        lc.enablePayloadType(pt, false);
                    } catch (LinphoneCoreException e) {
                        e.printStackTrace();
                    }
                    continue;
                } else {
                    codec.setTitle("AAC-ELD");
                }
            }

            codec.setSummary(pt.getRate() + " Hz");
            codec.setChecked(lc.isPayloadTypeEnabled(pt));

            codec.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean enable = (Boolean) newValue;
                    try {
                        LinphoneManager.getLcIfManagerNotDestroyOrNull().enablePayloadType(pt, enable);
                    } catch (LinphoneCoreException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            });

            codecs.addPreference(codec);
        }
    }

    private void setBandwidthLimint(int bandwidth) {
        getLc().setUploadBandwidth(bandwidth);
        getLc().setDownloadBandwidth(bandwidth);
    }

    private void setListPreferenceSummary(Preference preference, Object newValue) {
        ListPreference listPreference = (ListPreference)preference;
        CharSequence[] entries = listPreference.getEntries();
        int index = listPreference.findIndexOfValue((String)newValue);
        listPreference.setSummary(entries[index]);
    }
}
