package com.example.macbookpro.musictrainerteacher;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.macbookpro.musictrainerteacher.storage.LocalStorage;

import org.json.JSONObject;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

public class AudioTeachActivity extends AppCompatActivity {

    RtcEngine mRtcEngine;
    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private static final int PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1;
    private static final String LOG_TAG = "LOG_TAG";

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
            Log.e(LOG_TAG, uid + ":onJoinChannelSuccess====================");
        }

        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) { // Tutorial Step 5
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setupRemoteVideo(uid);
                    Log.e(LOG_TAG, uid + ":onFirstRemoteVideoDecoded====================");

                }
            });
        }
    };

    private boolean isLogin() {
        JSONObject User = LocalStorage.getObject(AudioTeachActivity.this, "UserInfo");
        return User.length() > 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_teach);
        initActionBar();


        final Button start_button = (Button) findViewById(R.id.start_button);
        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO)) {
                    initAgoraEngineAndJoinChannel();
                    setupAudioAndJoinChannel(9998);
                    Log.e(LOG_TAG,   "111");

                }
//                    setupAudioProfile();         // Tutorial Step 2
//                    setupLocalVideo(9997);
                }
            });
        }

    //初始化进入房间
    private void initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine();     // Tutorial Step 1
//        joinChannel();               // Tutorial Step 2
        Log.e(LOG_TAG,   "222");
    }
    //初始化声网
    private void initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));

            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }
    //设置导航栏
    public void initActionBar() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            LayoutInflater inflator = (LayoutInflater) this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflator.inflate(R.layout.actionbar_main, new LinearLayout(AudioTeachActivity.this), false);
            android.support.v7.app.ActionBar.LayoutParams layout = new android.support.v7.app.ActionBar.LayoutParams(
                    android.support.v7.app.ActionBar.LayoutParams.MATCH_PARENT, android.support.v7.app.ActionBar.LayoutParams.MATCH_PARENT);
            actionBar.setCustomView(v, layout);
            Toolbar parent = (Toolbar) v.getParent();
            parent.setContentInsetsAbsolute(0, 0);
        }
        TextView actionBarTitle = (TextView) findViewById(R.id.action_bar_title);
        actionBarTitle.setText("老师线上教室");
    }
    //设置音频并加入频道
    private void setupAudioAndJoinChannel(int uid) {
       // setupAudioProfile();         // Tutorial Step 2
        joinChannel(uid);               // Tutorial Step 4
        Log.e(LOG_TAG,   "333");

    }
    //设置视频并加入频道
    private void setupVideoAndJoinChannel(int uid) {
        setupVideoProfile();         // Tutorial Step 2
        joinChannel(uid);               // Tutorial Step 4
    }
    // Tutorial Step 2
    //设置视频参数
    private void setupVideoProfile() {
        mRtcEngine.disableVideo();//关闭视频
        mRtcEngine.setVideoProfile(Constants.VIDEO_PROFILE_360P, false);
    }
    // Tutorial Step 4
    //加入频道 (joinChannel)
    private void joinChannel(int uid) {
        mRtcEngine.joinChannel(null, "demoChannel1", "Extra Optional Data", uid); // if you do not specify the uid, we will generate the uid for you
    }
    //离开房间
    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

    //设置音质 (setAudioProfile)
    private void setupAudioProfile() {
        mRtcEngine.enableAudio();//打开音频
        mRtcEngine.setAudioProfile(0,2);//通信模式下为 1，直播模式下为 2
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
}
