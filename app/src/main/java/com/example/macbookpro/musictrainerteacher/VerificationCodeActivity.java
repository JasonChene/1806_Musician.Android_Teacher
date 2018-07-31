package com.example.macbookpro.musictrainerteacher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.IDNA;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.AVCallback;
import com.avos.avoscloud.AVCloud;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVMobilePhoneVerifyCallback;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVRelation;
import com.avos.avoscloud.AVRole;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.FunctionCallback;
import com.avos.avoscloud.LogInCallback;
import com.avos.avoscloud.RequestMobileCodeCallback;
import com.example.macbookpro.musictrainerteacher.common.SysExitUtil;
import com.tuo.customview.VerificationCodeView;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.callback.Callback;

import static android.icu.util.Calendar.YEAR;
import static android.icu.util.HebrewCalendar.AV;
import static com.example.macbookpro.musictrainerteacher.storage.LocalStorage.saveObject;

public class VerificationCodeActivity extends AppCompatActivity {
    String phone_number;
    String verification_code;
    private VerificationCodeView icv;
    TextView countDownTimerTextView;

    CountDownTimer countDownTimer;
    Button reverification_button;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_code);
        SysExitUtil.activityList.add(VerificationCodeActivity.this);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        initActionBar();
        countDownTimerTextView = findViewById(R.id.count_down_timer);
        countDownTimer = new CountDownTimer(60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                countDownTimerTextView.setText(formatTime(millisUntilFinished) + "s后重新获取");
            }

            @Override
            public void onFinish() {
                //显示重新获取验证码
                countDownTimerTextView.setVisibility(View.GONE);
                reverification_button.setVisibility(View.VISIBLE);
            }
        };

        countDownTimer.start();

        reverification_button = (Button) findViewById(R.id.reverification_button);
        reverification_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reverification_button.setVisibility(View.GONE);
                countDownTimerTextView.setText("60s后重新获取");
                countDownTimerTextView.setVisibility(View.VISIBLE);
                countDownTimer.start();

                AVOSCloud.requestSMSCodeInBackground(phone_number, new RequestMobileCodeCallback() {
                    @Override
                    public void done(AVException e) {
                        if (null == e) {
                            Log.e("e", "请求成功");
                        } else {
                            /* 请求失败 */
                            Log.e("e", "请求失败" + e.getMessage());
                        }
                    }
                });
            }
        });


        Intent intent = getIntent();
        phone_number = intent.getStringExtra("phoneNumber");

        TextView show_phone_number = (TextView) findViewById(R.id.show_phone_number);
        show_phone_number.setText("验证码已发送至" + phone_number);


        Button confirm_login_button = (Button) findViewById(R.id.confirm_login_button);
        confirm_login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (verification_code.length() == 6) {
                    //请求登录接口
                    AVUser.signUpOrLoginByMobilePhoneInBackground(phone_number, verification_code, new LogInCallback<AVUser>() {
                        @Override
                        public void done(final AVUser avUser, AVException e) {
                            Log.e("e", "" + avUser);
                            if (null == e) {
                                Log.e("e", "登录请求成功");

                                if (!avUser.containsKey("netEaseUserInfo"))
                                {
                                    Log.e("TAG","=======netEaseUserInfo=======");
                                    Map<String, String> dicParameters = new HashMap<String, String>();
                                    dicParameters.put("role", "teacher");
                                    //云函数设置角色
                                    AVCloud.callFunctionInBackground("mobileSetRole", dicParameters, new FunctionCallback() {
                                        public void done(Object object, AVException e) {
                                            Log.e("hahah",object.toString());
                                            try {
                                                JSONObject netEaseUserInfo = new JSONObject(object.toString());
                                                if (e == null && Integer.parseInt(netEaseUserInfo.get("status").toString())== 200) {
                                                    try {
                                                        avUser.put("netEaseUserInfo",netEaseUserInfo.get("data"));
                                                        AVUser.changeCurrentUser(avUser,true);
                                                        startActivity(new Intent(VerificationCodeActivity.this, MainActivity.class));
                                                    }catch (JSONException error)
                                                    {

                                                    }

                                                    // 处理返回结果
                                                } else {
                                                    // 处理报错
                                                }
                                            }catch (JSONException err)
                                            {

                                            }

                                        }
                                    });
                                }
                                else
                                {
                                    //角色验证
                                    try {
                                        List<AVRole> user_roles = avUser.getRoles();
                                        boolean allRoes = false;
                                        for (int i = 0; i < user_roles.size(); i++) {
                                            AVRole role = user_roles.get(i);
                                            if (role.getName().equals("teacher")) {
                                                allRoes = true;
                                                break;
                                            }
                                        }
                                        if (allRoes == true) {
                                            startActivity(new Intent(VerificationCodeActivity.this, MainActivity.class));
                                        } else {
                                            Toast.makeText(VerificationCodeActivity.this, "没有授权继续操作", Toast.LENGTH_SHORT).show();
                                        }

                                    } catch (Exception error) {
                                        Log.e("error", "===========" + error);

                                    }
                                }
                            } else {
                                /* 请求失败 */
                                Log.e("e", "登录请求失败" + e.getMessage());
                            }
                        }
                    });

                }
            }
        });


        icv = (VerificationCodeView) findViewById(R.id.icv);
        icv.setInputCompleteListener(new VerificationCodeView.InputCompleteListener() {
            @Override
            public void inputComplete() {
                verification_code = icv.getInputContent();
            }

            @Override
            public void deleteContent() {
                verification_code = icv.getInputContent();
            }
        });
    }


    public void initActionBar() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            LayoutInflater inflator = (LayoutInflater) this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflator.inflate(R.layout.actionbar_main, new LinearLayout(VerificationCodeActivity.this), false);
            android.support.v7.app.ActionBar.LayoutParams layout = new android.support.v7.app.ActionBar.LayoutParams(
                    android.support.v7.app.ActionBar.LayoutParams.MATCH_PARENT, android.support.v7.app.ActionBar.LayoutParams.MATCH_PARENT);
            actionBar.setCustomView(v, layout);
            Toolbar parent = (Toolbar) v.getParent();
            parent.setContentInsetsAbsolute(0, 0);
        }
        TextView actionBarTitle = (TextView) findViewById(R.id.action_bar_title);
        actionBarTitle.setText("手机验证");
    }

    /**
     * 将毫秒转化为 分钟：秒 的格式
     *
     * @param millisecond 毫秒
     * @return
     */
    public String formatTime(long millisecond) {
        int second;//秒数
        second = (int) ((millisecond / 1000) % 60);
        if (second < 10) {
            return "0" + second;
        } else {
            return "" + second;
        }
    }

    /**
     * 取消倒计时
     */
    public void timerCancel() {
        countDownTimer.cancel();
    }

    /**
     * 开始倒计时
     */
    public void timerStart() {
        countDownTimer.start();
    }
}