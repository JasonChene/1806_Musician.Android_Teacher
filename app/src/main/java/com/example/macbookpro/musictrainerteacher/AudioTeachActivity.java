package com.example.macbookpro.musictrainerteacher;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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


import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMMessageHandler;
import com.avos.avoscloud.im.v2.AVIMMessageManager;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;

import com.example.macbookpro.musictrainerteacher.CustomView.Draw;
import com.example.macbookpro.musictrainerteacher.common.SysExitUtil;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.rts.RTSCallback;
import com.netease.nimlib.sdk.rts.RTSManager;
import com.netease.nimlib.sdk.rts.model.RTSCommonEvent;
import com.netease.nimlib.sdk.rts.model.RTSTunData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.internal.Logging;
import io.agora.rtc.video.VideoCanvas;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class AudioTeachActivity extends AppCompatActivity {
    public static final int GET_DATA_SUCCESS = 1;
    public static final int NETWORK_ERROR = 2;
    public static final int SERVER_ERROR = 3;
    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private static final int PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1;
    private static final String LOG_TAG = "LOG_TAG";
    RtcEngine mRtcEngine = null;
    Draw main_draw;
    View drawBackgroud;
    Draw peer_draw;
    String Channel_name = "demoChannel1";
    String student_info = "null";
    MyLeanCloudApp myApp;
    JSONArray mArrStudentInfo;
    JSONArray mArrJoinStudentInfo;
    private CustomMessageHandler customMessageHandler;
    TextView showHandupInfo;
    String mStrImagePath = "";
    List<String> mPeerDataList = new ArrayList<String>();
    List<String> mDrawDataList = new ArrayList<String>();
    List<ListDrawBitMap> mAllBitmap = new ArrayList<ListDrawBitMap>();
    private IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
            Log.e(LOG_TAG, uid + ":onJoinChannelSuccess" + channel);
        }

        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) { // Tutorial Step 5
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setupRemoteVideo(uid);
                    Log.e(LOG_TAG, uid + ":onFirstRemoteVideoDecoded");
                }
            });
        }

        @Override
        public void onUserOffline(int uid, int reason)
        {
            Log.e("leave",reason + ";" + uid);
            Log.e("Channel_name",Channel_name);
        }
    };
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_DATA_SUCCESS:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    setImageBitmap(bitmap);
                    break;
                case NETWORK_ERROR:
                    Toast.makeText(AudioTeachActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                    break;
                case SERVER_ERROR:
                    Toast.makeText(AudioTeachActivity.this, "服务器发生错误", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void setImageBitmap(Bitmap bitmap) {
        drawBackgroud.setBackground(new BitmapDrawable(getResources(), bitmap));
        //显示清除按钮
        Button clear_button = (Button) findViewById(R.id.clear);
        clear_button.setVisibility(View.VISIBLE);
        Button close_music = (Button)findViewById(R.id.close_music);
        close_music.setVisibility(VISIBLE);
    }

    public void startKeepUpBoard(String sessionID, String toAccount) {
        main_draw.sessionID = sessionID;     //参数传递
        main_draw.toAccount = toAccount;     //参数传递
        main_draw.setVisibility(View.VISIBLE);
        peer_draw.sessionID = sessionID;
        peer_draw.toAccount = toAccount;
        peer_draw.setVisibility(View.VISIBLE);
        drawBackgroud.setVisibility(View.VISIBLE);
        hideMusicPicture();

        TextView textView = (TextView) findViewById(R.id.who_be_teach);
        String message = textView.getText().toString().replace("语音","乐谱");
        updateTeachStatus(message);

        //注册收到数据的监听
        WhiteBoardManager.registerIncomingData(sessionID, true, main_draw, AudioTeachActivity.this);
        WhiteBoardManager.registerRTSCloseObserver(sessionID, true, AudioTeachActivity.this);
        //隐藏本地视频窗口
        FrameLayout local_container = (FrameLayout) findViewById(R.id.local_video_view_container);
        local_container.setVisibility(GONE);
    }

    public static Bitmap getBitmapFromView(View v) {
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        Drawable bgDrawable = v.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        v.draw(canvas);
        return b;
    }

    public void terminateRTS(String sessionID) {
        if (main_draw.getVisibility() == GONE)
            return;
        //注销收数据监听
        Boolean isDataSuccess = RTSManager.getInstance().observeReceiveData(sessionID, new Observer<RTSTunData>() {
            @Override
            public void onEvent(RTSTunData rtsTunData) {
            }
        }, false);
        Log.e("TAG", "注销收数据监听" + isDataSuccess);
        //注销挂断监听
        Boolean isCloseSuccess = RTSManager.getInstance().observeHangUpNotification(sessionID, new Observer<RTSCommonEvent>() {
            @Override
            public void onEvent(RTSCommonEvent rtsCommonEvent) {

            }
        }, false);
        Log.e("TAG", "注销挂断监听" + isCloseSuccess);

        HashMap<String, List<String>> main_map = new HashMap<String, List<String>>();
        List<String> drawData = new ArrayList<>(mDrawDataList);
        main_map.put("main_draw",drawData);
        HashMap<String, List<String>> peer_map = new HashMap<String, List<String>>();
        List<String> peerData = new ArrayList<>(mPeerDataList);
        peer_map.put("peer_draw",peerData);
        ListDrawBitMap drawBitMap = new ListDrawBitMap(mStrImagePath,Channel_name,main_map,peer_map);
        Boolean isExistCommonPath = false;
        for (int i = 0; i < mAllBitmap.size(); i ++)
        {
            if (mAllBitmap.get(i).channel_name.equals(Channel_name) && mAllBitmap.get(i).path.equals(mStrImagePath))
            {
                //在后面追加数据
                mAllBitmap.get(i).main_map.get("main_draw").addAll(drawData);
                mAllBitmap.get(i).peer_map.get("peer_draw").addAll(peerData);
                isExistCommonPath = true;
                break;
            }
            else if (mAllBitmap.get(i).channel_name.equals(Channel_name) && !mAllBitmap.get(i).path.equals(mStrImagePath))
            {
                mAllBitmap.remove(i);
                isExistCommonPath = false;
                break;
            }
        }
        if (isExistCommonPath == false)
        {
            mAllBitmap.add(drawBitMap);
        }

        mDrawDataList.clear();
        mPeerDataList.clear();

        main_draw.Clear();
        main_draw.setVisibility(GONE);
        peer_draw.Clear();
        peer_draw.setVisibility(GONE);

        Button clear_button = (Button) findViewById(R.id.clear);
        clear_button.setVisibility(GONE);
        Button close_music = (Button)findViewById(R.id.close_music);
        close_music.setVisibility(GONE);
        //清楚原来的乐谱
        drawBackgroud.setBackgroundResource(0);
        drawBackgroud.setVisibility(GONE);
        showMusicPicture();

        TextView textView = (TextView) findViewById(R.id.who_be_teach);
        String message = textView.getText().toString().replace("乐谱","语音");
        updateTeachStatus(message);
    }

    public void addMusicPic(String strMusicImageUrl, String strImagePath) {
        //设置本地图片
        Log.i("MusicPicUrl:", "" + strMusicImageUrl);
//        setImageURL(strMusicImageUrl);
        mStrImagePath = strImagePath;
        Boolean isExist = false;
        for (int i = 0; i < mAllBitmap.size(); i ++)
        {
            ListDrawBitMap drawInfo = mAllBitmap.get(i);
            if (drawInfo.channel_name.equals(Channel_name) && drawInfo.path.equals(strImagePath))
            {
                //取出来是同一张图
                for (int m = 0; m < drawInfo.main_map.get("main_draw").size(); m ++)
                {
                    main_draw.dataPaint(drawInfo.main_map.get("main_draw").get(m));
                }
                for (int n = 0; n < drawInfo.peer_map.get("peer_draw").size(); n ++)
                {
                    peer_draw.dataPaint(drawInfo.peer_map.get("peer_draw").get(n));
                }
                break;
            }
            else if (drawInfo.channel_name.equals(Channel_name) && drawInfo.path.equals(strImagePath))
            {
                mAllBitmap.remove(i);
            }
        }
        setImageURL(strMusicImageUrl);
    }
    public void addPeerData(String data)
    {
        mPeerDataList.add(data);
    }
    public void addDrawData(String data)
    {
        mDrawDataList.add(data);
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
                    } else {
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
        myApp = (MyLeanCloudApp) getApplication();
        myApp.setAudioTeachActivity(AudioTeachActivity.this);
        main_draw = findViewById(R.id.main_draw);
        main_draw.setContext(AudioTeachActivity.this);
        peer_draw = findViewById(R.id.peer_draw);
        peer_draw.setContext(AudioTeachActivity.this);

        showHandupInfo = (TextView)findViewById(R.id.showHandupInfo);

        //设置教学页面的学生按钮的颜色
        week_onclick();
        //接受传过来的课程信息
        Intent intent = getIntent();
        student_info = intent.getStringExtra("student_info");
        Log.e("student_info", "" + student_info);
        get_student_info_handle();
        mArrJoinStudentInfo = new JSONArray();

        drawBackgroud = findViewById(R.id.drawBackgroud);
        if (mRtcEngine == null && checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO)) {
            initAgoraEngineAndJoinChannel(9998);
            mRtcEngine.disableVideo();
            mRtcEngine.setEnableSpeakerphone(true);
            FrameLayout container = (FrameLayout) findViewById(R.id.local_video_view_container);
            container.setVisibility(GONE);
            //注册默认的消息处理逻辑
            customMessageHandler = new CustomMessageHandler();
            customMessageHandler.setIsOpen(true);
            AVIMMessageManager.registerMessageHandler(AVIMMessage.class, customMessageHandler);
            //通知学生老师上线
            sendMessageToStudents("通知学生老师在线","老师上线");
        }

        //清空画板
        Button clear_button = (Button) findViewById(R.id.clear);
        clear_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                main_draw.Clear();
                peer_draw.Clear();
                for (int i = 0; i < mAllBitmap.size(); i ++)
                {
                    if (mAllBitmap.get(i).channel_name.equals(Channel_name))
                    {
                        mAllBitmap.remove(i);
                        mDrawDataList.clear();
                        mPeerDataList.clear();
                    }
                }
                String clear_remote = "clear";
                WhiteBoardManager.sendToRemote(main_draw.sessionID, main_draw.toAccount, clear_remote);
            }
        });
        Button close_music = (Button)findViewById(R.id.close_music);
        close_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String close_remote = "close";
                WhiteBoardManager.sendCloseToRemote(main_draw.sessionID, main_draw.toAccount, close_remote,AudioTeachActivity.this);
            }
        });
        //初始化按钮的图标
        stu_audio_icon_init();
        //离开房间
        final Button close_video_button = (Button) findViewById(R.id.open_video_button);
        close_video_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button openBtn = (Button) view;
                if (openBtn.getText().equals("打开视频教学")) {
                    if (checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
                        hideMusicPicture();
                        mRtcEngine.enableVideo();
                        FrameLayout local_container = (FrameLayout) findViewById(R.id.local_video_view_container);
                        local_container.setVisibility(View.VISIBLE);
                        FrameLayout remote_container = (FrameLayout) findViewById(R.id.remote_video_view_container);
                        remote_container.setVisibility(View.VISIBLE);
                        Button pause_playing_btn = (Button)findViewById(R.id.pause_playing);
                        pause_playing_btn.setVisibility(GONE);
                    }
                } else if (openBtn.getText().equals("关闭视频教学")) {
                    if (checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
                        close_Video();
                        showMusicPicture();
                        Button pause_playing_btn = (Button)findViewById(R.id.pause_playing);
                        pause_playing_btn.setVisibility(VISIBLE);
                    }
                }
                openBtn.setText(openBtn.getText().equals("打开视频教学") ? "关闭视频教学" : "打开视频教学");
            }
        });

        Button pause_playing_btn = (Button)findViewById(R.id.pause_playing);
        pause_playing_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendPauseMessageToCurrentStudent(Channel_name,"叫停学生","pausePlaying");
            }
        });
    }

    public String getUserName(int index) {
        String user_name = "未上线";
        try {
            user_name = mArrJoinStudentInfo.getJSONObject(index).getString("name");
        } catch (JSONException e) {

        }
        return user_name;
    }

    public void joinInNewRoom(int index) {
        String objectID = "";
        try {
            objectID = mArrJoinStudentInfo.getJSONObject(index).getString("studentID");
        } catch (JSONException e) {
        }
        close_Video();
        showMusicPicture();
        leaveChannel();
        Channel_name = objectID;
        joinChannel(9998);
        mRtcEngine.disableVideo();
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
        //顶部返回按键
        Button back_button = (Button) findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (main_draw.getVisibility() == GONE) {
                    final FrameLayout remote_video = findViewById(R.id.remote_video_view_container);
                    if (remote_video.getVisibility() == GONE) {
                        finish();
                        startActivity(new Intent(AudioTeachActivity.this, MainActivity.class));
                    } else {
                        Toast.makeText(AudioTeachActivity.this, "请先主动关闭乐谱后再退出课程", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AudioTeachActivity.this, "请先主动关闭视频后再退出课程", Toast.LENGTH_SHORT).show();
                }

            }
        });
        TextView actionBarTitle = (TextView) findViewById(R.id.action_bar_title);
        actionBarTitle.setText("老师线上教室");
    }

    private void joinChannel(int uid) {
        mRtcEngine.joinChannel(null, "" + Channel_name, null, uid); // if you do not specify the uid, we will generate the uid for you
    }

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

    private void close_Video() {
        if (checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
            showMusicPicture();
            mRtcEngine.disableVideo();
            FrameLayout container_local = (FrameLayout) findViewById(R.id.local_video_view_container);
            container_local.setVisibility(GONE);
            FrameLayout container_remote = (FrameLayout) findViewById(R.id.remote_video_view_container);
            container_remote.setVisibility(GONE);
        }
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

    private void hideMusicPicture() {
        View MusicPicture = (View) findViewById(R.id.music_picture);
        MusicPicture.setVisibility(GONE);
    }

    private void showMusicPicture() {
        View MusicPicture = (View) findViewById(R.id.music_picture);
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
                    } else {//用户拒绝授权
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
                        container.setVisibility(GONE);
                        //注册默认的消息处理逻辑
                        customMessageHandler = new CustomMessageHandler();
                        customMessageHandler.setIsOpen(true);
                        AVIMMessageManager.registerMessageHandler(AVIMMessage.class, customMessageHandler);
                        //通知学生老师上线
                        sendMessageToStudents("通知学生老师在线","老师上线");
                    } else {//用户拒绝授权
                        //可以简单提示用户
                        Toast.makeText(AudioTeachActivity.this, "没有授权继续操作", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        final FrameLayout local_video = findViewById(R.id.local_video_view_container);
        /* 返回键 */
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (main_draw.getVisibility() == GONE) {
                if (local_video.getVisibility() == GONE) {
                    this.finish();
                    startActivity(new Intent(AudioTeachActivity.this, MainActivity.class));
                } else {
                    Toast.makeText(AudioTeachActivity.this, "现在正在与学生教学", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } else {
                Toast.makeText(AudioTeachActivity.this, "现在正在与学生教学", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return super.onKeyDown(keyCode,event);
    }

    @Override
    protected void onDestroy() {
        Log.e("TAG", "==ggg===onDestroy");
        sendMessageToStudents("通知学生老师下线","老师下线");
        leaveChannel();
        mRtcEngine.destroy();
        customMessageHandler.setIsOpen(false);
        AVIMMessageManager.unregisterMessageHandler(AVIMMessage.class, customMessageHandler);
        super.onDestroy();
    }

    public void get_student_info_handle() {
        try {
            mArrStudentInfo = new JSONArray(student_info);
        } catch (JSONException error) {
            Log.e("error", "error" + error);
        }
    }

    public void week_onclick() {
        final LinearLayout weekLinearLayout = (LinearLayout) findViewById(R.id.all_student);
        final FrameLayout container = (FrameLayout) findViewById(R.id.local_video_view_container);
        for (int i = 0; i < weekLinearLayout.getChildCount(); i++) {
            Button btn = (Button) weekLinearLayout.getChildAt(i);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Button test = (Button) view;
                    if (test.getText().toString().equals("未上线"))
                    {
                        return;
                    }
                    //修改按钮颜色
                    if (main_draw.getVisibility() == VISIBLE)
                    {
                        Toast.makeText(AudioTeachActivity.this, "请先主动关闭乐谱后再进行切换", Toast.LENGTH_SHORT).show();
                    }
                    else if (container.getVisibility() == VISIBLE){
                        Toast.makeText(AudioTeachActivity.this, "请先主动关闭视频后再进行切换", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        updateTeachingStudent(test);
                    }
                }
            });
        }
    }

    public void updateTeachStatus(String message)
    {
        TextView textView = (TextView) findViewById(R.id.who_be_teach);
        textView.setText(message);
    }

    public void updateTeachingStudent(Button test){
        final LinearLayout weekLinearLayout = (LinearLayout) findViewById(R.id.all_student);
        for (int m = 0; m < weekLinearLayout.getChildCount(); m++) {
            Button stu_name = (Button) weekLinearLayout.getChildAt(m);
            String no_student = "未上线";
            if (test.getText().toString().equals(no_student) == false) {
                if (test.getText().toString().equals(stu_name.getText().toString())) {
                    FrameLayout container = (FrameLayout) findViewById(R.id.local_video_view_container);
                    joinInNewRoom(m);
                    String student_teach_name = "正在和"+test.getText().toString()+"语音教学";
                    updateTeachStatus(student_teach_name);
                    String showHandupInfoText = showHandupInfo.getText().toString();
                    if (showHandupInfoText.length() > 4)
                    {
                        String currentHandUp = showHandupInfoText.substring(0,showHandupInfoText.length() - 4);
                        if (currentHandUp.equals(test.getText()))
                        {
                            showHandupInfo.setText("");
                        } else if (currentHandUp.contains(test.getText().toString())) {
                            String[] arrCurrentHandUp = currentHandUp.split(",");
                            currentHandUp = "";
                            for (int n = 0; n < arrCurrentHandUp.length; n ++)
                            {
                                if (!arrCurrentHandUp[n].equals(test.getText().toString()))
                                {
                                    currentHandUp += arrCurrentHandUp[n]+",";
                                }
                            }
                            currentHandUp = currentHandUp.substring(0,currentHandUp.length()-1);
                            showHandupInfo.setText(currentHandUp + "正在举手");

                        }
                    }
                    container.setVisibility(GONE);
                    Button button = (Button) findViewById(stu_name.getId());
                    Drawable drawable1 = getResources().getDrawable(R.drawable.start_audio);
                    drawable1.setBounds(0, 0, 40, 40);//第一0是距左边距离，第二0是距上边距离，40分别是长宽
                    button.setCompoundDrawables(null, null, drawable1, null);//只放右边边
                    stu_name.setBackground(getResources().getDrawable(R.drawable.teach_stu_name_new_color));

                } else {
                    if (!showHandupInfo.getText().toString().contains(stu_name.getText()))
                    {
                        stu_name.setBackground(getResources().getDrawable(R.drawable.teach_stu_name_old_color));
                        if (stu_name.getText().toString().equals(no_student) == false) {
                            Button button = (Button) findViewById(stu_name.getId());
                            Drawable drawable1 = getResources().getDrawable(R.drawable.no_start_audio);
                            drawable1.setBounds(0, 0, 40, 40);//第一0是距左边距离，第二0是距上边距离，40分别是长宽
                            button.setCompoundDrawables(null, null, drawable1, null);//只放右边边
                        }
                    }
                }
            }else
            {
                stu_name.setBackground(getResources().getDrawable(R.drawable.teach_stu_name_old_color));
            }
        }
    }

    public void setTeachingFristStudent() {
        Button button = (Button) findViewById(R.id.join_first_btn);
        String no_student = "未上线";
        if (button.getText().toString().equals(no_student)) {
            button.setBackground(getResources().getDrawable(R.drawable.teach_stu_name_old_color));
            button.setTextColor(Color.WHITE);
            button.setCompoundDrawables(null, null, null, null);//只放右边边
        } else {
            button.setBackground(getResources().getDrawable(R.drawable.teach_stu_name_new_color));
            button.setTextColor(Color.WHITE);
            Button join_first_btn = (Button) findViewById(R.id.join_first_btn);
            Drawable drawable1 = getResources().getDrawable(R.drawable.start_audio);
            drawable1.setBounds(0, 0, 40, 40);//第一0是距左边距离，第二0是距上边距离，40分别是长宽
            join_first_btn.setCompoundDrawables(null, null, drawable1, null);//只放右边边
        }
    }

    public void stu_audio_icon_init() {
        final LinearLayout weekLinearLayout = (LinearLayout) findViewById(R.id.all_student);
        for (int i = 0; i < weekLinearLayout.getChildCount(); i++) {
            Button btn = (Button) weekLinearLayout.getChildAt(i);
            Button btn_id = (Button) findViewById(btn.getId());
            TextView textView = (TextView) findViewById(R.id.who_be_teach);
            String no_student = "未上线";
            if (no_student.equals(btn_id.getText().toString())) {
                Button button = (Button) findViewById(btn.getId());
                button.setBackground(getResources().getDrawable(R.drawable.teach_stu_name_old_color));

            } else {
                Button button = (Button) findViewById(btn.getId());
                Drawable drawable1 = getResources().getDrawable(R.drawable.no_start_audio);
                drawable1.setBounds(0, 0, 40, 40);//第一0是距左边距离，第二0是距上边距离，40分别是长宽
                button.setCompoundDrawables(null, null, drawable1, null);//只放右边边
            }


        }
    }
public void  set_teaching_student(){
    final LinearLayout weekLinearLayout = (LinearLayout) findViewById(R.id.all_student);
//    for (int i = 0; i < weekLinearLayout.getChildCount(); i++) {
        Button btn = (Button) weekLinearLayout.getChildAt(0);
        Button btn_id = (Button) findViewById(btn.getId());
        TextView textView = (TextView) findViewById(R.id.who_be_teach);
        String no_student = "未上线";
        if (no_student.equals(btn_id.getText().toString())) {
                String student_teach_name = "现在没有学生上线,请等待学生上线";
                textView.setText(student_teach_name);

        } else {
            String student_teach_name = "正在和"+getUserName(0)+"语音教学";
            updateTeachStatus(student_teach_name);
        }

}
        public class CustomMessageHandler extends AVIMMessageHandler {
            //即时通讯
            //接收到消息后的处理逻辑
            public Boolean mIsOpen;
            public void setIsOpen(Boolean isOpen)
            {
                mIsOpen = isOpen;
            }
            @Override
            public void onMessage(AVIMMessage message, AVIMConversation conversation, AVIMClient client) {
                if (mIsOpen == false) {
                    return;
                }
                if (message instanceof AVIMTextMessage) {
                    String fromStudentID = message.getFrom();
                    Boolean isInCourseInfo = false;
                    for (int m = 0; m < mArrStudentInfo.length(); m ++)
                    {
                        try {
                            JSONObject stu_info = mArrStudentInfo.getJSONObject(m);
                            if (stu_info.getString("studentID").equals(fromStudentID))
                            {
                                isInCourseInfo = true;
                                break;
                            }
                        }catch (JSONException e)
                        {

                        }
                    }
                    if (isInCourseInfo == false)
                    {
                        Log.e("===","不是老师的学生");
                        return;
                    }

                    Log.e("Tom & Jerry", "消息接听:" + ((AVIMTextMessage) message).getText());
                    if (((AVIMTextMessage) message).getText().equals("成功收到老师上线通知"))
                    {
                        updateOnlineStudent(message);
                    }
                    else if (((AVIMTextMessage) message).getText().equals("HandUp"))
                    {
                        //举手回调
                        for (int i = 0; i < mArrJoinStudentInfo.length(); i ++)
                        {
                            try {
                                JSONObject stu_info = mArrJoinStudentInfo.getJSONObject(i);
                                if (stu_info.getString("studentID").equals(fromStudentID))
                                {
                                    LinearLayout all_student_names = (LinearLayout) findViewById(R.id.all_student);
                                    Button hand_button = (Button)all_student_names.getChildAt(i);
                                    Drawable drawable1 = getResources().getDrawable(R.drawable.show_hands);
                                    drawable1.setBounds(0, 0, 60, 60);//第一0是距左边距离，第二0是距上边距离，40分别是长宽
                                    hand_button.setCompoundDrawables(null, null, drawable1, null);//只放右边边
                                    hand_button.setBackgroundColor(Color.parseColor("#DF8931"));

                                    String showHandupInfoText = showHandupInfo.getText().toString();
                                    Log.e("count",showHandupInfoText.length() +"");

                                    if (showHandupInfoText.length() == 0)
                                    {
                                        showHandupInfo.setText(stu_info.getString("name")+"正在举手");
                                    }
                                    else if (showHandupInfoText.length() > 4)
                                    {
                                        String handupNames = showHandupInfoText.substring(0,showHandupInfoText.length() - 4);
                                        if (!handupNames.contains(stu_info.getString("name")))
                                        {
                                            showHandupInfo.setText(handupNames + "," + stu_info.getString("name")+"正在举手");
                                        }
                                    }
                                }
                            }catch (JSONException e)
                            {

                            }
                        }
                    }
                    else if (((AVIMTextMessage) message).getText().equals("studentOnline"))
                    {
                        AVIMTextMessage msg = new AVIMTextMessage();
                        msg.setText("收到学生上线通知");
                        // 发送消息
                        conversation.sendMessage(msg, new AVIMConversationCallback() {
                            @Override
                            public void done(AVIMException e) {
                                if (e == null) {
                                    Log.e("Tom & Jerry", "收到学生上线通知 发送成功！");
                                }
                            }
                        });
                        updateOnlineStudent(message);
                    }
                    else if (((AVIMTextMessage) message).getText().equals("studentOffline"))
                    {
                        //学生下线ID
                        for (int i = 0; i < mArrJoinStudentInfo.length(); i ++)
                        {
                            try {
                                JSONObject stu_info = mArrJoinStudentInfo.getJSONObject(i);
                                if (stu_info.getString("studentID").equals(fromStudentID))
                                {
                                    updateHandUpStatus(stu_info.getString("name"));
                                    LinearLayout all_student_names = (LinearLayout) findViewById(R.id.all_student);
                                    Button join_button = (Button)all_student_names.getChildAt(i);
                                    mArrJoinStudentInfo.remove(i);
                                    if (Channel_name.equals(fromStudentID) || mArrJoinStudentInfo.length() > 0)
                                    {
                                        leaveChannel();
                                        Channel_name = mArrJoinStudentInfo.length() == 0 ? "channel":mArrJoinStudentInfo.getJSONObject(0).getString("studentID");
                                        if (mArrJoinStudentInfo.length() > 0)
                                        {
                                            updateStudentNamesInTop();
                                            Button first_button = (Button)all_student_names.getChildAt(0);
                                            first_button.setText(mArrJoinStudentInfo.getJSONObject(0).getString("name"));
                                            updateTeachingStudent(first_button);
                                        }
                                        else
                                        {
                                            updateStudentNamesInTop();
                                            stu_audio_icon_init();
                                            updateTeachStatus("学生已下线");
                                        }
                                    }
                                    else
                                    {
                                            join_button.setText("未上线");
                                    }
                                    break;

                                }
                            }catch (JSONException e)
                            {

                            }
                        }
                    }
                }
            }

            public void onMessageReceipt(AVIMMessage message, AVIMConversation conversation, AVIMClient client) {

            }
        }

        public void updateHandUpStatus(String name)
        {
            String showHandupInfoText = showHandupInfo.getText().toString();
            if (showHandupInfoText.length() > 4)
            {
                String handupNames = showHandupInfoText.substring(0,showHandupInfoText.length() - 4);
                if (handupNames.equals(name))
                {
                    showHandupInfo.setText("");
                }
                else if (handupNames.contains(name))
                {
                    String[] arrCurrentHandUp = handupNames.split(",");
                    handupNames = "";
                    for (int n = 0; n < arrCurrentHandUp.length; n ++)
                    {
                        if (!arrCurrentHandUp[n].equals(name))
                        {
                            handupNames += arrCurrentHandUp[n]+",";
                        }
                    }
                    handupNames = handupNames.substring(0,handupNames.length()-1);
                    showHandupInfo.setText(handupNames + "," + name+"正在举手");
                }
            }
        }

        public void updateStudentNamesInTop()
        {
            final LinearLayout weekLinearLayout = (LinearLayout) findViewById(R.id.all_student);
            for (int m = 0; m < weekLinearLayout.getChildCount(); m++) {
                Button stu_name_btn = (Button) weekLinearLayout.getChildAt(m);
                String no_student = "未上线";
                try
                {
                    stu_name_btn.setText(mArrJoinStudentInfo.getJSONObject(m).getString("name"));
                }catch (JSONException e) {
                    stu_name_btn.setText(no_student);
                    stu_name_btn.setBackground(getResources().getDrawable(R.drawable.teach_stu_name_old_color));
                    stu_name_btn.setTextColor(Color.WHITE);
                    stu_name_btn.setCompoundDrawables(null, null, null, null);//只放右边边
                }
            }
        }

        public void updateOnlineStudent(AVIMMessage message){
            //学生上线ID
            String fromStudentID = message.getFrom();
            for (int m = 0; m < mArrJoinStudentInfo.length(); m ++)
            {
                try {
                    JSONObject stu_info = mArrJoinStudentInfo.getJSONObject(m);
                    if (stu_info.getString("studentID").equals(fromStudentID))
                    {
                        mArrJoinStudentInfo.remove(m);
                        break;
                    }
                }catch (JSONException e)
                {

                }
            }
            for (int i = 0; i < mArrStudentInfo.length(); i++)
            {
                try {
                    JSONObject stu_info = mArrStudentInfo.getJSONObject(i);
                    if (stu_info.getString("studentID").equals(fromStudentID))
                    {
                        LinearLayout all_student_names = (LinearLayout) findViewById(R.id.all_student);
                        Button join_button = (Button)all_student_names.getChildAt(mArrJoinStudentInfo.length());
                        mArrJoinStudentInfo.put(stu_info);
                        join_button.setText(stu_info.getString("name"));
                        if (mArrJoinStudentInfo.length() == 1)
                        {
                            Channel_name = mArrJoinStudentInfo.getJSONObject(0).getString("studentID");
                            joinChannel(9998);
                            setTeachingFristStudent();
                            //初始化与谁视频教学
                            set_teaching_student();
                        }
                        break;

                    }
                }catch (JSONException e)
                {

                }
            }
        }


    public void sendMessageToStudents (String msgName,final String msgtext) {
        List<String> list = new ArrayList();
        for (int i = 0; i < mArrStudentInfo.length(); i++) {
            try {
                list.add(mArrStudentInfo.getJSONObject(i).getString("studentID"));
            } catch (JSONException e) {

            }
        }
        Log.e("学生列表list", list.toString());
        myApp.client.createConversation(list, msgName, null, new AVIMConversationCreatedCallback() {
                    @Override
                    public void done(AVIMConversation conversation, AVIMException e) {
                        if (e == null) {
                            AVIMTextMessage msg = new AVIMTextMessage();
                            msg.setText(msgtext);
                            // 发送消息
                            conversation.sendMessage(msg, new AVIMConversationCallback() {
                                @Override
                                public void done(AVIMException e) {
                                    if (e == null) {
                                        Log.e(msgtext, "发送成功！");
                                    }
                                }
                            });
                        }
                    }
        });
    }
    public void sendPauseMessageToCurrentStudent(String studentID,String msgName,final String msgtext)
    {
        myApp.client.createConversation(Arrays.asList(studentID), msgName, null, new AVIMConversationCreatedCallback() {
            @Override
            public void done(AVIMConversation conversation, AVIMException e) {
                if (e == null) {
                    AVIMTextMessage msg = new AVIMTextMessage();
                    msg.setText(msgtext);
                    // 发送消息
                    conversation.sendMessage(msg, new AVIMConversationCallback() {
                        @Override
                        public void done(AVIMException e) {
                            if (e == null) {
                                Log.e(msgtext, "叫停事件发送成功！");
                            }
                        }
                    });
                }
            }
        });
    }
}


