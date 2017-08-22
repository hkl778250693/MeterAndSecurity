package com.example.administrator.thinker_soft.Security_check.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.administrator.thinker_soft.R;
import com.example.administrator.thinker_soft.Security_check.activity.TaskFileDeleteActivity;
import com.example.administrator.thinker_soft.meter_code.activity.MeterSettingsActivity;

/**
 * Created by Administrator on 2017/8/21 0021.
 */
public class MyInfoFragment extends Fragment{
    private View view;
    private CardView fileManage,meterSettings;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my_info_sec, null);

        bindView();
        defaultSetting();
        setViewClickListener();
        return view;
    }

    //绑定控件
    private void bindView() {
        fileManage = (CardView) view.findViewById(R.id.file_manage);
        meterSettings = (CardView) view.findViewById(R.id.meter_settings);
    }

    //初始化设置
    private void defaultSetting() {

    }

    //点击事件
    public void setViewClickListener() {
        fileManage.setOnClickListener(clickListener);
        meterSettings.setOnClickListener(clickListener);
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent;
            switch (v.getId()) {
                case R.id.file_manage:
                    intent = new Intent(getActivity(), TaskFileDeleteActivity.class);
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
