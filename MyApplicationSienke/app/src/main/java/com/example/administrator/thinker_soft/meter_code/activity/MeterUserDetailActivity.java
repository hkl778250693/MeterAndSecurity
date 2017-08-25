package com.example.administrator.thinker_soft.meter_code.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.example.administrator.thinker_soft.R;
import com.example.administrator.thinker_soft.mode.MySqliteHelper;
import com.example.administrator.thinker_soft.mode.Tools;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MeterUserDetailActivity extends Activity{
    private ImageView back,save;
    private SQLiteDatabase db;  //数据库
    private Cursor cursor;
    private CoordinatorLayout coordinatorlayout;
    private SharedPreferences sharedPreferences_login, sharedPreferences;
    private SimpleDateFormat dateFormat;
    private EditText thisMonthEndDegreeEdit;   //本月止度
    private String userID,uploadState;  //当前抄表用户的ID  上传状态
    private String longitude, latitude,locationAddress,thisMonthEndDegree, thisMonthDosage, lastMonthDegree, lastMonthDosage, meterDate,
            meterFlag, meterUserName, meterUserID, meterUserOldID, meterNumber, meterBelongArea, meterUserAddress,
            meterUserPhone, meterUserBalance, meterOweAcount, meterOweMonths, meterUserProperty, meterBook,
            meterSerialNumb, meterModel, meterReader, meterChangeDosage, meterStartdosage, meterRemission, meterRubbish;
    private TextView longitudeTv, latitudeTv, addressTv, thisMonthDosageTv, lastMonthDegreeTv, lastMonthDosageTv, meterDateTv,
            meterFlagTv, meterUserNameTv, meterUserIDTv, meterUserOldIDTv, meterNumberTv, meterBelongAreaTv, meterUserAddressTv,
            meterUserPhoneTv, meterUserBalanceTv, meterOweAcountTv, meterOweMonthsTv, meterUserPropertyTv, meterBookTv,
            meterSeriaNumbTv, meterModelTv, meterReaderTv, meterChangeDosageTv, meterStartdosageTv, meterRemissionTv, meterRubbishTv;
    private int monthDosage;
    private CardView locationCardview;
    private static int LOCATION_REQUEST_CODE = 100;
    // 定位相关
    private LocationClient mLocClient;
    private MyLocationListenner myListener = new MyLocationListenner();
    /**
     * 下拉刷新，上拉加载
     */
    private RefreshLayout mRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meter_user_detail_info);

        bindView();
        defaultSetting();
        setViewClickListener();
    }

    //绑定控件
    private void bindView() {
        back = (ImageView) findViewById(R.id.back);
        coordinatorlayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout);
        mRefreshLayout = (RefreshLayout) findViewById(R.id.refreshLayout);
        thisMonthEndDegreeEdit = (EditText) findViewById(R.id.this_month_end_degree);
        save = (ImageView) findViewById(R.id.save);
        longitudeTv = (TextView) findViewById(R.id.longitude);
        latitudeTv = (TextView) findViewById(R.id.latitude);
        addressTv = (TextView) findViewById(R.id.address);
        locationCardview = (CardView) findViewById(R.id.location_cardview);
        thisMonthDosageTv = (TextView) findViewById(R.id.this_month_dosage);
        lastMonthDegreeTv = (TextView) findViewById(R.id.last_month_degree);
        lastMonthDosageTv = (TextView) findViewById(R.id.last_month_dosage);
        meterDateTv = (TextView) findViewById(R.id.meter_date);
        meterFlagTv = (TextView) findViewById(R.id.meter_flag);
        meterUserNameTv = (TextView) findViewById(R.id.user_name);
        meterUserIDTv = (TextView) findViewById(R.id.user_id);
        meterUserOldIDTv = (TextView) findViewById(R.id.user_old_id);
        meterNumberTv = (TextView) findViewById(R.id.meter_number);
        meterBelongAreaTv = (TextView) findViewById(R.id.area_name);
        meterUserAddressTv = (TextView) findViewById(R.id.user_addres);
        meterUserPhoneTv = (TextView) findViewById(R.id.user_phone);
        meterUserBalanceTv = (TextView) findViewById(R.id.user_balance);
        meterOweAcountTv = (TextView) findViewById(R.id.owe_acount);
        meterOweMonthsTv = (TextView) findViewById(R.id.owe_months);
        meterUserPropertyTv = (TextView) findViewById(R.id.user_property);
        meterBookTv = (TextView) findViewById(R.id.meter_book);
        meterSeriaNumbTv = (TextView) findViewById(R.id.meter_serial_numb);
        meterModelTv = (TextView) findViewById(R.id.meter_model);
        meterReaderTv = (TextView) findViewById(R.id.meter_reader);
        meterChangeDosageTv = (TextView) findViewById(R.id.change_dosage);
        meterStartdosageTv = (TextView) findViewById(R.id.start_dosage);
        meterRemissionTv = (TextView) findViewById(R.id.remission);
        meterRubbishTv = (TextView) findViewById(R.id.rubbish_cost);
    }

    //初始化设置
    private void defaultSetting() {
        MySqliteHelper helper = new MySqliteHelper(MeterUserDetailActivity.this, 1);
        db = helper.getWritableDatabase();
        sharedPreferences_login = getSharedPreferences("login_info", Context.MODE_PRIVATE);
        sharedPreferences = MeterUserDetailActivity.this.getSharedPreferences(sharedPreferences_login.getString("userId", "") + "data", Context.MODE_PRIVATE);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        /**
         * 设置 下拉刷新 Header风格样式
         */
        mRefreshLayout.setRefreshHeader(new ClassicsHeader(this));
        mRefreshLayout.setPrimaryColorsId(R.color.theme_colors, android.R.color.white);
        mRefreshLayout.setEnableLoadmore(false);  //关闭上拉加载功能
        mRefreshLayout.setDisableContentWhenRefresh(true);  //是否在刷新的时候禁止内容的一切手势操作（默认false）
        /**
         * 获取上个页面传过来的参数
         */
        Intent intent = getIntent();
        if (intent != null) {
            userID = intent.getStringExtra("user_id");
            uploadState = intent.getStringExtra("upload_state");
            if("已上传".equals(uploadState)){
                save.setVisibility(View.GONE);
                thisMonthEndDegreeEdit.setEnabled(false);
            }
            Log.i("MeterUserDetailActivity", "用户ID为：" + userID);
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    getMeterUserDetailInfo(userID);
                }
            }.start();
        }
    }

    //点击事件
    public void setViewClickListener() {
        back.setOnClickListener(clickListener);
        save.setOnClickListener(clickListener);
        locationCardview.setOnClickListener(clickListener);
        /**
         * 下拉刷新监听
         */
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                refreshlayout.finishRefresh(400);
                initLocation();  //初始化定位
            }
        });
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.back:
                    MeterUserDetailActivity.this.finish();
                    break;
                case R.id.save:
                    if(Tools.isInputMethodOpened(MeterUserDetailActivity.this)){
                        Tools.hideSoftInput(MeterUserDetailActivity.this,thisMonthEndDegreeEdit);
                    }
                    if (!"".equals(thisMonthEndDegreeEdit.getText().toString())) {
                        if(Integer.parseInt(thisMonthEndDegreeEdit.getText().toString()) > Integer.parseInt(lastMonthDegreeTv.getText().toString())){
                            if(!longitudeTv.getText().toString().equals("未获取")){
                                meterDate = dateFormat.format(new Date());
                                meterDateTv.setText(meterDate);
                                monthDosage = Integer.parseInt(thisMonthEndDegreeEdit.getText().toString()) - Integer.parseInt(lastMonthDegree);
                                updateMeterUserInfo();
                                Intent intent = new Intent();
                                intent.putExtra("this_month_end_degree", thisMonthEndDegreeEdit.getText().toString().trim());
                                intent.putExtra("this_month_dosage", String.valueOf(monthDosage));
                                setResult(RESULT_OK, intent);
                                finish();
                            }else {
                                Toast.makeText(MeterUserDetailActivity.this, "请您获取当前经纬度坐标！", Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            Toast.makeText(MeterUserDetailActivity.this, "本月读数不能小于上月读数哦！", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MeterUserDetailActivity.this, "请您输入本月读数！", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.location_cardview:
                    if(!"未获取".equals(longitudeTv.getText().toString())){
                        Intent intent = new Intent(MeterUserDetailActivity.this, ObtainMeterCoodinateActivity.class);
                        if(!longitudeTv.getText().toString().equals("未获取")){
                            intent.putExtra("longitude",longitudeTv.getText().toString());
                            intent.putExtra("latitude",latitudeTv.getText().toString());
                        }
                        startActivityForResult(intent, LOCATION_REQUEST_CODE);
                    }else {
                        Snackbar.make(coordinatorlayout,"定位失败！未获取到当前位置，不能查看",Snackbar.LENGTH_SHORT);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == LOCATION_REQUEST_CODE) {
                if (data != null) {

                }
            }
        }
    }

    //初始化定位
    private void initLocation() {
        // 定位初始化
        mLocClient = new LocationClient(getApplicationContext()); //声明LocationClient类
        mLocClient.registerLocationListener(myListener);     //注册监听函数
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll"); // 设置坐标类型
        //option.setScanSpan(1000);
        option.setOpenGps(true); // 打开gps
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        option.setNeedDeviceDirect(true);
        mLocClient.setLocOption(option);
        mLocClient.start();
        longitudeTv.setText(longitude);
        latitudeTv.setText(latitude);
        addressTv.setText(locationAddress);
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // map_meter_icon view 销毁后不在处理新接收的位置
            if (location == null) {
                return;
            }
            int code = location.getLocType();
            Log.i("MyLocationListenner","code是："+code);
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());

            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }
            locationAddress = location.getAddrStr();
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());
            Log.i("currentAddress", locationAddress);
            Log.i("MyLocationListenner", sb.toString());
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
        public void onReceivePoi(BDLocation poiLocation) {

        }
    }

    /**
     * 根据用户ID查询本地数据库抄表详情
     */
    private void getMeterUserDetailInfo(String userID) {
        if (sharedPreferences.getBoolean("show_temp_data", false)) {
            cursor = db.rawQuery("select * from MeterUser where login_user_id=?", new String[]{"0"});//查询并获得游标
        }else {
            cursor = db.rawQuery("select * from MeterUser where login_user_id=? and user_id=?", new String[]{sharedPreferences_login.getString("userId", ""), userID});//查询并获得游标
        }
        //如果游标为空，则显示没有数据图片
        if (cursor.getCount() == 0) {
            return;
        }
        while (cursor.moveToNext()) {
            latitude = cursor.getString(cursor.getColumnIndex("n_jw_x"));
            longitude = cursor.getString(cursor.getColumnIndex("n_jw_y"));
            locationAddress = cursor.getString(cursor.getColumnIndex("locationAddress"));
            thisMonthEndDegree = cursor.getString(cursor.getColumnIndex("this_month_end_degree"));
            thisMonthDosage = cursor.getString(cursor.getColumnIndex("this_month_dosage"));
            if ("".equals(cursor.getString(cursor.getColumnIndex("meter_date")))) {
                meterDate = "暂无";
            } else {
                meterDate = cursor.getString(cursor.getColumnIndex("meter_date"));
            }
            lastMonthDegree = cursor.getString(cursor.getColumnIndex("meter_degrees"));
            lastMonthDosage = cursor.getString(cursor.getColumnIndex("last_month_dosage"));
            if (cursor.getString(cursor.getColumnIndex("meterState")).equals("false")) {
                meterFlag = "未抄";
            } else {
                meterFlag = "已抄";
            }
            if (!cursor.getString(cursor.getColumnIndex("user_name")).equals("null")) {
                meterUserName = cursor.getString(cursor.getColumnIndex("user_name"));
            } else {
                meterUserName = "无";
            }
            meterUserID = cursor.getString(cursor.getColumnIndex("user_id"));
            meterUserOldID = cursor.getString(cursor.getColumnIndex("old_user_id"));
            if (!cursor.getString(cursor.getColumnIndex("meter_number")).equals("null")) {
                meterNumber = cursor.getString(cursor.getColumnIndex("meter_number"));
            } else {
                meterNumber = "暂无";
            }
            meterBelongArea = cursor.getString(cursor.getColumnIndex("area_name"));
            meterUserAddress = cursor.getString(cursor.getColumnIndex("user_address"));
            if (!cursor.getString(cursor.getColumnIndex("user_phone")).equals("null")) {
                meterUserPhone = cursor.getString(cursor.getColumnIndex("user_phone"));
            } else {
                meterUserPhone = "暂无";
            }
            meterUserBalance = cursor.getString(cursor.getColumnIndex("user_amount"));
            meterOweAcount = cursor.getString(cursor.getColumnIndex("arrearage_amount"));
            meterOweMonths = cursor.getString(cursor.getColumnIndex("arrearage_months"));
            meterUserProperty = cursor.getString(cursor.getColumnIndex("property_name"));
            meterBook = cursor.getString(cursor.getColumnIndex("book_name"));
            meterSerialNumb = cursor.getString(cursor.getColumnIndex("meter_order_number"));
            meterModel = cursor.getString(cursor.getColumnIndex("meter_model"));
            meterReader = cursor.getString(cursor.getColumnIndex("meter_reader_name"));
            meterChangeDosage = cursor.getString(cursor.getColumnIndex("dosage_change"));
            meterStartdosage = cursor.getString(cursor.getColumnIndex("start_dosage"));
            meterRemission = cursor.getString(cursor.getColumnIndex("remission"));
            meterRubbish = cursor.getString(cursor.getColumnIndex("rubbish_cost"));
        }
        handler.sendEmptyMessage(0);
    }

    /**
     * 保存抄表信息到本地数据库
     */
    private void updateMeterUserInfo() {
        ContentValues values = new ContentValues();
        values.put("n_jw_x", latitudeTv.getText().toString());
        values.put("n_jw_y", longitudeTv.getText().toString());
        values.put("locationAddress", addressTv.getText().toString());
        values.put("this_month_end_degree", thisMonthEndDegreeEdit.getText().toString().trim());
        values.put("this_month_dosage", "" + monthDosage);
        values.put("meter_date", meterDate);
        values.put("meterState", "true");
        db.update("MeterUser", values, "login_user_id=? and user_id=?", new String[]{sharedPreferences_login.getString("userId", ""), userID});
        //db.update("MeterUser", values, "login_user_id=?", new String[]{"0"});
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if(meterFlag.equals("未抄")){
                        mRefreshLayout.autoRefresh();
                        initLocation();  //初始化定位
                    }else {
                        mRefreshLayout.setEnableRefresh(false);
                        longitudeTv.setText(longitude);
                        latitudeTv.setText(latitude);
                        addressTv.setText(locationAddress);
                    }
                    thisMonthEndDegreeEdit.setText(thisMonthEndDegree);
                    thisMonthEndDegreeEdit.setSelection(thisMonthEndDegree.length());//将光标移至文字末尾
                    thisMonthDosageTv.setText(thisMonthDosage);
                    lastMonthDegreeTv.setText(lastMonthDegree);
                    lastMonthDosageTv.setText(lastMonthDosage);
                    meterDateTv.setText(meterDate);
                    meterFlagTv.setText(meterFlag);
                    meterUserNameTv.setText(meterUserName);
                    meterUserIDTv.setText(meterUserID);
                    meterUserOldIDTv.setText(meterUserOldID);
                    meterNumberTv.setText(meterNumber);
                    meterBelongAreaTv.setText(meterBelongArea);
                    meterUserAddressTv.setText(meterUserAddress);
                    meterUserPhoneTv.setText(meterUserPhone);
                    meterUserBalanceTv.setText(meterUserBalance);
                    meterOweAcountTv.setText(meterOweAcount);
                    meterOweMonthsTv.setText(meterOweMonths);
                    meterUserPropertyTv.setText(meterUserProperty);
                    meterBookTv.setText(meterBook);
                    meterSeriaNumbTv.setText(meterSerialNumb);
                    meterModelTv.setText(meterModel);
                    meterReaderTv.setText(meterReader);
                    meterChangeDosageTv.setText(meterChangeDosage);
                    meterStartdosageTv.setText(meterStartdosage);
                    meterRemissionTv.setText(meterRemission);
                    meterRubbishTv.setText(meterRubbish);
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cursor != null) {
            cursor.close();
        }
        // 退出时销毁定位
        if(mLocClient != null){
            mLocClient.stop();
        }
        db.close();
    }
}
