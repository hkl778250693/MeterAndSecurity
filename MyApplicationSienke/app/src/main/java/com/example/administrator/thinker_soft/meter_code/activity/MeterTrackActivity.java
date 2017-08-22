package com.example.administrator.thinker_soft.meter_code.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.example.administrator.thinker_soft.R;
import com.example.administrator.thinker_soft.mode.MySqliteHelper;

/**
 * Created by Administrator on 2017/8/22 0022.
 */
public class MeterTrackActivity extends AppCompatActivity {
    private ImageView back;
    private SQLiteDatabase db;  //数据库
    private SharedPreferences sharedPreferences_login, sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meter_track);

        bindView();
        defaultSetting();
        setViewClickListener();
    }

    //绑定控件
    private void bindView() {
        back = (ImageView) findViewById(R.id.back);
    }

    //初始化设置
    private void defaultSetting() {
        MySqliteHelper helper = new MySqliteHelper(MeterTrackActivity.this, 1);
        db = helper.getWritableDatabase();
        sharedPreferences_login = this.getSharedPreferences("login_info", Context.MODE_PRIVATE);
        sharedPreferences = MeterTrackActivity.this.getSharedPreferences(sharedPreferences_login.getString("login_name", "") + "data", Context.MODE_PRIVATE);
    }

    //点击事件
    public void setViewClickListener() {
        back.setOnClickListener(clickListener);
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.back:
                    MeterTrackActivity.this.finish();
                    break;
                default:
                    break;
            }
        }
    };
}
