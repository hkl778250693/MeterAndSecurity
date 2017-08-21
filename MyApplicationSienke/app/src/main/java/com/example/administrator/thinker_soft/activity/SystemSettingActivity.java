package com.example.administrator.thinker_soft.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;

import com.example.administrator.thinker_soft.R;

/**
 * Created by Administrator on 2017/3/21.
 */
public class SystemSettingActivity extends Activity {
    private ImageView back;
    private CardView ip, clearData;
    private SharedPreferences sharedPreferences,sharedPreferences_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_settings);
        //绑定控件
        bindView();
        defaultSetting();//初始化设置
        //点击事件
        setViewClickListener();
    }

    //绑定控件
    private void bindView() {
        back = (ImageView) findViewById(R.id.back);
        ip = (CardView) findViewById(R.id.ip);
        clearData = (CardView) findViewById(R.id.clear_data);
    }

    //初始化设置
    private void defaultSetting() {
        sharedPreferences_login = this.getSharedPreferences("login_info", Context.MODE_PRIVATE);
        sharedPreferences = this.getSharedPreferences(sharedPreferences_login.getString("userId","")+"data", Context.MODE_PRIVATE);
    }

    //点击事件
    private void setViewClickListener() {
        back.setOnClickListener(clickListener);
        ip.setOnClickListener(clickListener);
        clearData.setOnClickListener(clickListener);
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent;
            switch (v.getId()) {
                case R.id.back:
                    finish();
                    break;
                case R.id.ip:
                    intent = new Intent(SystemSettingActivity.this, IpSettingActivity.class);
                    startActivity(intent);
                    break;
                case R.id.clear_data:
                    intent = new Intent(SystemSettingActivity.this, TaskFileDeleteActivity.class);
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    };

}
