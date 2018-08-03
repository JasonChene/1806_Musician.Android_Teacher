package com.example.macbookpro.musictrainerteacher;

import android.app.Application;
import android.content.Context;

import com.avos.avoscloud.AVOSCloud;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.SDKOptions;

public class MyLeanCloudApp extends Application {

    public Boolean isRegisterRTSIncomingCallObserver = false;
    public Context currentContext = null;

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化参数依次为 this, AppId, AppKey
        AVOSCloud.initialize(this,"dqozOWhkl50Xh5HQyfeFkDxV-gzGzoHsz","S6kknLSdUpaztxebGuLUMDUT");
        SDKOptions options = new SDKOptions();
        options.appKey = "34b421cf05779d2ddcfe1a1ae66035d1";
        NIMClient.init(this, null, options);
        WhiteBoardManager.registerRTSIncomingCallObserver(true,this);
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
