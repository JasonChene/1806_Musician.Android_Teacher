package com.example.macbookpro.musictrainerteacher;
import android.graphics.Bitmap;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by chen on 17/10/27.
 */

public class ListDrawBitMap {
    //用于区分listview显示的不同item,告诉适配器我这是什么类型，listview适配器根据type决定怎么显示
    public String path;
    //将要显示的数据用HashMap包装好
    public HashMap<String,List<String>> main_map ;
    public HashMap<String,List<String>> peer_map;
    public String channel_name;

    public ListDrawBitMap(String path,String channel_name, HashMap<String, List<String>> main_map, HashMap<String, List<String>> peer_map)
    {
        this.channel_name = channel_name;
        this.path = path;
        this.main_map = main_map;
        this.peer_map = peer_map;
    }
}




