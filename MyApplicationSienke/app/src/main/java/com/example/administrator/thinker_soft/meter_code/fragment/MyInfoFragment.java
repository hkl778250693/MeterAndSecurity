package com.example.administrator.thinker_soft.meter_code.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.administrator.thinker_soft.R;
import com.example.administrator.thinker_soft.meter_code.activity.MapMeterActivity;
import com.example.administrator.thinker_soft.meter_code.activity.MeterDeleteFileActivity;
import com.example.administrator.thinker_soft.meter_code.activity.MeterMapDownloadActivity;
import com.example.administrator.thinker_soft.meter_code.activity.MeterSettingsActivity;
import com.example.administrator.thinker_soft.meter_code.activity.MeterTrackActivity;
import com.example.administrator.thinker_soft.meter_code.zxing.android.CaptureActivity;

/**
 * Created by Administrator on 2017/8/21 0021.
 */
public class MyInfoFragment extends Fragment{
    private View view;
    private CardView scanCode,mapMeter,fileManage,mapManage,meterTrack,meterSettings;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my_info, null);

        bindView();
        defaultSetting();
        setViewClickListener();
        return view;
    }

    //绑定控件
    private void bindView() {
        scanCode = (CardView) view.findViewById(R.id.scan_code);
        mapMeter = (CardView) view.findViewById(R.id.map_meter_cardview);
        fileManage = (CardView) view.findViewById(R.id.file_manage);
        mapManage = (CardView) view.findViewById(R.id.map_manage);
        meterTrack = (CardView) view.findViewById(R.id.meter_track);
        meterSettings = (CardView) view.findViewById(R.id.meter_settings);
    }

    //初始化设置
    private void defaultSetting() {

    }

    //点击事件
    public void setViewClickListener() {
        scanCode.setOnClickListener(clickListener);
        mapMeter.setOnClickListener(clickListener);
        fileManage.setOnClickListener(clickListener);
        mapManage.setOnClickListener(clickListener);
        meterTrack.setOnClickListener(clickListener);
        meterSettings.setOnClickListener(clickListener);
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent;
            switch (v.getId()) {
                case R.id.scan_code:
                    intent = new Intent(getActivity(), CaptureActivity.class);
                    startActivity(intent);
                    break;
                case R.id.map_meter:
                    intent = new Intent(getActivity(), MapMeterActivity.class);
                    startActivity(intent);
                    break;
                case R.id.file_manage:
                    intent = new Intent(getActivity(), MeterDeleteFileActivity.class);
                    startActivity(intent);
                    break;
                case R.id.map_manage:
                    intent = new Intent(getActivity(), MeterMapDownloadActivity.class);
                    startActivity(intent);
                    break;
                case R.id.meter_track:
                    intent = new Intent(getActivity(), MeterTrackActivity.class);
                    startActivity(intent);
                    break;
                case R.id.meter_settings:
                    intent = new Intent(getActivity(), MeterSettingsActivity.class);
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    };
}
