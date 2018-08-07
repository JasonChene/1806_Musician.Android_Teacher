package com.example.macbookpro.musictrainerteacher;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVSMS;
import com.avos.avoscloud.AVSMSOption;
import com.avos.avoscloud.RequestMobileCodeCallback;
import com.example.macbookpro.musictrainerteacher.common.SysExitUtil;

public class LoginActivity extends AppCompatActivity {
    EditText phone_number;
    EditText verification_code;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SysExitUtil.activityList.add(LoginActivity.this);
        initActionBar();
        phone_number = (EditText)findViewById(R.id.phone_number);

        Button next_button = (Button)findViewById(R.id.next_button);
        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //为了防止调试时总是获取验证码 可以使用下面注释的代码 调试完成 发布时删掉
//                Intent intent = new Intent(LoginActivity.this, VerificationCodeActivity.class);
//                // 在Intent中传递数据
//                intent.putExtra("phoneNumber", phone_number.getText() + "" );
//                // 启动Intent
//                startActivity(intent);

                AVOSCloud.requestSMSCodeInBackground(phone_number.getText() + "", new RequestMobileCodeCallback() {
                    @Override
                    public void done(AVException e) {
                        if (null == e) {
                            Log.e("e","请求成功");
                            Intent intent = new Intent(LoginActivity.this, VerificationCodeActivity.class);
                            // 在Intent中传递数据
                            intent.putExtra("phoneNumber", phone_number.getText() + "");
                            // 启动Intent
                            startActivity(intent);
                            /* 请求成功 */
                        } else {
                            /* 请求失败 */
                            Log.e("e","请求失败" + e.getMessage());
                            if (e.getMessage().equals("java.net.NoRouteToHostException: No route to host"))
                            {
                                Toast.makeText(LoginActivity.this, "网络链接异常，请检查本地网络", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

            }
        });
    }
    public  void  initActionBar(){
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            LayoutInflater inflator = (LayoutInflater) this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflator.inflate(R.layout.actionbar_main,new LinearLayout(LoginActivity.this),false);
            android.support.v7.app.ActionBar.LayoutParams layout = new android.support.v7.app.ActionBar.LayoutParams(
                    android.support.v7.app.ActionBar.LayoutParams.MATCH_PARENT, android.support.v7.app.ActionBar.LayoutParams.MATCH_PARENT);
            actionBar.setCustomView(v, layout);
            Toolbar parent = (Toolbar) v.getParent();
            parent.setContentInsetsAbsolute(0, 0);
        }
        TextView actionBarTitle = (TextView) findViewById(R.id.action_bar_title);
        actionBarTitle.setText("手机验证码登录");
    }
    private long time = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - time > 1000)) {
                Toast.makeText(this, "请重新登陆,再按一次返回桌面", Toast.LENGTH_SHORT).show();
                time = System.currentTimeMillis();
            } else {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }
}
