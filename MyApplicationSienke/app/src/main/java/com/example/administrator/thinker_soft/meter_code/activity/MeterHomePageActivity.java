package com.example.administrator.thinker_soft.meter_code.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.example.administrator.thinker_soft.R;
import com.example.administrator.thinker_soft.meter_code.adapter.MeterHomePageViewPagerAdapter;
import com.example.administrator.thinker_soft.meter_code.fragment.CustomQueryFragment;
import com.example.administrator.thinker_soft.meter_code.fragment.MeterDataTransferFragment;
import com.example.administrator.thinker_soft.meter_code.fragment.MeterHomePageFragment;
import com.example.administrator.thinker_soft.meter_code.fragment.MyInfoFragment;
import com.example.administrator.thinker_soft.meter_code.model.MeterUserListviewItem;
import com.example.administrator.thinker_soft.meter_code.zxing.android.CaptureActivity;
import com.example.administrator.thinker_soft.mode.MySqliteHelper;
import com.example.administrator.thinker_soft.mode.TempDataTools;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;

public class MeterHomePageActivity extends FragmentActivity {
    private static final String LTAG = MeterHomePageActivity.class.getSimpleName();
    private TextView mapInfo;
    private SDKReceiver mReceiver;
    private RelativeLayout rootRelative;
    private LinearLayout rootLinearlayout;
    private ViewPager viewPager;
    private TextView titleName, tips, ensure, title;
    private PopupWindow popupWindow, loadingWindow;
    private View line;
    private ImageView back, more, frameAnimation;
    private List<Fragment> fragmentList;
    private MeterHomePageViewPagerAdapter adapter;
    private RadioButton radio_button0, radio_button1, radio_button2, radio_button3;
    private SharedPreferences sharedPreferences_login, sharedPreferences;
    private SQLiteDatabase db;  //数据库
    private TempDataTools tools;
    private AnimationDrawable animationDrawable;
    private boolean flag = true;
    private boolean isFirst = true;
    private static final int REQUEST_CODE_SCAN = 0x0000;
    private ArrayList<MeterUserListviewItem> userLists = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meter_home_page);

        bindView();
        setViewPager();
        defaultSetting();
        setViewClickListener();
    }

    /**
     * 构造广播监听类，监听 SDK key 验证以及网络异常广播
     */
    public class SDKReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String s = intent.getAction();
            Log.i(LTAG, "action: " + s);
            if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
                mapInfo.setText("key 验证出错! 错误码 :" + intent.getIntExtra(SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_CODE, 0)
                        + " ; 请在 AndroidManifest.xml 文件中检查 key 设置");
            } else if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK)) {
                mapInfo.setText("key 验证成功! 功能可以正常使用");
                mapInfo.setTextColor(Color.YELLOW);
            } else if (s.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
                mapInfo.setText("网络出错");
            }
        }
    }

    //绑定控件
    private void bindView() {
        back = (ImageView) findViewById(R.id.back);
        rootLinearlayout = (LinearLayout) findViewById(R.id.root_linearlayout);
        titleName = (TextView) findViewById(R.id.title_name);
        more = (ImageView) findViewById(R.id.more);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        radio_button0 = (RadioButton) findViewById(R.id.radio_button0);
        radio_button1 = (RadioButton) findViewById(R.id.radio_button1);
        radio_button2 = (RadioButton) findViewById(R.id.radio_button2);
        radio_button3 = (RadioButton) findViewById(R.id.radio_button3);
        rootRelative = (RelativeLayout) findViewById(R.id.root_relative);
    }

    //初始化设置
    private void defaultSetting() {
        sharedPreferences_login = this.getSharedPreferences("login_info", Context.MODE_PRIVATE);
        sharedPreferences = MeterHomePageActivity.this.getSharedPreferences(sharedPreferences_login.getString("login_name", "") + "data", Context.MODE_PRIVATE);
        MySqliteHelper helper = new MySqliteHelper(MeterHomePageActivity.this, 1);
        db = helper.getWritableDatabase();
        radio_button0.setChecked(true);
        titleName.setText("移动抄表");
        if (sharedPreferences.getBoolean("show_temp_data", false)) {
            if (isFirst) {
                db.delete("MeterUser", null, null);  //删除User表中当前用户的所有数据（官方推荐方法）
                db.delete("MeterFile", null, null);
                db.delete("MeterBook", null, null);
                //设置id从1开始（sqlite默认id从1开始），若没有这一句，id将会延续删除之前的id
                db.execSQL("update sqlite_sequence set seq=0 where name='MeterUser'");
                db.execSQL("update sqlite_sequence set seq=0 where name='MeterFile'");
                db.execSQL("update sqlite_sequence set seq=0 where name='MeterBook'");
                isFirst = false;
            }
        }
    }

    /**
     * 这个函数在Activity创建完成之后会调用。购物车悬浮窗需要依附在Activity上，如果Activity还没有完全建好就去
     * 调用showCartFloatView()，则会抛出异常
     *
     * @param hasFocus
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (sharedPreferences.getBoolean("show_temp_data", false)) {
            if (flag) {
                tools = new TempDataTools(db);
                showPopupwindow();
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        tools.insertMeterUserData();
                        tools.insertMeterBook();
                        tools.insertMeterFile();
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        handler.sendEmptyMessage(2);
                    }
                }.start();
                flag = false;
            }
        }
    }

    //show获取数据加载动画
    public void showPopupwindow() {
        View loadingView = getLayoutInflater().inflate(R.layout.popupwindow_query_loading, null);
        loadingWindow = new PopupWindow(loadingView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        frameAnimation = (ImageView) loadingView.findViewById(R.id.frame_animation);
        title = (TextView) loadingView.findViewById(R.id.title);
        tips = (TextView) loadingView.findViewById(R.id.tips);
        line = loadingView.findViewById(R.id.line);
        ensure = (TextView) loadingView.findViewById(R.id.ensure);
        tips.setText("正在初始化演示数据，请稍后...");
        ensure.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingWindow.dismiss();
            }
        });
        loadingWindow.setBackgroundDrawable(getResources().getDrawable(R.color.white_transparent));
        loadingWindow.setAnimationStyle(R.style.camera);
        loadingWindow.update();
        loadingWindow.showAtLocation(rootLinearlayout, Gravity.CENTER, 0, 0);
        backgroundAlpha(0.6F);   //背景变暗
        startFrameAnimation();
        loadingWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                Log.i("MeterHomePageActivity", "弹窗消失");
                backgroundAlpha(1.0F);
            }
        });
    }

    //开始帧动画
    public void startFrameAnimation() {
        frameAnimation.setBackgroundResource(R.drawable.frame_animation_list);
        animationDrawable = (AnimationDrawable) frameAnimation.getDrawable();
        animationDrawable.start();
    }

    //设置背景透明度
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = MeterHomePageActivity.this.getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        if (bgAlpha == 1) {
            MeterHomePageActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//不移除该Flag的话,在有视频的页面上的视频会出现黑屏的bug
        } else {
            MeterHomePageActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//此行代码主要是解决在华为手机上半透明效果无效的bug
        }
        MeterHomePageActivity.this.getWindow().setAttributes(lp);
    }

    //设置viewPager
    private void setViewPager() {
        fragmentList = new ArrayList<>();
        //添加fragment到list
        fragmentList.add(new MeterHomePageFragment());
        fragmentList.add(new CustomQueryFragment());
        fragmentList.add(new MeterDataTransferFragment());
        fragmentList.add(new MyInfoFragment());
        //避免报空指针
        if (fragmentList != null) {
            adapter = new MeterHomePageViewPagerAdapter(getSupportFragmentManager(), fragmentList);
        }
        viewPager.setAdapter(adapter);
    }

    //点击事件
    public void setViewClickListener() {
        back.setOnClickListener(onClickListener);
        more.setOnClickListener(onClickListener);
        radio_button0.setOnClickListener(onClickListener);
        radio_button1.setOnClickListener(onClickListener);
        radio_button2.setOnClickListener(onClickListener);
        radio_button3.setOnClickListener(onClickListener);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        titleName.setText("首页");
                        radio_button0.setChecked(true);
                        radio_button1.setChecked(false);
                        radio_button2.setChecked(false);
                        radio_button3.setChecked(false);
                        break;
                    case 1:
                        titleName.setText("查询");
                        radio_button0.setChecked(false);
                        radio_button1.setChecked(true);
                        radio_button2.setChecked(false);
                        radio_button3.setChecked(false);
                        break;
                    case 2:
                        titleName.setText("传输");
                        radio_button0.setChecked(false);
                        radio_button1.setChecked(false);
                        radio_button2.setChecked(true);
                        radio_button3.setChecked(false);
                        break;
                    case 3:
                        titleName.setText("我");
                        radio_button0.setChecked(false);
                        radio_button1.setChecked(false);
                        radio_button2.setChecked(false);
                        radio_button3.setChecked(true);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.back:
                    finish();
                    break;
                case R.id.more:
                    showMoreWindow();
                    break;
                case R.id.radio_button0:
                    titleName.setText("首页");
                    viewPager.setCurrentItem(0);
                    break;
                case R.id.radio_button1:
                    titleName.setText("查询");
                    viewPager.setCurrentItem(1);
                    break;
                case R.id.radio_button2:
                    titleName.setText("传输");
                    viewPager.setCurrentItem(2);
                    break;
                case R.id.radio_button3:
                    titleName.setText("我");
                    viewPager.setCurrentItem(3);
                    break;
                default:
                    break;
            }
        }
    };

    private void showMoreWindow() {
        View contentView = getLayoutInflater().inflate(R.layout.popwindow_meter_more_content, null);
        popupWindow = new PopupWindow(contentView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mapInfo = (TextView) contentView.findViewById(R.id.map_info);
        TextView tesseractOcr = (TextView) contentView.findViewById(R.id.tesseract_ocr);
        TextView mapMeter = (TextView) contentView.findViewById(R.id.map_meter);
        //TextView coordinateManage = (TextView) contentView.findViewById(R.id.coordinate_manage);
        TextView systemSettings = (TextView) contentView.findViewById(R.id.system_settings);
       // TextView tasks = (TextView) contentView.findViewById(R.id.popwindow_content_actualtask);
        TextView scanCode = (TextView) contentView.findViewById(R.id.scan_code);
        /*// 注册 SDK 广播监听者
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        mReceiver = new SDKReceiver();
        LocalBroadcastManager.getInstance(MeterHomePageActivity.this).registerReceiver(mReceiver, iFilter);*/
        mapMeter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(MeterHomePageActivity.this, MapMeterActivity.class);
                startActivity(intent);
            }
        });
       /* coordinateManage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!"".equals(sharedPreferences.getString("currentFileName", ""))) {
                    if (!"".equals(sharedPreferences.getString("currentBookName", ""))) {
                        Intent intent = new Intent(MeterHomePageActivity.this, MeterUserCoordinateManageActivity.class);
                        intent.putExtra("fileName", sharedPreferences.getString("currentFileName", ""));
                        intent.putExtra("bookName", sharedPreferences.getString("currentBookName", ""));
                        intent.putExtra("bookID", sharedPreferences.getString("currentBookID", ""));
                        startActivity(intent);
                    } else {
                        Toast.makeText(MeterHomePageActivity.this, "请先选择抄表本！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MeterHomePageActivity.this, "请先完成文件选择！", Toast.LENGTH_SHORT).show();
                }
            }
        });*/
        scanCode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(MeterHomePageActivity.this, CaptureActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SCAN);
            }
        });
        systemSettings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(MeterHomePageActivity.this, MeterSettingsActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        tesseractOcr.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(MeterHomePageActivity.this, TesseractOcrActivity.class);
                startActivity(intent);
            }
        });
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.color.white_transparent));
        popupWindow.showAsDropDown(more, -180, 10);
        backgroundAlpha(0.6F);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1.0F);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 扫描二维码/条码回传
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(MeterHomePageActivity.this, "扫码取消！", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MeterHomePageActivity.this, "扫描成功，条码值: " + result.getContents(), Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                String codeResult = data.getStringExtra("codedContent");
                Bitmap bitmap = data.getParcelableExtra("codedBitmap");
                Log.i("MeterHomePageActivity", "codeResult = " + codeResult);
                queryMeterUserInfo(codeResult);
            }
        }
    }

    /**
     * 查询抄表用户信息
     * @param userID
     */
    public void queryMeterUserInfo(String userID) {
        userLists.clear();
        Cursor cursor = db.rawQuery("select * from MeterUser where login_user_id=? and file_name=? and user_id=?", new String[]{sharedPreferences_login.getString("userId", ""),sharedPreferences.getString("currentFileName",""),userID});//查询并获得游标
        //如果游标为空，则显示没有数据图片
        if (cursor.getCount() == 0) {
            handler.sendEmptyMessage(0);
            return;
        }
        while (cursor.moveToNext()) {
            MeterUserListviewItem item = new MeterUserListviewItem();
            item.setMeterID(cursor.getString(cursor.getColumnIndex("meter_order_number")));
            item.setUserName(cursor.getString(cursor.getColumnIndex("user_name")));
            item.setUserID(cursor.getString(cursor.getColumnIndex("user_id")));
            if(!cursor.getString(cursor.getColumnIndex("meter_number")).equals("null")){
                item.setMeterNumber(cursor.getString(cursor.getColumnIndex("meter_number")));
            }else {
                item.setMeterNumber("无");
            }
            item.setLastMonthDegree(cursor.getString(cursor.getColumnIndex("meter_degrees")));
            item.setLastMonthDosage(cursor.getString(cursor.getColumnIndex("last_month_dosage")));
            item.setAddress(cursor.getString(cursor.getColumnIndex("user_address")));
            if (cursor.getString(cursor.getColumnIndex("uploadState")).equals("false")) {
                item.setUploadState("");
            } else {
                item.setUploadState("已上传");
            }
            if (cursor.getString(cursor.getColumnIndex("meterState")).equals("false")) {
                item.setMeterState("未抄");
                item.setIfEdit(R.mipmap.meter_false);
                item.setThisMonthDegree("无");
                item.setThisMonthDosage("无");
            } else {
                item.setMeterState("已抄");
                item.setIfEdit(R.mipmap.meter_true);
                item.setThisMonthDegree(cursor.getString(cursor.getColumnIndex("this_month_end_degree")));
                item.setThisMonthDosage(cursor.getString(cursor.getColumnIndex("this_month_dosage")));
            }
            userLists.add(item);
        }
        handler.sendEmptyMessage(1);
        cursor.close();
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    Toast.makeText(MeterHomePageActivity.this,"未查到用户信息，请您核对编号是否正确！",Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Intent intent = new Intent(MeterHomePageActivity.this,MeterUserQueryResultActivity.class);
                    intent.putParcelableArrayListExtra("meter_user_info",userLists);
                    startActivity(intent);
                    break;
                case 2:
                    title.setVisibility(View.VISIBLE);
                    frameAnimation.setVisibility(View.GONE);
                    line.setVisibility(View.VISIBLE);
                    ensure.setVisibility(View.VISIBLE);
                    tips.setVisibility(View.GONE);
                    title.setText("数据初始化完成，请您体验！做的不好之处，请您反馈，谢谢！");
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消监听 SDK 广播
        LocalBroadcastManager.getInstance(MeterHomePageActivity.this).unregisterReceiver(mReceiver);
    }
}
