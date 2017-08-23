package com.example.administrator.thinker_soft.meter_code.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.thinker_soft.R;
import com.example.administrator.thinker_soft.meter_code.adapter.MeterUserListRecycleAdapter;
import com.example.administrator.thinker_soft.meter_code.model.MeterTypeListviewItem;
import com.example.administrator.thinker_soft.meter_code.model.MeterUserListviewItem;
import com.example.administrator.thinker_soft.mode.MyAnimationUtils;
import com.example.administrator.thinker_soft.mode.MySqliteHelper;
import com.example.administrator.thinker_soft.mode.Tools;
import com.scwang.smartrefresh.header.WaterDropHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.BallPulseFooter;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MeterUserListviewActivity extends AppCompatActivity {
    private ImageView back, pageTurning;
    private LinearLayout selectPage, rootLinearlayout;
    private RelativeLayout title;
    private TextView noData, lastPage, nextPage, currentPageTv, totalPageTv, bookName;
    private ArrayList<MeterUserListviewItem> userLists = new ArrayList<>();
    private SQLiteDatabase db;  //数据库
    private Cursor totalCountCursor, userLimitCursor;
    private SharedPreferences sharedPreferences_login, sharedPreferences;
    private int dataStartCount = 0;   //用于分页查询，表示从第几行开始
    private int currentPage = 1;  //当前页数
    private int totalPage;    //总页数
    private MeterUserListviewItem item;
    private int currentPosition;  //点击当前抄表用户的item位置
    private List<MeterTypeListviewItem> areaItemList = new ArrayList<>();
    private List<MeterTypeListviewItem> bookItemList = new ArrayList<>();
    private List<String> bookIDList = new ArrayList<>();  //存放表册ID的集合
    private String bookID, book_name, fileName;
    private LayoutInflater layoutInflater;
    private PopupWindow fastMeterWindow;
    private View fastMeterview;
    private MeterUserListRecycleAdapter.ViewHolder viewHolder;
    private String meterDate;
    private SimpleDateFormat dateFormat;
    private RelativeLayout layout;
    /**
     * 下拉刷新，上拉加载
     */
    private RefreshLayout mRefreshLayout;
    /**
     * RecyclerView相关
     */
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meter_user_listview);

        bindView();
        defaultSetting();
        setViewClickListener();
    }

    //绑定控件
    private void bindView() {
        rootLinearlayout = (LinearLayout) findViewById(R.id.root_linearlayout);
        title = (RelativeLayout) findViewById(R.id.title);
        pageTurning = (ImageView) findViewById(R.id.page_turning);
        back = (ImageView) findViewById(R.id.back);
        bookName = (TextView) findViewById(R.id.book_name);
        noData = (TextView) findViewById(R.id.no_data);
        selectPage = (LinearLayout) findViewById(R.id.select_page);
        lastPage = (TextView) findViewById(R.id.last_page);
        nextPage = (TextView) findViewById(R.id.next_page);
        currentPageTv = (TextView) findViewById(R.id.current_page_tv);
        totalPageTv = (TextView) findViewById(R.id.total_page_tv);
        mRefreshLayout = (RefreshLayout) findViewById(R.id.refreshLayout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
    }

    //初始化设置
    private void defaultSetting() {
        MySqliteHelper helper = new MySqliteHelper(MeterUserListviewActivity.this, 1);
        db = helper.getWritableDatabase();
        sharedPreferences_login = this.getSharedPreferences("login_info", Context.MODE_PRIVATE);
        sharedPreferences = MeterUserListviewActivity.this.getSharedPreferences(sharedPreferences_login.getString("login_name", "") + "data", Context.MODE_PRIVATE);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        /**
         * 设置 下拉刷新 Header 和 footer 风格样式
         */
        //mRefreshLayout.setRefreshHeader(new MaterialHeader(this).setShowBezierWave(true));
        //mRefreshLayout.setRefreshHeader(new DeliveryHeader(this));
        //mRefreshLayout.setRefreshHeader(new CircleHeader(this));
        //mRefreshLayout.setRefreshHeader(new DropboxHeader(this));
        //mRefreshLayout.setRefreshHeader(new FunGameHeader(this));
        //mRefreshLayout.setRefreshHeader(new FalsifyHeader(this));
        //mRefreshLayout.setRefreshHeader(new PhoenixHeader(this));
        mRefreshLayout.setRefreshHeader(new WaterDropHeader(this));
        mRefreshLayout.setPrimaryColorsId(R.color.theme_colors, android.R.color.white);
        //mRefreshLayout.setRefreshHeader(new WaveSwipeHeader(this));
        //mRefreshLayout.setRefreshHeader(new TaurusHeader(this));
        //mRefreshLayout.setRefreshHeader(new StoreHouseHeader(this));
        //mRefreshLayout.setRefreshHeader(new FunGameHitBlockHeader(this));
        //mRefreshLayout.setRefreshHeader(new FunGameBattleCityHeader(this));
        //mRefreshLayout.setRefreshHeader(new FlyRefreshHeader(this));
        //设置 Footer 为 球脉冲
        mRefreshLayout.setRefreshFooter(new BallPulseFooter(this).setSpinnerStyle(SpinnerStyle.Scale));
        mRefreshLayout.setDisableContentWhenRefresh(true);  //是否在刷新的时候禁止内容的一切手势操作（默认false）
        mRefreshLayout.setDisableContentWhenLoading(true);  //是否在加载的时候禁止内容的一切手势操作（默认false）
        /**
         * 获取上个页面传过来的参数
         */
        Intent intent = getIntent();
        if (intent != null) {
            fileName = intent.getStringExtra("fileName");
            bookID = intent.getStringExtra("bookID");
            book_name = intent.getStringExtra("bookName");
            bookName.setText("当前：" + book_name);
            if (!"".equals(bookID) && !"".equals(fileName)) {
                Log.i("meter_user", "");
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        getTotalUserCount();
                        getMeterUserData(fileName, bookID, dataStartCount);   //默认读取本地的抄表分区用户数据
                    }
                }.start();
            }
        }
    }

    //点击事件
    public void setViewClickListener() {
        back.setOnClickListener(clickListener);
        pageTurning.setOnClickListener(clickListener);
        lastPage.setOnClickListener(clickListener);
        nextPage.setOnClickListener(clickListener);
        /**
         * 下拉刷新监听
         */
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                refreshlayout.finishRefresh(800);
                if (currentPageTv.getText().equals("1")) {
                    Toast.makeText(MeterUserListviewActivity.this, "已经是第一页哦！", Toast.LENGTH_SHORT).show();
                } else {
                    dataStartCount -= 50;
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            getMeterUserData(fileName, bookID, dataStartCount);  //读取抄表本用户数据
                            handler.sendEmptyMessage(2);
                        }
                    }.start();
                }
            }
        });
        /**
         * 上拉加载监听
         */
        mRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                refreshlayout.finishLoadmore(800);
                if (currentPageTv.getText().equals(totalPageTv.getText())) {
                    Toast.makeText(MeterUserListviewActivity.this, "已经是最后一页哦！", Toast.LENGTH_SHORT).show();
                } else {
                    dataStartCount += 50;
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            getMeterUserData(fileName, bookID, dataStartCount);  //读取抄表本用户数据
                            handler.sendEmptyMessage(1);
                        }
                    }.start();
                }
            }
        });
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.back:
                    MeterUserListviewActivity.this.finish();
                    break;
                case R.id.page_turning:
                    if (selectPage.getVisibility() == View.GONE) {
                        selectPage.setVisibility(View.VISIBLE);
                        MyAnimationUtils.viewGroupBottomInAnimation(MeterUserListviewActivity.this, selectPage, 0.3F);
                    } else {
                        MyAnimationUtils.viewGroupBottomOutAnimation(MeterUserListviewActivity.this, selectPage, 0.3F);
                        selectPage.setVisibility(View.GONE);
                    }
                    break;
                case R.id.last_page:
                    mRefreshLayout.autoRefresh();
                    break;
                case R.id.next_page:
                    mRefreshLayout.autoLoadmore();
                    break;
                default:
                    break;
            }
        }
    };

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    /**
                     * 使用 RecyclerView 控件
                     */
                    mLayoutManager = new LinearLayoutManager(MeterUserListviewActivity.this, LinearLayoutManager.VERTICAL, false);
                    mAdapter = new MeterUserListRecycleAdapter(userLists, new MeterUserListRecycleAdapter.onRecyclerViewItemClick() {
                        @Override
                        public void onItemClick(View v, int position) {
                            currentPosition = position;
                            Log.i("MeterUserListviewAc", "RecycleView的点击事件进来了！");
                            if (null != mRecyclerView.getChildViewHolder(v)) {
                                viewHolder = (MeterUserListRecycleAdapter.ViewHolder) mRecyclerView.getChildViewHolder(v);
                                item = (MeterUserListviewItem) viewHolder.itemView.getTag();
                                if (sharedPreferences.getBoolean("detail_meter", true)) {
                                    Intent intent = new Intent(MeterUserListviewActivity.this, MeterUserDetailActivity.class);
                                    intent.putExtra("user_id", item.getUserID());
                                    intent.putExtra("upload_state", item.getUploadState());
                                    startActivityForResult(intent, currentPosition);
                                } else {
                                    layout = (RelativeLayout) mLayoutManager.findViewByPosition(currentPosition);
                                    layout.findViewById(R.id.red_stroke).setVisibility(View.VISIBLE);
                                    showFastMeterWindow(item.getLastMonthDegree());   //弹出快捷抄表框
                                }
                            }
                        }
                    }, new MeterUserListRecycleAdapter.onRecyclerViewItemLongClick() {
                        @Override
                        public void onItemLongClick(View v, int position) {

                        }
                    });
                    // 设置布局管理器
                    mRecyclerView.setLayoutManager(mLayoutManager);
                    // 设置adapter
                    mRecyclerView.setAdapter(mAdapter);
                    MyAnimationUtils.viewGroupOutAlphaAnimation(MeterUserListviewActivity.this, mRecyclerView, 0.1F);
                    //设置增加或删除条目的动画
                    mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                    currentPageTv.setText(String.valueOf(currentPage));
                    if (totalCountCursor.getCount() % 50 != 0) {
                        totalPage = totalCountCursor.getCount() / 50 + 1;
                    } else {
                        if (totalCountCursor.getCount() <= 50) {
                            totalPage = 1;
                        } else {
                            totalPage = totalCountCursor.getCount() / 50;
                        }
                    }
                    totalPageTv.setText(String.valueOf(totalPage));
                    break;
                case 1:
                    mLayoutManager = new LinearLayoutManager(MeterUserListviewActivity.this, LinearLayoutManager.VERTICAL, false);
                    mAdapter = new MeterUserListRecycleAdapter(userLists, new MeterUserListRecycleAdapter.onRecyclerViewItemClick() {
                        @Override
                        public void onItemClick(View v, int position) {
                            currentPosition = position;
                            Log.i("MeterUserListviewAc", "RecycleView的点击事件进来了！");
                            if (null != mRecyclerView.getChildViewHolder(v)) {
                                RecyclerView.ViewHolder viewHolder = mRecyclerView.getChildViewHolder(v);
                                item = (MeterUserListviewItem) viewHolder.itemView.getTag();
                                Intent intent = new Intent(MeterUserListviewActivity.this, MeterUserDetailActivity.class);
                                intent.putExtra("user_id", item.getUserID());
                                intent.putExtra("upload_state", item.getUploadState());
                                startActivityForResult(intent, currentPosition);
                            }
                        }
                    }, new MeterUserListRecycleAdapter.onRecyclerViewItemLongClick() {
                        @Override
                        public void onItemLongClick(View v, int position) {

                        }
                    });
                    mAdapter.notifyDataSetChanged();
                    // 设置布局管理器
                    mRecyclerView.setLayoutManager(mLayoutManager);
                    // 设置adapter
                    mRecyclerView.setAdapter(mAdapter);
                    MyAnimationUtils.viewGroupOutAlphaAnimation(MeterUserListviewActivity.this, mRecyclerView, 0.1F);
                    //设置增加或删除条目的动画
                    mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                    currentPageTv.setText(String.valueOf(Integer.parseInt(currentPageTv.getText().toString()) + 1));
                    break;
                case 2:
                    mLayoutManager = new LinearLayoutManager(MeterUserListviewActivity.this, LinearLayoutManager.VERTICAL, false);
                    mAdapter = new MeterUserListRecycleAdapter(userLists, new MeterUserListRecycleAdapter.onRecyclerViewItemClick() {
                        @Override
                        public void onItemClick(View v, int position) {
                            currentPosition = position;
                            Log.i("MeterUserListviewAc", "RecycleView的点击事件进来了！");
                            if (null != mRecyclerView.getChildViewHolder(v)) {
                                RecyclerView.ViewHolder viewHolder = mRecyclerView.getChildViewHolder(v);
                                item = (MeterUserListviewItem) viewHolder.itemView.getTag();
                                Intent intent = new Intent(MeterUserListviewActivity.this, MeterUserDetailActivity.class);
                                intent.putExtra("user_id", item.getUserID());
                                intent.putExtra("upload_state", item.getUploadState());
                                startActivityForResult(intent, currentPosition);
                            }
                        }
                    }, new MeterUserListRecycleAdapter.onRecyclerViewItemLongClick() {
                        @Override
                        public void onItemLongClick(View v, int position) {

                        }
                    });
                    mAdapter.notifyDataSetChanged();
                    // 设置布局管理器
                    mRecyclerView.setLayoutManager(mLayoutManager);
                    // 设置adapter
                    mRecyclerView.setAdapter(mAdapter);
                    MyAnimationUtils.viewGroupOutAlphaAnimation(MeterUserListviewActivity.this, mRecyclerView, 0.1F);
                    //设置增加或删除条目的动画
                    mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                    currentPageTv.setText(String.valueOf(Integer.parseInt(currentPageTv.getText().toString()) - 1));
                    break;
                case 3:
                    if (noData.getVisibility() == View.GONE) {
                        noData.setVisibility(View.VISIBLE);
                    }
                    pageTurning.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    /**
     * 弹出快捷抄表窗口
     */
    public void showFastMeterWindow(final String lastMonthDegree) {
        layoutInflater = LayoutInflater.from(MeterUserListviewActivity.this);
        fastMeterview = layoutInflater.inflate(R.layout.popupwindow_fast_meter, null);
        fastMeterWindow = new PopupWindow(fastMeterview, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //绑定控件ID
        RadioButton cancel = (RadioButton) fastMeterview.findViewById(R.id.cancel_rb);
        final EditText meterEdit = (EditText) fastMeterview.findViewById(R.id.meter_edit);
        RadioButton save = (RadioButton) fastMeterview.findViewById(R.id.save_rb);
        //设置点击事件
        meterEdit.requestFocus();
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fastMeterWindow.dismiss();
                layout.findViewById(R.id.red_stroke).setVisibility(View.GONE);
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!"".equals(meterEdit.getText().toString())) {
                    if (Integer.parseInt(meterEdit.getText().toString()) > Integer.parseInt(lastMonthDegree)) {
                        fastMeterWindow.dismiss();
                        String thisMonthDegree = meterEdit.getText().toString();
                        int thisMonthDosage = Integer.parseInt(meterEdit.getText().toString()) - Integer.parseInt(lastMonthDegree);
                        item.setThisMonthDegree(thisMonthDegree);
                        item.setThisMonthDosage(String.valueOf(thisMonthDosage));
                        item.setIfEdit(R.mipmap.meter_true);
                        item.setMeterState("已抄");
                        mAdapter.notifyDataSetChanged();
                        Tools.moveToPosition((LinearLayoutManager) mLayoutManager, mRecyclerView, currentPosition + 1);
                        //updateMeterUserInfo();
                        currentPosition = ((LinearLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
                        mLayoutManager.getChildAt(currentPosition);
                    } else {
                        Toast.makeText(MeterUserListviewActivity.this, "本月读数不能小于上月读数哦！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MeterUserListviewActivity.this, "请您输入本月读数！", Toast.LENGTH_SHORT).show();
                }
            }
        });
        fastMeterWindow.update();
        fastMeterWindow.setFocusable(true);
        fastMeterWindow.setTouchable(true);
        fastMeterWindow.setOutsideTouchable(true);
        fastMeterWindow.setBackgroundDrawable(getResources().getDrawable(R.color.white_transparent));
        fastMeterWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
        backgroundAlpha(0.6F);   //背景变暗
        fastMeterWindow.showAtLocation(rootLinearlayout, Gravity.CENTER, 0, 0);
        fastMeterWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1.0F);
            }
        });
    }

    /**
     * 保存抄表信息到本地数据库
     */
    /*private void updateMeterUserInfo() {
        ContentValues values = new ContentValues();
        values.put("n_jw_x", latitudeTv.getText().toString());
        values.put("n_jw_y", longitudeTv.getText().toString());
        values.put("locationAddress", addressTv.getText().toString());
        values.put("this_month_end_degree", thisMonthEndDegreeEdit.getText().toString().trim());
        values.put("this_month_dosage", "" + item.getThisMonth());
        values.put("meter_date",dateFormat.format(new Date()));
        values.put("meterState", "true");
        db.update("MeterUser", values, "login_user_id=? and user_id=?", new String[]{sharedPreferences_login.getString("userId", ""), userID});
    }*/

    //设置背景透明度
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = MeterUserListviewActivity.this.getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        if (bgAlpha == 1) {
            MeterUserListviewActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//不移除该Flag的话,在有视频的页面上的视频会出现黑屏的bug
        } else {
            MeterUserListviewActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//此行代码主要是解决在华为手机上半透明效果无效的bug
        }
        MeterUserListviewActivity.this.getWindow().setAttributes(lp);
    }

    //查询抄表用户总数
    public void getTotalUserCount() {
        if (sharedPreferences.getBoolean("show_temp_data", false)) {
            totalCountCursor = db.rawQuery("select * from MeterUser where login_user_id=? and file_name=? and book_id=?", new String[]{"0", fileName, bookID});//查询并获得游标
        } else {
            totalCountCursor = db.rawQuery("select * from MeterUser where login_user_id=? and file_name=? and book_id=?", new String[]{sharedPreferences_login.getString("userId", ""), fileName, bookID});//查询并获得游标
        }
        //如果游标为空，则显示没有数据图片
        Log.i("MeterUserLVActivity", "总的查询到" + totalCountCursor.getCount() + "条数据！");
        if (totalCountCursor.getCount() == 0) {
            return;
        }
        while (totalCountCursor.moveToNext()) {

        }
        totalCountCursor.close();
    }


    //读取本地的抄表分区用户数据
    public void getMeterUserData(String fileName, String bookID, int dataStartCount) {
        userLists.clear();
        if (!"".equals(sharedPreferences.getString("page_count", ""))) {
            if (sharedPreferences.getBoolean("show_temp_data", false)) {   //显示演示数据
                userLimitCursor = db.rawQuery("select * from MeterUser where login_user_id=? and file_name=? and book_id=? limit " + dataStartCount + "," + Integer.parseInt(sharedPreferences.getString("page_count", "")), new String[]{"0", fileName, bookID});//查询并获得游标
            } else {
                userLimitCursor = db.rawQuery("select * from MeterUser where login_user_id=? and file_name=? and book_id=? limit " + dataStartCount + "," + Integer.parseInt(sharedPreferences.getString("page_count", "")), new String[]{sharedPreferences_login.getString("userId", ""), fileName, bookID});//查询并获得游标
            }
        } else {
            if (sharedPreferences.getBoolean("show_temp_data", false)) {  //显示演示数据
                userLimitCursor = db.rawQuery("select * from MeterUser where login_user_id=? and file_name=? and book_id=? limit " + dataStartCount + ",50", new String[]{"0", fileName, bookID});//查询并获得游
            } else {
                userLimitCursor = db.rawQuery("select * from MeterUser where login_user_id=? and file_name=? and book_id=? limit " + dataStartCount + ",50", new String[]{sharedPreferences_login.getString("userId", ""), fileName, bookID});//查询并获得游
            }
        }
        Log.i("MeterUserLVActivity", "分页查询到" + userLimitCursor.getCount() + "条数据！");
        //如果游标为空，则显示没有数据图片
        if (userLimitCursor.getCount() == 0) {
            handler.sendEmptyMessage(3);
            return;
        }
        while (userLimitCursor.moveToNext()) {
            MeterUserListviewItem item = new MeterUserListviewItem();
            item.setMeterID(userLimitCursor.getString(userLimitCursor.getColumnIndex("meter_order_number")));
            if (!userLimitCursor.getString(userLimitCursor.getColumnIndex("user_name")).equals("null")) {
                item.setUserName(userLimitCursor.getString(userLimitCursor.getColumnIndex("user_name")));
            } else {
                item.setUserName("无");
            }
            item.setUserID(userLimitCursor.getString(userLimitCursor.getColumnIndex("user_id")));
            if (!userLimitCursor.getString(userLimitCursor.getColumnIndex("meter_number")).equals("null")) {
                item.setMeterNumber(userLimitCursor.getString(userLimitCursor.getColumnIndex("meter_number")));
            } else {
                item.setMeterNumber("无");
            }
            item.setLastMonthDegree(userLimitCursor.getString(userLimitCursor.getColumnIndex("meter_degrees")));
            item.setLastMonthDosage(userLimitCursor.getString(userLimitCursor.getColumnIndex("last_month_dosage")));
            item.setAddress(userLimitCursor.getString(userLimitCursor.getColumnIndex("user_address")));
            if (userLimitCursor.getString(userLimitCursor.getColumnIndex("uploadState")).equals("false")) {
                item.setUploadState("");
            } else {
                item.setUploadState("已上传");
            }
            if (userLimitCursor.getString(userLimitCursor.getColumnIndex("meterState")).equals("false")) {
                item.setMeterState("未抄");
                item.setIfEdit(R.mipmap.meter_false);
                item.setThisMonthDegree("无");
                item.setThisMonthDosage("无");
            } else {
                item.setMeterState("已抄");
                item.setIfEdit(R.mipmap.meter_true);
                item.setThisMonthDegree(userLimitCursor.getString(userLimitCursor.getColumnIndex("this_month_end_degree")));
                item.setThisMonthDosage(userLimitCursor.getString(userLimitCursor.getColumnIndex("this_month_dosage")));
            }
            userLists.add(item);
        }
        handler.sendEmptyMessage(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == currentPosition) {
                if (data != null) {
                    item.setThisMonthDegree(data.getStringExtra("this_month_end_degree"));
                    item.setThisMonthDosage(data.getStringExtra("this_month_dosage"));
                    item.setIfEdit(R.mipmap.meter_true);
                    item.setMeterState("已抄");
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //cursor游标操作完成以后,一定要关闭
        if (totalCountCursor != null) {
            totalCountCursor.close();
        }
        if (userLimitCursor != null) {
            userLimitCursor.close();
        }
        db.close();
    }
}
