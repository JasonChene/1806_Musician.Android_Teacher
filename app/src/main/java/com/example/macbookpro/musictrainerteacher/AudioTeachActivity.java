package com.example.macbookpro.musictrainerteacher;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
        @Override
        public void onUserMuteVideo(int uid, boolean muted) {
            Log.e("onUserEnableVideo",muted + ":"+uid);
            if (muted) {
                setupRemoteVideo(uid);
            }
            else {
//                FrameLayout container_remote = (FrameLayout) findViewById(R.id.remote_video_view_container);
//                container_remote.setVisibility(View.GONE);
            }
        }
    };
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        /* 返回键 */
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            leaveChannel();
            startActivity(new Intent(AudioTeachActivity.this, MainActivity.class));
        }
        return false;
    }
    private boolean isLogin() {
        JSONObject User = LocalStorage.getObject(AudioTeachActivity.this, "UserInfo");
        return User.length() > 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_teach);
        initActionBar();
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO)) {
            initAgoraEngineAndJoinChannel(9990);
            mRtcEngine.disableVideo();
            FrameLayout container = (FrameLayout) findViewById(R.id.local_video_view_container);
            container.setVisibility(View.GONE);
        }


        Button back_button = (Button) findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leaveChannel();
                startActivity(new Intent(AudioTeachActivity.this, MainActivity.class));

            }
        });

        final Button join_first_btn = (Button) findViewById(R.id.join_first_btn);
        join_first_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }

        });
//        final Button join_second_btn = (Button) findViewById(R.id.join_second_btn);
//        join_second_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mRtcEngine.disableVideo();
//                setupAudioAndJoinChannel(9997);
//            }
//        });
//        final Button join_third_btn = (Button) findViewById(R.id.join_third_btn);
//        join_third_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mRtcEngine.disableVideo();
//                setupAudioAndJoinChannel(9996);
//            }
//        });
//        final Button join_fourth_btn = (Button) findViewById(R.id.join_fourth_btn);
//        join_fourth_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mRtcEngine.disableVideo();
//                setupAudioAndJoinChannel(9995);
//            }
//        });

        final Button open_video_button = (Button) findViewById(R.id.open_video_button);
        open_video_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA))
                {
//                    setupLocalVideo(9990);
                    hideMusicPicture();
                    mRtcEngine.enableVideo();
                    FrameLayout local_container = (FrameLayout) findViewById(R.id.local_video_view_container);
                    local_container.setVisibility(View.VISIBLE);
                    FrameLayout remote_container = (FrameLayout) findViewById(R.id.remote_video_view_container);
                    remote_container.setVisibility(View.VISIBLE);
                }

            }
        });
        //离开房间
        final Button close_video_button = (Button) findViewById(R.id.close_video_button);
        close_video_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA))
                {
                    close_Video();
                    showMusicPicture();
                }

            }
        });
    }

    //初始化进入房间
    private void initAgoraEngineAndJoinChannel(int uid) {
        initializeAgoraEngine();     // Tutorial Step 1
        setupVideoProfile();
        setupLocalVideo(uid);
        joinChannel(uid);
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
        mRtcEngine.enableVideo();
        mRtcEngine.setVideoProfile(Constants.VIDEO_PROFILE_360P, false);
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
            View v = inflator.inflate(R.layout.actionbar_audio_teach, new LinearLayout(AudioTeachActivity.this), false);
            android.support.v7.app.ActionBar.LayoutParams layout = new android.support.v7.app.ActionBar.LayoutParams(
                    android.support.v7.app.ActionBar.LayoutParams.MATCH_PARENT, android.support.v7.app.ActionBar.LayoutParams.MATCH_PARENT);
            actionBar.setCustomView(v, layout);
            Toolbar parent = (Toolbar) v.getParent();
            parent.setContentInsetsAbsolute(0, 0);
        }
        TextView actionBarTitle = (TextView) findViewById(R.id.action_bar_title);
        actionBarTitle.setText("老师线上教室");
    }

    // Tutorial Step 4
    //加入频道 (joinChannel)
    private void joinChannel(int uid) {
        mRtcEngine.joinChannel(null, "demoChannel1", null, uid); // if you do not specify the uid, we will generate the uid for you
    }
    //离开房间
    private void leaveChannel() {
        mRtcEngine.leaveChannel();
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
        FrameLayout container = (FrameLayout) findViewById(R.id.remote_video_view_container);

        if (container.getChildCount() >= 1) {
            return;
        }
        container.setVisibility(View.VISIBLE);
        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
        container.addView(surfaceView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, uid));

        surfaceView.setTag(uid); // for mark purpose
//        View tipMsg = findViewById(R.id.quick_tips_when_use_agora_sdk); // optional UI
//        tipMsg.setVisibility(View.GONE);
    }
//    private void setupRemoteVideo(int uid) {
//        FrameLayout container = (FrameLayout) findViewById(R.id.remote_video_view_container);
//        if (container.getChildCount() >= 1) {
//            return;
//        }
//        container.setVisibility(View.VISIBLE);
//        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
//        container.addView(surfaceView);
//        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, uid));
//        surfaceView.setTag(uid); // for mark purpose
//    }

    private  void close_Video(){
        mRtcEngine.disableVideo();
        FrameLayout container_local = (FrameLayout) findViewById(R.id.local_video_view_container);
        container_local.setVisibility(View.GONE);
        FrameLayout container_remote = (FrameLayout) findViewById(R.id.remote_video_view_container);
        container_remote.setVisibility(View.GONE);
    }
    private void setupLocalVideo(int uid) {
        FrameLayout container = (FrameLayout) findViewById(R.id.local_video_view_container);
        container.setVisibility(View.VISIBLE);
        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
        surfaceView.setZOrderMediaOverlay(true);
        container.addView(surfaceView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, uid));
        surfaceView.setTag(uid); // for mark purpose

    }
    private  void hideMusicPicture (){
        View  MusicPicture = (View) findViewById(R.id.music_picture);
        MusicPicture.setVisibility(View.GONE);
    }
    private  void showMusicPicture (){
        View  MusicPicture = (View) findViewById(R.id.music_picture);
        MusicPicture.setVisibility(View.VISIBLE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {//根据请求码判断是哪一次申请的权限
            case PERMISSION_REQ_ID_RECORD_AUDIO:
                if (grantResults.length > 0) {//grantResults 数组中存放的是授权结果
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {//同意授权
                        //授权后做一些你想做的事情，即原来不需要动态授权时做的操作
                        checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA);
                    }else {//用户拒绝授权
                        //可以简单提示用户
                        Toast.makeText(AudioTeachActivity.this, "没有授权继续操作", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case PERMISSION_REQ_ID_CAMERA:
                if (grantResults.length > 0) {//grantResults 数组中存放的是授权结果
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {//同意授权
                        //授权后做一些你想做的事情，即原来不需要动态授权时做的操作
                        initAgoraEngineAndJoinChannel(9990);
                        mRtcEngine.disableVideo();
                        FrameLayout container = (FrameLayout) findViewById(R.id.local_video_view_container);
                        container.setVisibility(View.GONE);
                    }else {//用户拒绝授权
                        //可以简单提示用户
                        Toast.makeText(AudioTeachActivity.this, "没有授权继续操作", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default: super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
