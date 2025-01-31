package com.example.macbookpro.musictrainerteacher.CustomView;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.example.macbookpro.musictrainerteacher.AudioTeachActivity;
import com.example.macbookpro.musictrainerteacher.R;
import com.example.macbookpro.musictrainerteacher.WhiteBoardManager;
import com.example.macbookpro.musictrainerteacher.manager.DataItem;
import com.example.macbookpro.musictrainerteacher.manager.DataManager;

import java.util.ArrayList;
import java.util.List;

//import com.netease.nical.rtsdemo.Manager.DataItem;
//import com.netease.nical.rtsdemo.Manager.DataManager;
//import com.netease.nical.rtsdemo.Manager.WhiteBoardManager;

public class Draw extends SurfaceView implements SurfaceHolder.Callback,View.OnTouchListener {

    public String sessionID;
    public String toAccount;
    public String dataReceived;
    public String channelID = "";
    public DataItem dataItem;

    private Path path = new Path();
    private String timeStamp;
    private float x;
    private float y;
    private String dataProcessed;
    int refPacketID;
    public Bitmap canvasBitmap = null;
    public Context mContext = null;


    Paint paint = new Paint();

    //实例化数据处理者对象
    DataManager dataManager =new DataManager();


    public Draw(Context context) {
        super(context);
    }
    public Draw(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Draw(Context context, AttributeSet attrs) {
        super(context,attrs);
        getHolder().addCallback(this);//回调
        paint.setColor(Color.RED);//笔的颜色
        paint.setTextSize(30);//画笔大小
        paint.setFakeBoldText(true);
        paint.setAntiAlias(true);//去除锯齿
        paint.setStrokeWidth(4);
        paint.setStyle(Paint.Style.STROKE);//实心线
        setOnTouchListener(this);
        setZOrderOnTop(true);//使surfaceview放到最顶层
        getHolder().setFormat(PixelFormat.TRANSLUCENT);//使窗口支持透明度
    }
    public void setContext(Context context)
    {
        mContext = context;
        Log.e("context",context.toString());
    }

    /**
     * 画
     */
    public void draw(){
        Canvas canvas = getHolder().lockCanvas();
        if (canvas != null)
        {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//绘制透明色
            canvas.drawPath(path,paint);
            canvas.drawARGB(1,255,0,0);
            getHolder().unlockCanvasAndPost(canvas);
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        draw();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int height=getMeasuredHeight();
        int width=getMeasuredWidth();
        String time = String.format("%010d", System.currentTimeMillis()/1000);
        String strData = "";
        AudioTeachActivity audioTeachActivity = (AudioTeachActivity)mContext;
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                x = event.getX();       //获取触摸点X轴坐标
                y = event.getY();       //获取触摸点Y轴坐标
                path.moveTo(x,y);
                timeStamp = getTime();  //获取时间戳
                dataProcessed = "1"+":"+x/width+","+y/height+";" + "5:" + (refPacketID++) + ",0";  //数据打包
                draw();
                strData = time +"," + x +"," + y + "m";
                audioTeachActivity.addDrawData(strData);
                WhiteBoardManager.sendToRemote(sessionID,toAccount,dataProcessed); //发送封装好的数据
                break;
            case MotionEvent.ACTION_MOVE:
                x = event.getX();       //获取触摸点X轴坐标
                y = event.getY();       //获取触摸点Y轴坐标
                path.lineTo(x,y);
                timeStamp = getTime();  //获取时间戳
                dataProcessed = "3"+":"+x/width+","+y/height+";" + "5:" + (refPacketID++) + ",0";  //数据打包
                draw();
                strData = time +"," + x +"," + y + "l";
                audioTeachActivity.addDrawData(strData);
                WhiteBoardManager.sendToRemote(sessionID,toAccount,dataProcessed); //发送封装好的数据
                break;
        }
        return true;
    }

    /**
     清除画布上的内容
     */
    public void Clear(){
        path.reset();
        draw();
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
     * 处理收到的白板data并绘制
     */
    public void dataPaint(String data){
         dataItem = dataManager.dataDecode(data);
         if(dataItem!=null){
             whitboardPaint(dataItem);
         }
    }

    /**
     * 绘制
     */
    private void whitboardPaint(DataItem dataItem){
        switch (dataItem.getStyle()){
            case "m":
                try {
                    float x1 = Float.parseFloat(dataItem.getX());
                    float y1 = Float.parseFloat(dataItem.getY());
                    path.moveTo(x1,y1);
                    draw();
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }
                break;

            case "l":
                try {
                    float x1 = Float.parseFloat(dataItem.getX());
                    float y1 = Float.parseFloat(dataItem.getY());
                    path.lineTo(x1,y1);
                    draw();
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }
                break;
        }
    }
}
