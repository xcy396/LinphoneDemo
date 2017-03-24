package com.xuchongyang.linphonedemo.fragment;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.xuchongyang.linphonedemo.R;
import com.xuchongyang.linphonedemo.linphone.LinphoneCallManager;
import com.xuchongyang.linphonedemo.linphone.LinphoneManager;
import com.xuchongyang.linphonedemo.linphone.PhoneServiceCallback;
import com.xuchongyang.linphonedemo.linphone.PhoneVoiceUtils;
import com.xuchongyang.linphonedemo.service.LinphoneService;

import org.linphone.core.LinphoneCore;
import org.linphone.mediastream.Log;
import org.linphone.mediastream.video.AndroidVideoWindowImpl;
import org.linphone.mediastream.video.capture.hwconf.AndroidCameraConfiguration;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Mark Xu on 17/3/14.
 * 视频通话界面
 */
public class VideoFragment extends Fragment {
    private static final String TAG = "VideoFragment";
    @BindView(R.id.video_hang_image)
    ImageView mVideoHangImage;
    @BindView(R.id.video_surface)
    SurfaceView mVideoView;
    @BindView(R.id.video_capture_surface)
    SurfaceView mCaptureView;
    @BindView(R.id.silent_switch_camera_linear_layout)
    LinearLayout mSilentSwitchCamera;
    @BindView(R.id.silent_in_video)
    ImageView mSilent;
    @BindView(R.id.voice_out_in_video)
    ImageView mVoiceOut;
    @BindView(R.id.switch_camera_in_video)
    ImageView mSwitchCamera;


    private AndroidVideoWindowImpl mAndroidVideoWindow;

    public VideoFragment() {
        // Required empty public constructor
    }

    public static VideoFragment newInstance() {
        VideoFragment fragment = new VideoFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        ButterKnife.bind(this, view);

        mVideoHangImage.setVisibility(View.GONE);
        mSilentSwitchCamera.setVisibility(View.GONE);

        fixZOrder(mVideoView, mCaptureView);

        mSilent.setTag(false);
        mVoiceOut.setTag(true);

        mAndroidVideoWindow = new AndroidVideoWindowImpl(mVideoView, mCaptureView, new AndroidVideoWindowImpl.VideoWindowListener() {
            @Override
            public void onVideoRenderingSurfaceReady(AndroidVideoWindowImpl androidVideoWindow, SurfaceView surfaceView) {
                LinphoneManager.getLc().setVideoWindow(androidVideoWindow);
                mVideoView = surfaceView;
            }

            @Override
            public void onVideoRenderingSurfaceDestroyed(AndroidVideoWindowImpl androidVideoWindow) {
                LinphoneCore linphoneCore = LinphoneManager.getLc();
                if (linphoneCore != null) {
                    linphoneCore.setVideoWindow(null);
                }
            }

            @Override
            public void onVideoPreviewSurfaceReady(AndroidVideoWindowImpl androidVideoWindow, SurfaceView surfaceView) {
                mCaptureView = surfaceView;
                LinphoneManager.getLc().setPreviewWindow(mCaptureView);
            }

            @Override
            public void onVideoPreviewSurfaceDestroyed(AndroidVideoWindowImpl androidVideoWindow) {
                LinphoneManager.getLc().setPreviewWindow(null);
            }
        });

        LinphoneService.addCallback(new PhoneServiceCallback() {
            @Override
            public void callConnected() {
                super.callConnected();
                PhoneVoiceUtils.getInstance().toggleSpeaker(true);
                PhoneVoiceUtils.getInstance().toggleMicro(false);
            }

            @Override
            public void callReleased() {
                super.callReleased();
                getActivity().finish();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mVideoView != null) {
            ((GLSurfaceView) mVideoView).onResume();
        }

        if (mAndroidVideoWindow != null) {
            synchronized (mAndroidVideoWindow) {
                LinphoneManager.getLc().setVideoWindow(mAndroidVideoWindow);
            }
        }
    }

    @Override
    public void onPause() {
        if (mAndroidVideoWindow != null) {
            synchronized (mAndroidVideoWindow) {
                LinphoneManager.getLc().setVideoWindow(null);
            }
        }

        if (mVideoView != null) {
            ((GLSurfaceView) mVideoView).onPause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mCaptureView = null;
        mVideoView = null;

        if (mAndroidVideoWindow != null) {
            mAndroidVideoWindow.release();
            mAndroidVideoWindow = null;
        }

        super.onDestroy();
    }

    @OnClick(R.id.video_hang_image)
    public void videoHang() {
        PhoneVoiceUtils.getInstance().hangUp();
        getActivity().finish();
    }

    @OnClick(R.id.silent_in_video)
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

    @OnClick(R.id.voice_out_in_video)
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

    @OnClick(R.id.switch_camera_in_video)
    public void switchCamera() {
        try {
            int videoDeviceId = LinphoneManager.getLc().getVideoDevice();
            videoDeviceId = (videoDeviceId + 1) % AndroidCameraConfiguration.retrieveCameras().length;
            LinphoneManager.getLc().setVideoDevice(videoDeviceId);
            LinphoneCallManager.getInstance().updateCall();

            if (mCaptureView != null) {
                LinphoneManager.getLc().setPreviewWindow(mCaptureView);
            }
        } catch (ArithmeticException ae) {
            Log.e("Cannot switch camera : no camera");
        }
    }

    @OnClick(R.id.video_hang_frame_layout)
    public void showVideoHangButton() {
        if (mVideoHangImage.getVisibility() == View.VISIBLE) {
            mVideoHangImage.setVisibility(View.GONE);
            mSilentSwitchCamera.setVisibility(View.GONE);
        } else {
            mVideoHangImage.setVisibility(View.VISIBLE);
            mSilentSwitchCamera.setVisibility(View.VISIBLE);
        }
    }

    private void fixZOrder(SurfaceView video, SurfaceView preview) {
        video.setZOrderOnTop(false);
        preview.setZOrderOnTop(true);
        preview.setZOrderMediaOverlay(true); // Needed to be able to display control layout over
    }
}
