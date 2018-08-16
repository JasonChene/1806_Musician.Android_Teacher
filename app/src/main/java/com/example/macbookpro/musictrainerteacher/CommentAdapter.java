package com.example.macbookpro.musictrainerteacher;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
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
        try {
            student_name_show.setText(course.getJSONObject("student").getString("name"));

        }catch (JSONException e)
        {
        }
        return view;
    }
    public void layoutItemWithTime(View view,int imageResource,int shape_color ,String str_start_time,String str_end_time)
    {
        ImageView statusImage = (ImageView) view.findViewById(R.id.image);//获取该布局内的图片视图
        statusImage.setImageResource(imageResource);
        LinearLayout time_background = (LinearLayout)view.findViewById(R.id.time_background);
        TextView start_time = (TextView)view.findViewById(R.id.start_time);
        TextView end_time = (TextView)view.findViewById(R.id.end_time);
        time_background.setBackgroundResource(shape_color);
        start_time.setText(str_start_time);
        end_time.setText(str_end_time);
    }
    public void setTeachingAndCommentStatus(Date startDate,Date endDate, Button start_teaching,Button start_comment, Boolean isAllCommented){
        //未开始
        if (startDate.after(new Date()))
        {
            start_teaching.setText("未开始");
            start_teaching.setBackgroundResource(R.drawable.end_nostart_button);
            start_teaching.setEnabled(false);

        }//已结束
        else if (endDate.before(new Date()))
        {
            start_teaching.setText("已结束");
            start_teaching.setBackgroundResource(R.drawable.end_nostart_button);
            start_teaching.setEnabled(false);
            //评价按钮状态
            if (isAllCommented == true)
            {
                //已评价
                start_comment.setText("已评价");
                start_comment.setBackgroundResource(R.drawable.end_nostart_button);
            }
            else
            {
                //未评价
                start_comment.setText("未评价");
                start_comment.setBackgroundResource(R.drawable.btn_shape);
                start_comment.setEnabled(true);
            }
        }else
        {
            //正在上课
            start_teaching.setText("上课中");
            start_teaching.setEnabled(true);
        }
    }
}
