package com.example.macbookpro.musictrainerteacher;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommentAdapter extends BaseAdapter {
    private final int resourceId;
    public Context mContext;
    List<JSONObject> listData;
    private LayoutInflater mLayoutInflater;



    public CommentAdapter(Context context, int textViewResourceId, ArrayList<JSONObject> objects) {
        resourceId = textViewResourceId;
        mContext = context;
        listData = objects;
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public  int getCount(){
        return listData.size();
    }
    @Override
    public Object getItem(int position)
    {
        return listData.get(position);
    }
    @Override
    public  long getItemId(int position)
    {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final JSONObject course = (JSONObject) getItem(position); // 获取当前项的Fruit实例
        Log.e("course",course.toString());
        View view = mLayoutInflater.inflate(resourceId, null);//实例化一个对象
        TextView student_name_show = (TextView) view.findViewById(R.id.student_name_show);//获取该布局内的课程文本视图
        EditText show_comment_id = (EditText)view.findViewById(R.id.show_comment_id);
        try {
            student_name_show.setText(course.getJSONObject("student").getString("name"));
            show_comment_id.setText(course.getString("comment"));
        }catch (JSONException e)
        {
        }
        return view;
    }
}
