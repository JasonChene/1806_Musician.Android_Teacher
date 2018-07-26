package com.example.macbookpro.musictrainerteacher;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.macbookpro.musictrainerteacher.CustomView.Draw;

//import com.netease.nical.rtsdemo.CustomView.Draw;


public class RTSMainActivity extends AppCompatActivity {

    private Button sendData;
    private Button clear;
    private Draw drawView;
    private String sessionId;
    private String toaccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtsmain);



        initView();     //初始化视图
        Intent intent = getIntent();        //获取上一个activity传递的参数
        sessionId = intent.getStringExtra("sessionId");
        toaccount = intent.getStringExtra("toAccount");
        drawView.sessionID = sessionId;     //参数传递
        drawView.toAccount = toaccount;     //参数传递

        //注册收到数据的监听
        WhiteBoardManager.registerIncomingData(sessionId,true, drawView);

        WhiteBoardManager.registerRTSCloseObserver(sessionId,true,RTSMainActivity.this);

        //挂断按钮的点击事件
        sendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //执行挂断
                WhiteBoardManager.close(sessionId,RTSMainActivity.this);
            }
        });
        //清除界面的点击事件
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.Clear();
            }
        });

    }

    /**
     * 注销监听
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //注销收数据监听
        WhiteBoardManager.registerIncomingData(sessionId,false, drawView);
        //注销挂断监听
        WhiteBoardManager.registerRTSCloseObserver(sessionId,false,RTSMainActivity.this);
    }

    /**
     * 初始化视图
     */
    private void initView(){
        sendData = findViewById(R.id.senddata);
        clear = findViewById(R.id.clear);
        drawView = findViewById(R.id.main_draw);
    }
}
