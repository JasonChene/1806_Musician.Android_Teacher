package com.example.macbookpro.musictrainerteacher.common;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chen on 17/11/8.
 */

public class SysExitUtil {
    public static List activityList = new ArrayList();


    //finish所有list中的activity
    public static void exit(){
        int siz=activityList.size();
        for(int i=0;i<siz;i++){
            if(activityList.get(i)!=null){
                ((Activity) activityList.get(i)).finish();
            }
        }
    }
}
