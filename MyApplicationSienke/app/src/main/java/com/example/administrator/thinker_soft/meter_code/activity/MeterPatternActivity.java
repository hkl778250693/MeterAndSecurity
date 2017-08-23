package com.example.administrator.thinker_soft.meter_code.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.example.administrator.thinker_soft.R;

/**
 * Created by Administrator on 2017/8/23.
 */
public class MeterPatternActivity extends Activity {
    private RadioButton meter, openBluetoothTb;
    private ImageView back;
    private SharedPreferences sharedPreferences_login, sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern);

        bindView();
        defaultSetting();
        setViewClickListener();
    }

    //绑定控件
    private void bindView() {
        openBluetoothTb = (RadioButton) findViewById(R.id.openBluetooth_tb);
        meter = (RadioButton) findViewById(R.id.meter);
        back = (ImageView) findViewById(R.id.back);
    }

    //初始化设置
    private void defaultSetting() {
        sharedPreferences_login = getSharedPreferences("login_info", Context.MODE_PRIVATE);
        sharedPreferences = MeterPatternActivity.this.getSharedPreferences(sharedPreferences_login.getString("login_name", "") + "data", Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean("detail_meter", true)) {
            openBluetoothTb.setChecked(true);
        } else {
            meter.setChecked(true);
        }
    }

    //点击事件
    public void setViewClickListener() {
        openBluetoothTb.setOnClickListener(clickListener);
        meter.setOnClickListener(clickListener);
        back.setOnClickListener(clickListener);
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.openBluetooth_tb:
                    meter.setChecked(false);
                    break;
                case R.id.meter:
                    openBluetoothTb.setChecked(false);
                    break;
                case R.id.back:
                    if (openBluetoothTb.isChecked()) {
                        sharedPreferences.edit().putBoolean("detail_meter", true).apply();
                    } else {
                        sharedPreferences.edit().putBoolean("detail_meter", false).apply();
                    }
                    finish();
                    break;
            }
        }
    };


}
