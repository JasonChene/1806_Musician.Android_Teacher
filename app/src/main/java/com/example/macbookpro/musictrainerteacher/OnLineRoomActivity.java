package com.example.macbookpro.musictrainerteacher;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.example.macbookpro.musictrainerteacher.common.SysExitUtil;
import com.example.macbookpro.musictrainerteacher.storage.LocalStorage;

import org.json.JSONObject;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

public class OnLineRoomActivity extends AppCompatActivity {

    RtcEngine mRtcEngine;
    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private static final int PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1;
    private static final String LOG_TAG = "LOG_TAG";

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
            Log.e(LOG_TAG,uid + ":onJoinChannelSuccess====================");
        }
        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) { // Tutorial Step 5
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setupRemoteVideo(uid);
                    Log.e(LOG_TAG,uid + ":onFirstRemoteVideoDecoded====================");

                }
            });
        }

    };
    private  boolean isLogin(){
        JSONObject User =  LocalStorage.getObject(OnLineRoomActivity.this, "UserInfo");
        return  User.length() > 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_room);
        SysExitUtil.activityList.add(OnLineRoomActivity.this);
        initializeAgoraEngine();
        Button start_button = (Button)findViewById(R.id.start_button);
        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
                    setupVideoAndJoinChannel(9997);
//                    setupVideoProfile();         // Tutorial Step 2
//                    setupLocalVideo(9997);
                }
            }
        });
        Button join_button = (Button)findViewById(R.id.join_button);
        join_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
                    setupVideoAndJoinChannel(9998);
                }
            }
        });
        Button rejoin_button = (Button)findViewById(R.id.rejoin_button);
        rejoin_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                setupVideoAndJoinChannel(9999);
                mRtcEngine.enableVideo();
                setupLocalVideo(9997);           // Tutorial Step 3
            }
        });

        Button leave_start_button = (Button)findViewById(R.id.leave_start_button);
        leave_start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leaveChannel();
                FrameLayout container = (FrameLayout) findViewById(R.id.local_video_view_container);
                container.setVisibility(View.GONE);
            }
        });

        Button leave_join_button = (Button)findViewById(R.id.leave_join_button);
        leave_join_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leaveChannel();
                FrameLayout container = (FrameLayout) findViewById(R.id.remote_video_view_container);
                container.setVisibility(View.GONE);
            }
        });
        Button leave_rejoin_button = (Button)findViewById(R.id.leave_rejoin_button);
        leave_rejoin_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRtcEngine.enableVideo();
                setupLocalVideo(9998);
            }
        });
    }
    public boolean checkSelfPermission(String permission, int requestCode) {
        Log.i(LOG_TAG, "checkSelfPermission " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    requestCode);
            return false;
        }
        return true;
    }
    private void setupVideoAndJoinChannel(int uid) {
        setupVideoProfile();         // Tutorial Step 2
        joinChannel(uid);               // Tutorial Step 4
    }
    private void setupAudioAndJoinChannel(int uid) {
        setupAudioProfile();         // Tutorial Step 2
        joinChannel(uid);               // Tutorial Step 4
    }
    private void initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    // Tutorial Step 2
    private void setupVideoProfile() {
        mRtcEngine.disableVideo();
        mRtcEngine.setVideoProfile(Constants.VIDEO_PROFILE_360P, false);
    }

    private void setupAudioProfile() {
        mRtcEngine.enableAudio();
        mRtcEngine.setAudioProfile(0,2);
    }

    // Tutorial Step 3
    private void setupLocalVideo(int uid) {
        FrameLayout container = (FrameLayout) findViewById(R.id.local_video_view_container);
        if (uid == 9998)
        {
            Log.e("9998","1");
            container = (FrameLayout) findViewById(R.id.remote_video_view_container);
        }
        container.setVisibility(View.VISIBLE);
        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
        surfaceView.setZOrderMediaOverlay(true);
        container.addView(surfaceView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, uid));
    }

    // Tutorial Step 4
    private void joinChannel(int uid) {
        mRtcEngine.joinChannel(null, "demoChannel1", "Extra Optional Data", uid); // if you do not specify the uid, we will generate the uid for you
    }

    // Tutorial Step 5
    private void setupRemoteVideo(int uid) {
        FrameLayout container = (FrameLayout) findViewById(R.id.local_video_view_container);
        if (uid == 9998)
        {
            container = (FrameLayout) findViewById(R.id.remote_video_view_container);
        }
        container.setVisibility(View.VISIBLE);
        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
        container.addView(surfaceView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, uid));
        surfaceView.setTag(uid); // for mark purpose

    }

    // Tutorial Step 6
    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

    // Tutorial Step 7
    private void onRemoteUserLeft() {
//        FrameLayout container = (FrameLayout) findViewById(R.id.remote_video_view_container);
//        container.removeAllViews();
//
//        View tipMsg = findViewById(R.id.quick_tips_when_use_agora_sdk); // optional UI
//        tipMsg.setVisibility(View.VISIBLE);
    }

    // Tutorial Step 10
    private void onRemoteUserVideoMuted(int uid, boolean muted) {
        FrameLayout container = (FrameLayout) findViewById(R.id.remote_video_view_container);
        SurfaceView surfaceView = (SurfaceView) container.getChildAt(0);

        Object tag = surfaceView.getTag();
        if (tag != null && (Integer) tag == uid) {
            surfaceView.setVisibility(muted ? View.GONE : View.VISIBLE);
        }
    }
}
