package com.example.administrator.thinker_soft.meter_code.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;

import com.example.administrator.thinker_soft.R;

public class MeterSettingsActivity extends Activity {
    private ImageView back;
    private CardView bluetooth,fileDelete,setPageCount,mapDonload,printNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meter_settings);

        bindView();
        defaultSetting();
        setViewClickListener();
    }

    //绑定控件
    private void bindView() {
        back = (ImageView) findViewById(R.id.back);
        bluetooth = (CardView) findViewById(R.id.bluetooth);
        fileDelete = (CardView) findViewById(R.id.file_delete);
        setPageCount = (CardView) findViewById(R.id.set_page_count);
        printNote = (CardView) findViewById(R.id.print_note);
    }

    //初始化设置
    private void defaultSetting() {

    }

    //点击事件
    public void setViewClickListener() {
        back.setOnClickListener(onClickListener);
        bluetooth.setOnClickListener(onClickListener);
        fileDelete.setOnClickListener(onClickListener);
        setPageCount.setOnClickListener(onClickListener);
        printNote.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent;
            switch (v.getId()) {
                case R.id.back:
                    MeterSettingsActivity.this.finish();
                    break;
                case R.id.bluetooth:
                    intent = new Intent(MeterSettingsActivity.this, BluetoothActivity.class);
                    startActivity(intent);
                    break;
                case R.id.file_delete:
                    intent = new Intent(MeterSettingsActivity.this, MeterDeleteFileActivity.class);
                    startActivity(intent);
                    break;
                case R.id.set_page_count:
                    intent = new Intent(MeterSettingsActivity.this, MeterPageCountSettingsActivity.class);
                    startActivity(intent);
                    break;
                case R.id.print_note:
                    intent = new Intent(MeterSettingsActivity.this, MeterPrintNoteActivity.class);
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    };
}
