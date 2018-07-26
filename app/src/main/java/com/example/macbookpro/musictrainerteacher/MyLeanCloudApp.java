package com.example.macbookpro.musictrainerteacher;

import android.app.Application;

import com.avos.avoscloud.AVOSCloud;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.SDKOptions;

public class MyLeanCloudApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化参数依次为 this, AppId, AppKey
        AVOSCloud.initialize(this,"dqozOWhkl50Xh5HQyfeFkDxV-gzGzoHsz","S6kknLSdUpaztxebGuLUMDUT");
        SDKOptions options = new SDKOptions();
        options.appKey = "1ee5a51b7d008254cd73b1d4369a9494";
        NIMClient.init(this, null, options);
    }
}
