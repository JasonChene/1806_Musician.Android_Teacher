package com.example.macbookpro.musictrainerteacher;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVMobilePhoneVerifyCallback;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.RequestMobileCodeCallback;
import com.example.macbookpro.musictrainerteacher.common.SysExitUtil;
import com.tuo.customview.VerificationCodeView;

public class VerificationCodeActivity  extends AppCompatActivity {
    String phone_number;
    String verification_code;
    private VerificationCodeView icv;
    TextView countDownTimerTextView;

    CountDownTimer countDownTimer;
    Button reverification_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_code);
        SysExitUtil.activityList.add(VerificationCodeActivity.this);

        countDownTimerTextView = findViewById(R.id.count_down_timer);
        countDownTimer = new CountDownTimer(60*1000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.e("millisUntilFinished",millisUntilFinished+"======");
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

        reverification_button = (Button)findViewById(R.id.reverification_button);
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
                            Log.e("e","请求成功");
                        } else {
                            /* 请求失败 */
                            Log.e("e","请求失败" + e.getMessage());
                        }
                    }
                });
            }
        });



        Intent intent = getIntent();
        phone_number = intent.getStringExtra("phoneNumber");

        TextView show_phone_number = (TextView) findViewById(R.id.show_phone_number);
        show_phone_number.setText("验证码已发送至"+phone_number);


        Button confirm_login_button = (Button)findViewById(R.id.confirm_login_button);
        confirm_login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (verification_code.length() == 6)
                {
                    //请求登录接口
                    AVOSCloud.verifySMSCodeInBackground(verification_code, phone_number, new AVMobilePhoneVerifyCallback() {
                        @Override
                        public void done(AVException e) {
                            if (null == e) {
                                Log.e("e","登录请求成功");
                                Intent intent = new Intent(VerificationCodeActivity.this, MainActivity.class);
                                // 在Intent中传递数据
//                                intent.putExtra("phoneNumber", phone_number.getText() + "");
                                // 启动Intent
                                startActivity(intent);
                                /* 请求成功 */
                            } else {
                                /* 请求失败 */
                                Log.e("e","登录请求失败" + e.getMessage());
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
            return  "0" + second;
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