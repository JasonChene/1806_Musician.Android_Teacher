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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVACL;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.view.View.GONE;

public class CommentActivity extends AppCompatActivity {
    CommentAdapter commentAdapter;
    ListView commentListView;
    ArrayList<JSONObject> allCourseArrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        initActionBar();
        Intent intent = getIntent();
        String course_info = intent.getStringExtra("courseInfo");
        Log.e("courseInfo", "" + course_info);
        allCourseArrayList = new ArrayList<JSONObject>();
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
        commentListView = (ListView) findViewById(R.id.comment_list_view);
        commentListView.setAdapter(commentAdapter);

        Button save_button = (Button)findViewById(R.id.save_button);
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("save","保存");
                final Boolean isSaveSuccess = true;
                for (int m = 0; m < commentListView.getChildCount(); m ++)
                {
                    View itemView = commentListView.getChildAt(m);
                    EditText commentEditView = (EditText) itemView.findViewById(R.id.show_comment_id);
                    String courseID = "";
                    String comment = commentEditView.getText().toString();
                    try {
                        courseID = allCourseArrayList.get(m).getString("courseID");
                    }
                    catch (JSONException e)
                    {

                    }
                    // 第一参数是 className,第二个参数是 objectId
                    AVObject course = AVObject.createWithoutData("Course", courseID);
                    Log.e("todo",course.toString());
                    // 修改 content
                    course.put("comment",comment);
                    // 保存到云端
                    course.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(AVException e) {
                            if (e != null)
                            {
                                Toast.makeText(CommentActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                }
                Toast.makeText(CommentActivity.this,"保存成功",Toast.LENGTH_SHORT).show();
                finish();
            }
        });
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
