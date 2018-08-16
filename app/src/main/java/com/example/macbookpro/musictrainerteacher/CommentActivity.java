package com.example.macbookpro.musictrainerteacher;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.view.View.GONE;

public class CommentActivity extends AppCompatActivity {
    CommentAdapter commentAdapter;
    ListView commentListVIew;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        initActionBar();
        Intent intent = getIntent();
        String course_info = intent.getStringExtra("courseInfo");
        Log.e("courseInfo", "" + course_info);
        ArrayList<JSONObject> allCourseArrayList = new ArrayList<JSONObject>();
        JSONObject course_stu_info = null;
        try {
            course_stu_info = new JSONObject(course_info);
            JSONArray values = course_stu_info.getJSONArray("value");
            for (int i = 0; i < values.length(); i ++)
            {
                allCourseArrayList.add(values.getJSONObject(i));
            }
        }catch (JSONException e)
        {

        }
        commentAdapter = new CommentAdapter(CommentActivity.this, R.layout.comment_list_item, allCourseArrayList);
        commentListVIew = (ListView) findViewById(R.id.comment_list_view);
        commentListVIew.setAdapter(commentAdapter);
    }
    public void initActionBar() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            LayoutInflater inflator = (LayoutInflater) this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflator.inflate(R.layout.actionbar_audio_teach, new LinearLayout(CommentActivity.this), false);
            android.support.v7.app.ActionBar.LayoutParams layout = new android.support.v7.app.ActionBar.LayoutParams(
                    android.support.v7.app.ActionBar.LayoutParams.MATCH_PARENT, android.support.v7.app.ActionBar.LayoutParams.MATCH_PARENT);
            actionBar.setCustomView(v, layout);
            Toolbar parent = (Toolbar) v.getParent();
            parent.setContentInsetsAbsolute(0, 0);
        }
        TextView actionBarTitle = (TextView) findViewById(R.id.action_bar_title);
        actionBarTitle.setText("写评语");
        //顶部返回按键
        Button back_button = (Button) findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(CommentActivity.this, MainActivity.class));

            }
        });
    }
}
