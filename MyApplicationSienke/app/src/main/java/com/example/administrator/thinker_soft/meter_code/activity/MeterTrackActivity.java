package com.example.administrator.thinker_soft.meter_code.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.example.administrator.thinker_soft.R;
import com.example.administrator.thinker_soft.mode.MySqliteHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/8/22 0022.
 */
public class MeterTrackActivity extends AppCompatActivity {
    private ImageView back;
    private SQLiteDatabase db;  //数据库
    private SharedPreferences sharedPreferences_login, sharedPreferences;
    private CoordinatorLayout coordinatorLayout;
    // 抄表轨迹折线
    Polyline mPolyline,mTexturePolyline;
    // 地图相关
    MapView mMapView;
    BaiduMap mBaiduMap;
    private List<LatLng> latLngList = new ArrayList<>();  //存放坐标的集合
    // 初始化全局 bitmap 信息，不用时及时 recycle
    BitmapDescriptor startMarkerIcon = BitmapDescriptorFactory.fromResource(R.mipmap.start_point_icon);
    BitmapDescriptor middleMarkerIcon = BitmapDescriptorFactory.fromResource(R.mipmap.footprint);
    BitmapDescriptor terminalMarkerIcon = BitmapDescriptorFactory.fromResource(R.mipmap.terminal);
    BitmapDescriptor mGreenTexture = BitmapDescriptorFactory.fromAsset("icon_road_green_arrow.png");
    public Marker mMarker;

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
        mMapView = (MapView) findViewById(R.id.map_meter);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
    }

    //初始化设置
    private void defaultSetting() {
        MySqliteHelper helper = new MySqliteHelper(MeterTrackActivity.this, 1);
        db = helper.getWritableDatabase();
        sharedPreferences_login = this.getSharedPreferences("login_info", Context.MODE_PRIVATE);
        sharedPreferences = MeterTrackActivity.this.getSharedPreferences(sharedPreferences_login.getString("userId", "") + "data", Context.MODE_PRIVATE);
        mBaiduMap = mMapView.getMap();
        new Thread(){
            @Override
            public void run() {
                super.run();
                addMeterCoodinate();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(latLngList.size() != 0){
                            Log.i("MeterTrackActivity","抄表轨迹进来了");
                            MapStatus mMapStatus = new MapStatus.Builder()
                                    .target(latLngList.get(0))
                                    .zoom(14.0F)
                                    .build();
                            //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
                            MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
                            //改变地图状态
                            mBaiduMap.setMapStatus(mMapStatusUpdate);
                            for(int i = 0;i<latLngList.size();i++){
                                if(i == 0){
                                    setStartMarker(latLngList.get(i));
                                }else if(i == (latLngList.size()-1)){
                                    setTerminalMarker(latLngList.get(i));
                                }else {
                                    setMiddleMarker(latLngList.get(i));
                                }
                            }
                            /*OverlayOptions ooPolyline = new PolylineOptions().width(10).color(0xAAFF0000).points(latLngList);
                            mPolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline);*/
                            OverlayOptions ooPolyline11 = new PolylineOptions().width(15).points(latLngList).dottedLine(true).customTexture(mGreenTexture);
                            mTexturePolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline11);
                        }else {
                            LatLng center = new LatLng(29.5956,106.579369);
                            MapStatus mMapStatus = new MapStatus.Builder()
                                    .target(center)
                                    .zoom(14.0F)
                                    .build();
                            //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
                            MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
                            //改变地图状态
                            mBaiduMap.setMapStatus(mMapStatusUpdate);
                            Snackbar.make(coordinatorLayout,"您今天还没有抄表轨迹哦！",Snackbar.LENGTH_INDEFINITE).show();
                        }
                    }
                });
            }
        }.start();
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

    /**
     * 添加抄表坐标进行折线绘制
     */
    public void addMeterCoodinate() {
        latLngList.clear();
        /*Cursor cursor = db.rawQuery("select * from MeterUser where login_user_id=? and file_name=?", new String[]{sharedPreferences_login.getString("userId", ""),sharedPreferences.getString("currentFileName", "")});//查询并获得游标
        //如果游标为空，则显示没有数据图片
        if (cursor.getCount() == 0) {
            return;
        }
        while (cursor.moveToNext()) {
            if(!"未获取".equals(cursor.getString(cursor.getColumnIndex("n_jw_x"))) && !"未获取".equals(cursor.getString(cursor.getColumnIndex("n_jw_y")))){
                LatLng latLng = new LatLng(Double.parseDouble(cursor.getString(cursor.getColumnIndex("n_jw_x"))),Double.parseDouble(cursor.getString(cursor.getColumnIndex("n_jw_y"))));
                latLngList.add(latLng);
            }
        }*/
        LatLng p1 = new LatLng(29.965, 106.444);
        LatLng p2 = new LatLng(29.925, 106.494);
        LatLng p3 = new LatLng(29.955, 106.534);
        LatLng p4 = new LatLng(29.905, 106.594);
        LatLng p5 = new LatLng(29.965, 106.644);
        latLngList.add(p1);
        latLngList.add(p2);
        latLngList.add(p3);
        latLngList.add(p4);
        latLngList.add(p5);
        Log.i("MeterTrackActivity", "查询到" + latLngList.size() + "个坐标数据！");
    }

    /**
     * 添加起点位置marker
     */
    private void setStartMarker(LatLng center) {
        MarkerOptions markerOptions = new MarkerOptions().position(center).icon(startMarkerIcon);
        // 掉下动画
        markerOptions.animateType(MarkerOptions.MarkerAnimateType.drop);
        mMarker = (Marker) (mBaiduMap.addOverlay(markerOptions));
    }

    /**
     * 添加中间位置位置marker
     */
    private void setMiddleMarker(LatLng center) {
        MarkerOptions markerOptions = new MarkerOptions().position(center).icon(middleMarkerIcon);
        // 掉下动画
        markerOptions.animateType(MarkerOptions.MarkerAnimateType.drop);
        mMarker = (Marker) (mBaiduMap.addOverlay(markerOptions));
    }

    /**
     * 添加终点位置marker
     */
    private void setTerminalMarker(LatLng center) {
        MarkerOptions markerOptions = new MarkerOptions().position(center).icon(terminalMarkerIcon);
        // 掉下动画
        markerOptions.animateType(MarkerOptions.MarkerAnimateType.drop);
        mMarker = (Marker) (mBaiduMap.addOverlay(markerOptions));
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        if ( startMarkerIcon != null ) {
            startMarkerIcon.recycle();
        }
        if (middleMarkerIcon != null ) {
            middleMarkerIcon.recycle();
        }
        if (terminalMarkerIcon != null) {
            terminalMarkerIcon.recycle();
        }
        super.onDestroy();
    }
}
