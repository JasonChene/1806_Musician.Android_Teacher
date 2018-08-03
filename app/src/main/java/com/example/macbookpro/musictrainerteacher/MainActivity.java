package com.example.macbookpro.musictrainerteacher;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.LinearGradient;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVUser;
import com.example.macbookpro.musictrainerteacher.common.SysExitUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;


import org.json.JSONException;
import org.json.JSONObject;

import static com.netease.nimlib.sdk.StatusCode.LOGINED;

public class MainActivity extends AppCompatActivity {

    Boolean isLoginEaseSuccess = false;

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }
    public void startLoginEase()
    {
        if (NIMClient.getStatus() != LOGINED)
        {
            NIMClient.getService(AuthService.class).logout();
            AVUser currentUser = getCurrentUser();
            if (currentUser != null)
            {
                try {
                    JSONObject netEaseUserInfo = new JSONObject(currentUser.get("netEaseUserInfo").toString());
                    LoginInfo info = new LoginInfo(netEaseUserInfo.getString("accid"),netEaseUserInfo.getString("token"));
                    NIMClient.getService(AuthService.class).login(info)
                            .setCallback(new RequestCallback() {
                                @Override
                                public void onSuccess(Object param) {
                                    Toast.makeText(MainActivity.this,"白板登录成功",Toast.LENGTH_SHORT);
                                    Log.e("TAG","白板登录成功");
                                    isLoginEaseSuccess = true;
                                }

                                @Override
                                public void onFailed(int code) {
                                    Toast.makeText(MainActivity.this,"白板登录失败"+code,Toast.LENGTH_SHORT);
                                    Log.e("TAG","白板登录失败"+code);
                                    isLoginEaseSuccess = false;
                                }

                                @Override
                                public void onException(Throwable exception) {
                                    Log.e("TAG","login: onException");
                                    Toast.makeText(MainActivity.this,"白板登录异常失败",Toast.LENGTH_SHORT);
                                    isLoginEaseSuccess = false;

                                }
                            });
                }catch (JSONException e)
                {
                }
            }
        }
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SysExitUtil.activityList.add(MainActivity.this);
        initActionBar();
        Button login_button = (Button) findViewById(R.id.login_button);
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
        Button room_button = (Button) findViewById(R.id.login_room);
        room_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AudioTeachActivity.class));
            }
        });
        startLoginEase();
    }

    public  void  initActionBar(){
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            LayoutInflater inflator = (LayoutInflater) this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflator.inflate(R.layout.actionbar_main,new LinearLayout(MainActivity.this),false);
            android.support.v7.app.ActionBar.LayoutParams layout = new android.support.v7.app.ActionBar.LayoutParams(
                    android.support.v7.app.ActionBar.LayoutParams.MATCH_PARENT, android.support.v7.app.ActionBar.LayoutParams.MATCH_PARENT);
            actionBar.setCustomView(v, layout);
            Toolbar parent = (Toolbar) v.getParent();
            parent.setContentInsetsAbsolute(0, 0);
        }
        TextView actionBarTitle = (TextView) findViewById(R.id.action_bar_title);
        actionBarTitle.setText("老师课程表");
    }

    //双击退回手机主页面
    private long time = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - time > 1000)) {
                Toast.makeText(this, "再按一次返回桌面", Toast.LENGTH_SHORT).show();
                time = System.currentTimeMillis();
            } else {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
                NIMClient.getService(AuthService.class).logout();
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }

    public AVUser getCurrentUser(){
        AVUser currentUser = AVUser.getCurrentUser();
        if (currentUser != null) {
            Log.e("e", "+++++=========+++++" +currentUser.get("netEaseUserInfo"));

        } else {
            //缓存用户对象为空时，可打开用户注册界面…
            Log.e("e", "+++++=========+++++" );
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
        return currentUser;

    }

}

