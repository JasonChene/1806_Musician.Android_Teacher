package com.example.macbookpro.musictrainerteacher;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.macbookpro.musictrainerteacher.CustomView.Draw;
import com.example.macbookpro.musictrainerteacher.common.SysExitUtil;
import com.example.macbookpro.musictrainerteacher.storage.LocalStorage;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.rts.RTSCallback;
import com.netease.nimlib.sdk.rts.RTSManager;
import com.netease.nimlib.sdk.rts.constant.RTSTunnelType;
import com.netease.nimlib.sdk.rts.model.RTSCommonEvent;
import com.netease.nimlib.sdk.rts.model.RTSData;
import com.netease.nimlib.sdk.rts.model.RTSTunData;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

import static android.net.sip.SipErrorCode.SERVER_ERROR;
import static com.netease.nimlib.sdk.StatusCode.LOGINED;

public class AudioTeachActivity extends AppCompatActivity {

    RtcEngine mRtcEngine = null;
    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private static final int PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1;
    private static final String LOG_TAG = "LOG_TAG";


    public static final int GET_DATA_SUCCESS = 1;
    public static final int NETWORK_ERROR = 2;
    public static final int SERVER_ERROR = 3;

    Draw main_draw;
    View drawBackgroud;
    Draw peer_draw;
    String Channel_name = "demoChannel1";
    MyLeanCloudApp myApp;
    private IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
            Log.e(LOG_TAG, uid + ":onJoinChannelSuccess===================="+ channel);
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

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case GET_DATA_SUCCESS:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    setImageBitmap(bitmap);
                    break;
                case NETWORK_ERROR:
                    Toast.makeText(AudioTeachActivity.this,"网络连接失败",Toast.LENGTH_SHORT).show();
                    break;
                case SERVER_ERROR:
                    Toast.makeText(AudioTeachActivity.this,"服务器发生错误",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    public void setImageBitmap(Bitmap bitmap)
    {
        drawBackgroud.setBackground(new BitmapDrawable(getResources(),bitmap));
        //显示清除按钮
        Button clear_button = (Button) findViewById(R.id.clear);
        clear_button.setVisibility(View.VISIBLE);
    }

    public void startKeepUpBoard(String sessionID, String toAccount)
    {
        main_draw.sessionID = sessionID;     //参数传递
        main_draw.toAccount = toAccount;     //参数传递
        main_draw.setVisibility(View.VISIBLE);
        peer_draw.sessionID = sessionID;
        peer_draw.toAccount = toAccount;
        peer_draw.setVisibility(View.VISIBLE);
//        //显示清除按钮
//        Button clear_button = (Button) findViewById(R.id.clear);
//        clear_button.setVisibility(View.VISIBLE);
        drawBackgroud.setVisibility(View.VISIBLE);

        //注册收到数据的监听
        WhiteBoardManager.registerIncomingData(sessionID,true, main_draw, AudioTeachActivity.this);
        WhiteBoardManager.registerRTSCloseObserver(sessionID,true,AudioTeachActivity.this);
        //隐藏本地视频窗口
        FrameLayout local_container = (FrameLayout) findViewById(R.id.local_video_view_container);
        local_container.setVisibility(View.GONE);
    }
    public void terminateRTS(String sessionID)
    {

        //注销收数据监听
        Boolean isDataSuccess = RTSManager.getInstance().observeReceiveData(sessionID, new Observer<RTSTunData>() {
            @Override
            public void onEvent(RTSTunData rtsTunData) {
            }
        }, false);
        Log.e("TAG","注销收数据监听"+isDataSuccess);
        //注销挂断监听
        Boolean isCloseSuccess = RTSManager.getInstance().observeHangUpNotification(sessionID, new Observer<RTSCommonEvent>() {
            @Override
            public void onEvent(RTSCommonEvent rtsCommonEvent) {
            }
        },false);
        Log.e("TAG","注销挂断监听"+isCloseSuccess);

        main_draw.Clear();
        main_draw.setVisibility(View.GONE);
        peer_draw.Clear();
        peer_draw.setVisibility(View.GONE);

        Button clear_button = (Button) findViewById(R.id.clear);
        clear_button.setVisibility(View.GONE);
        //清楚原来的乐谱
        drawBackgroud.setBackgroundResource(0);
        drawBackgroud.setVisibility(View.GONE);
    }
    public void updateWhiteBoardChannelID(String channelID)
    {
        main_draw.channelID = channelID;
    }
    public void addMusicPic(String strMusicImageUrl)
    {
        //设置本地图片
        Log.i("MusicPicUrl:","---------------------:"+strMusicImageUrl);
        setImageURL(strMusicImageUrl);
    }

    public void setImageURL(final String path) {
        //开启一个线程用于联网
        new Thread() {
            @Override
            public void run() {
                try {
                    //把传过来的路径转成URL
                    URL url = new URL(path);
                    //获取连接
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    //使用GET方法访问网络
                    connection.setRequestMethod("GET");
                    //超时时间为10秒
                    connection.setConnectTimeout(10000);
                    //获取返回码
                    int code = connection.getResponseCode();
                    if (code == 200) {
                        InputStream inputStream = connection.getInputStream();
                        //使用工厂把网络的输入流生产Bitmap
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        //利用Message把图片发给Handler
                        Message msg = Message.obtain();
                        msg.obj = bitmap;
                        msg.what = GET_DATA_SUCCESS;
                        handler.sendMessage(msg);
                        inputStream.close();
                    }else {
                        //服务启发生错误
                        handler.sendEmptyMessage(SERVER_ERROR);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    //网络连接错误
                    handler.sendEmptyMessage(NETWORK_ERROR);
                }
            }
        }.start();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_teach);
        SysExitUtil.activityList.add(AudioTeachActivity.this);
        initActionBar();
//        WhiteBoardManager.registerRTSIncomingCallObserver(true,this);

        myApp=(MyLeanCloudApp) getApplication();
        myApp.setAudioTeachActivity(AudioTeachActivity.this);

        main_draw = findViewById(R.id.main_draw);
        peer_draw = findViewById(R.id.peer_draw);

        drawBackgroud = findViewById(R.id.drawBackgroud);
        if (mRtcEngine == null && checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO)) {
            initAgoraEngineAndJoinChannel(9998);
            joinChannel(9998);
            mRtcEngine.disableVideo();
            FrameLayout container = (FrameLayout)findViewById(R.id.local_video_view_container);
            container.setVisibility(View.GONE);
        }

        //顶部返回按键
        Button back_button = (Button) findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (main_draw.getVisibility() == View.GONE){
                    final FrameLayout remote_video = findViewById(R.id.remote_video_view_container);
                    if (remote_video.getVisibility() == View.GONE){
                        finish();
                        startActivity(new Intent(AudioTeachActivity.this, MainActivity.class));
                    }
                    else {
                        Toast.makeText(AudioTeachActivity.this, "现在正在与学生教学", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(AudioTeachActivity.this, "现在正在与学生教学", Toast.LENGTH_SHORT).show();
                }

            }
        });


        //清空画板
        Button clear_button = (Button) findViewById(R.id.clear);
        clear_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                main_draw.Clear();
                peer_draw.Clear();
                String clear_remote = "clear";
                WhiteBoardManager.sendToRemote(main_draw.sessionID,main_draw.toAccount,clear_remote);
            }
        });


        final Button join_first_btn = (Button) findViewById(R.id.join_first_btn);
        join_first_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FrameLayout container = (FrameLayout)findViewById(R.id.local_video_view_container);
                if(container.getVisibility() == View.GONE){
                    close_Video();
                    showMusicPicture();
                    leaveChannel();
                    Channel_name = "demoChannel1";
//                    initAgoraEngineAndJoinChannel(9998);
                    joinChannel(9998);
                    mRtcEngine.disableVideo();
                    container.setVisibility(View.GONE);
                }else{
                    Toast.makeText(AudioTeachActivity.this, "现在正在与学生教学,请先关闭视频", Toast.LENGTH_SHORT).show();
                }
            }

        });
        final Button join_second_btn = (Button) findViewById(R.id.join_second_btn);
        join_second_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FrameLayout container = (FrameLayout)findViewById(R.id.local_video_view_container);
                if(container.getVisibility() == View.GONE){
                    close_Video();
                    showMusicPicture();
                    leaveChannel();
                    Channel_name = "demoChannel2";
//                    initAgoraEngineAndJoinChannel(9998);
                    joinChannel(9998);
                    mRtcEngine.disableVideo();
                    container.setVisibility(View.GONE);
                }else{
                    Toast.makeText(AudioTeachActivity.this, "现在正在与学生教学,请先关闭视频", Toast.LENGTH_SHORT).show();
                }
            }
        });
//        final Button join_third_btn = (Button) findViewById(R.id.join_third_btn);
//        join_third_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//               leaveChannel();
//                Channel_name = "demoChannel3";
//                initAgoraEngineAndJoinChannel(9998);
//                mRtcEngine.disableVideo();
//                FrameLayout container = (FrameLayout)findViewById(R.id.local_video_view_container);
//                container.setVisibility(View.GONE);
//            }
//        });
//        final Button join_fourth_btn = (Button) findViewById(R.id.join_fourth_btn);
//        join_fourth_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//               leaveChannel();
//                Channel_name = "demoChannel4";
//                initAgoraEngineAndJoinChannel(9998);
//                mRtcEngine.disableVideo();
//                FrameLayout container = (FrameLayout)findViewById(R.id.local_video_view_container);
//                container.setVisibility(View.GONE);
//            }
//        });

        final Button open_video_button = (Button) findViewById(R.id.open_video_button);
        open_video_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button openBtn = (Button)view;
                if (openBtn.getText().equals("打开视频教学"))
                {
                    if (checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA))
                    {
                        hideMusicPicture();
                        mRtcEngine.enableVideo();
                        FrameLayout local_container = (FrameLayout) findViewById(R.id.local_video_view_container);
                        local_container.setVisibility(View.VISIBLE);
                        FrameLayout remote_container = (FrameLayout) findViewById(R.id.remote_video_view_container);
                        remote_container.setVisibility(View.VISIBLE);
//                        openBtn.setText("关闭视频教学");
                    }
                }else if(openBtn.getText().equals("关闭视频教学"))
                {
                    if (checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA))
                    {
                        close_Video();
                        showMusicPicture();
//                        openBtn.setText("打开视频教学");
                    }
                }
                openBtn.setText(openBtn.getText().equals("打开视频教学")?"关闭视频教学":"打开视频教学");


            }
        });
        //离开房间
//        final Button close_video_button = (Button) findViewById(R.id.close_video_button);
//        close_video_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA))
//                {
//                    close_Video();
//                    showMusicPicture();
//                }
//
//            }
//        });
    }

    //初始化进入房间
    private void initAgoraEngineAndJoinChannel(int uid) {
        initializeAgoraEngine();     // Tutorial Step 1
        setupVideoProfile();
        setupLocalVideo(uid);
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
        mRtcEngine.joinChannel(null, "" + Channel_name, null, uid); // if you do not specify the uid, we will generate the uid for you
    }
    //离开房间
    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

    public boolean checkSelfPermission(String permission, int requestCode) {
        Log.e(LOG_TAG, "checkSelfPermission " + permission + " " + requestCode);
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
            container.removeAllViews();
        }
        container.setVisibility(View.VISIBLE);
        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
        container.addView(surfaceView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
        surfaceView.setTag(uid); // for mark purpose
    }


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
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
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
                        initAgoraEngineAndJoinChannel(9998);
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
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        final FrameLayout local_video = findViewById(R.id.local_video_view_container);
        /* 返回键 */
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (main_draw.getVisibility() == View.GONE){
                if (local_video.getVisibility() == View.GONE){
                    this.finish();
                }
                else {
                    Toast.makeText(AudioTeachActivity.this, "现在正在与学生教学", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(AudioTeachActivity.this, "现在正在与学生教学", Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }
//private long time = 0;
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            if ((System.currentTimeMillis() - time > 1000)) {
//                Toast.makeText(this, "正在与学生教学，再点一次返回", Toast.LENGTH_SHORT).show();
//                time = System.currentTimeMillis();
//            } else {
//                leaveChannel();
//                startActivity(new Intent(AudioTeachActivity.this, MainActivity.class));
//            }
//            return true;
//        } else {
//            NIMClient.getService(AuthService.class).logout();
//            return super.onKeyDown(keyCode, event);
//        }
//
//    }
//
//

    @Override
    protected void onDestroy() {
        Log.e("TAG","onDestroy=============+++++++++++");
        leaveChannel();
        mRtcEngine.destroy();
        super.onDestroy();
    }
}
