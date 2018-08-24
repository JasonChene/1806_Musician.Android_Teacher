package com.example.macbookpro.musictrainerteacher;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.icu.text.RelativeDateTimeFormatter;
import android.text.LoginFilter;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.macbookpro.musictrainerteacher.CustomView.Draw;
import com.example.macbookpro.musictrainerteacher.manager.DataManager;

import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.rts.RTSCallback;
import com.netease.nimlib.sdk.rts.RTSManager;
import com.netease.nimlib.sdk.rts.constant.RTSEventType;
import com.netease.nimlib.sdk.rts.model.RTSCalleeAckEvent;
import com.netease.nimlib.sdk.rts.model.RTSCommonEvent;
import com.netease.nimlib.sdk.rts.model.RTSData;
import com.netease.nimlib.sdk.rts.model.RTSTunData;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.security.acl.LastOwnerException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 白板能力类
 */

public class WhiteBoardManager {

    /**
     * 发起方收到被叫相应的回调
     * @param sessionId
     * @param register
     * @param toAccount
     * @param context
     */
//    AudioTeachActivity　Button;
//    public static void registerCalleeAckNotification(final String sessionId, Boolean register, final String toAccount, final Context context){
//
//        Observer<RTSCalleeAckEvent> calleeAckEventObserver = new Observer<RTSCalleeAckEvent>() {
//            @Override
//            public void onEvent(RTSCalleeAckEvent rtsCalleeAckEvent) {
//                if (rtsCalleeAckEvent.getEvent() == RTSEventType.CALLEE_ACK_AGREE) {
//                    // 判断SDK自动开启通道是否成功
//                    if (!rtsCalleeAckEvent.isTunReady()) {
//                        return;
//                    }
//                    // 进入会话界面
//                    Toast.makeText(context, "接听成功", Toast.LENGTH_SHORT).show();
//                    AudioTeachActivity activity = (AudioTeachActivity)context;
//                    activity.startKeepUpBoard(sessionId,toAccount);
//
//                } else if (rtsCalleeAckEvent.getEvent() == RTSEventType.CALLEE_ACK_REJECT) {
//                    // 被拒绝，结束会话
//                    Toast.makeText(context, "对方已经拒绝白板请求", Toast.LENGTH_SHORT).show();
//                }
//            }
//        };
//        RTSManager.getInstance().observeCalleeAckNotification(sessionId, calleeAckEventObserver, register);
//    }
    public static void registerCalleeAckNotification(final String sessionId, Boolean register, final String toAccount, final Context context){

        Observer<RTSCalleeAckEvent> calleeAckEventObserver = new Observer<RTSCalleeAckEvent>() {
            @Override
            public void onEvent(RTSCalleeAckEvent rtsCalleeAckEvent) {
                if (rtsCalleeAckEvent.getEvent() == RTSEventType.CALLEE_ACK_AGREE) {
                    // 判断SDK自动开启通道是否成功
                    if (!rtsCalleeAckEvent.isTunReady()) {
                        return;
                    }
                    // 进入会话界面
//                    Toast.makeText(context, "接听成功", Toast.LENGTH_SHORT).show();
//                    AudioTeachActivity activity = (AudioTeachActivity)context;
//                    activity.startKeepUpBoard(sessionId,toAccount);

                } else if (rtsCalleeAckEvent.getEvent() == RTSEventType.CALLEE_ACK_REJECT) {
                    // 被拒绝，结束会话
                    Toast.makeText(context, "对方已经拒绝白板请求", Toast.LENGTH_SHORT).show();
                }
            }
        };
        RTSManager.getInstance().observeCalleeAckNotification(sessionId, calleeAckEventObserver, register);
    }

    /**
     * 收到白板请求的回调
     * @param register      是否注册
     * @param context       上下文（当前所处的Activity）
     */
    public static void registerRTSIncomingCallObserver(boolean register , final Context context) {
        RTSManager.getInstance().observeIncomingSession(new Observer<RTSData>() {
            @Override
            public void onEvent(final RTSData rtsData) {

                String sessionID = rtsData.getLocalSessionId();
                String fromAccount = rtsData.getAccount();
                long channelID = rtsData.getChannelId();
                MyLeanCloudApp app = (MyLeanCloudApp)context;

                Log.e("TAG",app.currentContext.getClass().toString());
                if (app.currentContext.getClass().toString().equals("class com.example.macbookpro.musictrainerteacher.AudioTeachActivity"))
                {
                    AudioTeachActivity audioTeachActivity = (AudioTeachActivity) app.currentContext;
                    accept(sessionID,fromAccount,channelID,audioTeachActivity);
//                    return;
                }
                else
                {
                    close(sessionID,(Activity) app.currentContext);
//                    return;
                }
            }
        },register);
    }
    /**
     * 白板接听
     * @param sessionID     本地白板会话ID
     * @param account       呼叫方账号
     * @param context       上下文（当前所处的Activity）
     */
    public static void accept(final String sessionID, final String account,final long channelID, final Context context){
        Toast.makeText(context, "开始接受白板请求", Toast.LENGTH_SHORT).show();
        //跳转到接听界面
        Log.e("Tag","==============收到白板请求："+channelID);
        RTSManager.getInstance().accept(sessionID, null, new RTSCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                Toast.makeText(context, "接听成功", Toast.LENGTH_SHORT).show();
                AudioTeachActivity activity = (AudioTeachActivity)context;
                activity.startKeepUpBoard(sessionID,account);
            }

            @Override
            public void onFailed(int i) {
                Toast.makeText(context, "接听白板失败，错误码"+i, Toast.LENGTH_SHORT).show();
                Log.e("e", "接听失败"+i);

            }

            @Override
            public void onException(Throwable throwable) {
                Toast.makeText(context, "接听白板异常", Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * 数据接收，注册收到的白板数据的回调
     * @param sessionId     本地白板会话ID
     * @param register      是否注册
     * @param draw          绘制类对象
     */
    public static void registerIncomingData(String sessionId, final boolean register, final Draw draw , final Context context){

        Observer<RTSTunData> receiveDataObserver = new Observer<RTSTunData>() {
            @Override
            public void onEvent(RTSTunData rtsTunData) {
                if (register == true)
                {
                    String data = "[parse bytes error]";
                    long timeStampSec = System.currentTimeMillis()/1000;
                    String time = String.format("%010d", timeStampSec);

                    try {
                        data = new String(rtsTunData.getData(), 0, rtsTunData.getLength(), "UTF-8");
                        String tag = "";
                        if (Integer.valueOf(data.substring(0,1)) == 1)
                        {
                            tag = "m";
                        }else if (Integer.valueOf(data.substring(0,1)) == 2)
                        {
                            tag = "l";
                        }
                        else if (Integer.valueOf(data.substring(0,1)) == 0)
                        {
                            tag = "p";
                        }


                        if (tag.equals("p"))
                        {
                            String[] strMusicImageUrl = data.split(":");
                            Log.e("tagstrMusicImageUrl","==============strMusicImageUrl:"+Arrays.toString(strMusicImageUrl));
                            AudioTeachActivity audioTeachActivity = (AudioTeachActivity) context;
                            audioTeachActivity.addMusicPic(strMusicImageUrl[1]+":"+strMusicImageUrl[2],strMusicImageUrl[3]+":"+strMusicImageUrl[4]);
                        }
                        else
                        {
                            DataManager dataManager = new DataManager();
                            dataManager.dataDecode(data);

                            String[] strPoint = data.split(";")[0].split(":")[1].split(",");
                            double x = Double.valueOf(strPoint[0]) *draw.getWidth();
                            double y = Double.valueOf(strPoint[1]) *draw.getHeight();
                            String newData = time +"," + x +"," + y + tag;
                            AudioTeachActivity audioTeachActivity = (AudioTeachActivity)context;
                            audioTeachActivity.addPeerData(newData);
                            if (draw != null)
                            {
                                draw.dataPaint(newData);
                            }
                            else {
                                Toast.makeText(context,"正在准备乐谱",Toast.LENGTH_SHORT);
                            }
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Boolean issuccess = RTSManager.getInstance().observeReceiveData(sessionId, receiveDataObserver, register);
        Log.e("TAG","注册数据监听"+issuccess);
    }

    /**
     * 获取时间戳
     * @return
     */
    private String getTime(){

        long timeStampSec = System.currentTimeMillis()/1000;
        String time = String.format("%010d", timeStampSec);
        return time;
    }

    /**
     * 数据发送方法
     * @param sessionId     本地白板会话ID
     * @param toAccount     对方账号
     * @param data          白板数据
     */
    public static void sendToRemote(String sessionId, String toAccount, String data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        try {
            RTSTunData channelData = new RTSTunData(sessionId, toAccount, data.getBytes
                    ("UTF-8"), data.getBytes().length);
            boolean b = RTSManager.getInstance().sendData(channelData);
            Log.i("白板发送数据： ", data);
        } catch (UnsupportedEncodingException e) {
            Log.e("Transaction", "send to remote, getBytes exception : " + data);
        }
    }


    /**
     * 白板挂断
     * @param SessionID     本地会话ID
     * @param context       上下文（当前所处的Activity）
     */
    public static void close(String SessionID , final Activity context){
        RTSManager.getInstance().close(SessionID, new RTSCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
//                Toast.makeText(context, "挂断成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(int code) {
//                Toast.makeText(context, "白板挂断失败，错误码"+code, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onException(Throwable exception) {
//                Toast.makeText(context, "白板挂断异常" +exception.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    /**
     * 收到白板挂断的回调
     * @param sessionID     本地会话ID
     * @param register      是否注册
     * @param context       上下文（当前所处的Activity）
     */
    public static void registerRTSCloseObserver(final String sessionID, final Boolean register, final Activity context){
        Observer<RTSCommonEvent> endSessionObserver = new Observer<RTSCommonEvent>() {
            @Override
            public void onEvent(RTSCommonEvent rtsCommonEvent) {
                if (register == true)
                {
//                    close(rtsCommonEvent.getLocalSessionId(),context);
                    Toast.makeText(context, "收到来自"+rtsCommonEvent.getAccount()+"挂断请求", Toast.LENGTH_SHORT).show();
                    AudioTeachActivity activity = (AudioTeachActivity)context;
                    close(sessionID,context);
                    activity.terminateRTS(sessionID);
                }


            }
        };
        Boolean issuccess = RTSManager.getInstance().observeHangUpNotification(sessionID,endSessionObserver,register);
        Log.e("TAG","注册数据监听"+issuccess);
    }

}