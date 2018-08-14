package com.example.macbookpro.musictrainerteacher;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.SDKOptions;

public class MyLeanCloudApp extends Application {

    public Boolean isRegisterRTSIncomingCallObserver = false;
    public Context currentContext = null;
    public AVIMClient client = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("MyLeanCloudApp","onCreate");
        // 初始化参数依次为 this, AppId, AppKey
        AVOSCloud.initialize(this,"dqozOWhkl50Xh5HQyfeFkDxV-gzGzoHsz","S6kknLSdUpaztxebGuLUMDUT");
        SDKOptions options = new SDKOptions();
        options.appKey = "34b421cf05779d2ddcfe1a1ae66035d1";
        NIMClient.init(this, null, options);
        WhiteBoardManager.registerRTSIncomingCallObserver(true,this);

        AVUser currentUser = AVUser.getCurrentUser();
        if (currentUser != null) {
            Log.e("TAg===",currentUser.getUsername());
            client = AVIMClient.getInstance(currentUser.getUsername());
            // 与服务器连接
            client.open(new AVIMClientCallback() {
                @Override
                public void done(AVIMClient client, AVIMException e) {
                    if (e == null) {
                        Log.e("MyLeanCloudAppTAg","leancloud 即时消息打开成功");
                    }
                    else
                    {
                        Log.e("MyLeanCloudAppTAg","leancloud 即时消息打开失败"+e.toString());
                    }
                }
            });
        }
    }
    public Boolean getIsRegisterRTSIncomingCallObserver() {
        return isRegisterRTSIncomingCallObserver;
    }

    public void setIsRegisterRTSIncomingCallObserver(Boolean RegisterRTSIncomingCallObserver) {
        this.isRegisterRTSIncomingCallObserver = RegisterRTSIncomingCallObserver;
    }
    public void setAudioTeachActivity(Context context)
    {
        currentContext = context;
    }

}
