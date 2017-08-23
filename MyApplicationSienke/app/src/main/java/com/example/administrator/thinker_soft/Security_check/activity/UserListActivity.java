package com.example.administrator.thinker_soft.Security_check.activity;

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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.administrator.thinker_soft.R;
import com.example.administrator.thinker_soft.Security_check.adapter.UserListviewAdapter;
import com.example.administrator.thinker_soft.mode.MySqliteHelper;
import com.example.administrator.thinker_soft.Security_check.model.UserListviewItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2017/3/16.
 */
public class UserListActivity extends Activity {
    private ImageView back,editDelete;
    private ListView listView;
    private TextView noData,name;
    private List<UserListviewItem> userListviewItemList = new ArrayList<>();
    private ArrayList<String> stringList = new ArrayList<>();//保存字符串参数
    private int task_total_numb = 0;
    private SQLiteDatabase db;  //数据库
    private MySqliteHelper helper; //数据库帮助类
    private int currentPosition;  //点击listview
    private UserListviewAdapter userListviewAdapter;
    private UserListviewItem item;
    private SharedPreferences sharedPreferences,sharedPreferences_login;
    private SharedPreferences.Editor editor;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userlist);

        bindView();//绑定控件
        defaultSetting();//初始化设置
        setOnClickListener();//点击事件
    }

    //绑定控件ID
    private void bindView() {
        back = (ImageView) findViewById(R.id.back);
        name = (TextView) findViewById(R.id.name);
        listView = (ListView) findViewById(R.id.listview);
        noData = (TextView) findViewById(R.id.no_data);
        etSearch = (EditText) findViewById(R.id.etSearch);
        editDelete = (ImageView) findViewById(R.id.edit_delete);
    }

    //初始化设置
    private void defaultSetting() {
        helper = new MySqliteHelper(UserListActivity.this, 1);
        db = helper.getWritableDatabase();
        sharedPreferences_login = this.getSharedPreferences("login_info", Context.MODE_PRIVATE);
        sharedPreferences = this.getSharedPreferences(sharedPreferences_login.getString("userId","")+"data", Context.MODE_PRIVATE);
        if (!"".equals(sharedPreferences.getString("currentTaskName",""))) {
            name.setText(sharedPreferences.getString("currentTaskName",""));
        }else {
            name.setText("用户列表");
        }
        new Thread() {
            @Override
            public void run() {
                if (sharedPreferences.getBoolean("show_temp_data", false)) {   //显示演示数据
                    getTempData();
                }else {
                    if (!"".equals(sharedPreferences.getString("currentTaskId",""))) {
                        getUserData(sharedPreferences.getString("currentTaskId",""), sharedPreferences_login.getString("userId", ""));//读取所有安检用户数据
                        Log.i("UserListActivity----", "查询的任务编号是：" + sharedPreferences.getString("currentTaskId",""));
                    } else {
                        handler.sendEmptyMessage(1);
                    }
                }
            }
        }.start();
    }

    //点击事件
    private void setOnClickListener() {
        back.setOnClickListener(onClickListener);
        editDelete.setOnClickListener(onClickListener);
        listView.setTextFilterEnabled(true);  // 开启过滤功能
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("UserListActivity", "userListviewItemList点击事件进来了，集合长度为：" + userListviewItemList.size());
                //item = userListviewItemList.get((int) parent.getAdapter().getItemId(position));
                item = (UserListviewItem) userListviewAdapter.getItem(position);
                currentPosition = position;
                Intent intent = new Intent(UserListActivity.this, UserDetailInfoActivity.class);
                intent.putExtra("user_new_id", item.getUserNewId());
                intent.putExtra("taskId", item.getTaskId());
                intent.putExtra("if_upload", item.getIfUpload());
                startActivityForResult(intent, currentPosition);
            }
        });
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("UserListActivity", "onTextChanged进来了");
                if (TextUtils.isEmpty(s.toString().trim())) {
                    listView.clearTextFilter();    //搜索文本为空时，清除ListView的过滤
                    if (userListviewAdapter != null) {
                        userListviewAdapter.getFilter().filter("");
                    }
                    if (editDelete.getVisibility() == View.VISIBLE) {
                        editDelete.setVisibility(View.GONE);  //当输入框为空时，叉叉消失
                    }
                } else {
                    if (userListviewAdapter != null) {
                        userListviewAdapter.getFilter().filter(s);
                    }
                    //listView.setFilterText(s.toString().trim());  //设置过滤关键字
                    if (editDelete.getVisibility() == View.GONE) {
                        editDelete.setVisibility(View.VISIBLE);  //反之则显示
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.i("UserListActivity_after", "afterTextChanged进来了");
            }
        });
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.back:
                    UserListActivity.this.finish();
                    break;
                case R.id.edit_delete:
                    etSearch.setText("");
                    editDelete.setVisibility(View.GONE);
                    if (userListviewAdapter != null) {
                        userListviewAdapter.getFilter().filter("");
                    }
                    break;
                default:
                    break;
            }
        }
    };

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    Log.i("UserListActivity", "UserListActivit的数据源长度为："+userListviewItemList.size());
                    userListviewAdapter = new UserListviewAdapter(UserListActivity.this, userListviewItemList);
                    userListviewAdapter.notifyDataSetChanged();
                    listView.setAdapter(userListviewAdapter);
                    break;
                case 1:
                    noData.setVisibility(View.VISIBLE);
                    break;
            }
            super.handleMessage(msg);
        }
    };


    //获取任务编号参数
    public void getTaskParams() {
        if (sharedPreferences.getStringSet("stringSet", null) != null && sharedPreferences.getInt("task_total_numb", 0) != 0) {
            Iterator iterator = sharedPreferences.getStringSet("stringSet", null).iterator();
            while (iterator.hasNext()) {
                stringList.add(iterator.next().toString());
                Log.i("UserListActivity====>", "得到的参数为：" +stringList);
            }
            task_total_numb = sharedPreferences.getInt("task_total_numb", 0);
        }
    }

    /**
     * 查询方法参数详解
     * <p/>
     * public Cursor query (boolean distinct, String table, String[] columns,
     * String selection, String[] selectionArgs,
     * String groupBy, String having,
     * String orderBy, String limit,
     * CancellationSignal cancellationSignal)
     * <p/>
     * 参数介绍 :
     * <p/>
     * -- 参数① distinct : 是否去重复, true 去重复;
     * <p/>
     * -- 参数② table : 要查询的表名;
     * <p/>
     * -- 参数③ columns : 要查询的列名, 如果为null, 就会查询所有的列;
     * <p/>
     * -- 参数④ whereClause : 条件查询子句, 在这里可以使用占位符 "?";
     * <p/>
     * -- 参数⑤ whereArgs : whereClause查询子句中的传入的参数值, 逐个替换 "?" 占位符;
     * <p/>
     * -- 参数⑥ groupBy: 控制分组, 如果为null 将不会分组;
     * <p/>
     * -- 参数⑦ having : 对分组进行过滤;
     * <p/>
     * -- 参数⑧ orderBy : 对记录进行排序;
     * <p/>
     * -- 参数⑨ limite : 用于分页, 如果为null, 就认为不进行分页查询;
     * <p/>
     * -- 参数⑩ cancellationSignal : 进程中取消操作的信号, 如果操作被取消, 当查询命令执行时会抛出 OperationCanceledException 异常;
     */

    //读取下载到本地的用户数据
    public void getUserData(String taskId, String loginUserId) {
        Cursor cursor = db.rawQuery("select * from User where taskId=? and loginUserId=?", new String[]{taskId, loginUserId});//查询并获得游标
        Log.i("UserListActivityget=", "任务编号是：" + taskId);
        Log.i("UserListActivityget=", "有" + cursor.getCount() + "条数据！");
        //如果游标为空，则显示没有数据图片
        if (cursor.getCount() == 0) {
            handler.sendEmptyMessage(1);
            return;
        }
        while (cursor.moveToNext()) {
            UserListviewItem userListviewItem = new UserListviewItem();
            userListviewItem.setSecurityNumber(cursor.getString(cursor.getColumnIndex("securityNumber")));
            userListviewItem.setUserName(cursor.getString(cursor.getColumnIndex("userName")));
            userListviewItem.setTaskId(cursor.getString(cursor.getColumnIndex("taskId")));
            userListviewItem.setNumber(cursor.getString(cursor.getColumnIndex("meterNumber")));
            userListviewItem.setPhoneNumber(cursor.getString(cursor.getColumnIndex("userPhone")));
            userListviewItem.setSecurityType(cursor.getString(cursor.getColumnIndex("securityType")));
            userListviewItem.setUserId(cursor.getString(cursor.getColumnIndex("oldUserId")));
            userListviewItem.setUserNewId(cursor.getString(cursor.getColumnIndex("newUserId")));
            userListviewItem.setAdress(cursor.getString(cursor.getColumnIndex("userAddress")));
            userListviewItem.setUserProperty(cursor.getString(cursor.getColumnIndex("userProperty")));
            Log.i("UserList=cursor", "安检状态为 = " + cursor.getString(cursor.getColumnIndex("ifChecked")));
            if (cursor.getString(cursor.getColumnIndex("ifChecked")).equals("true")) {
                Log.i("UserList=cursor", "安检状态为true");
                userListviewItem.setIfEdit(R.mipmap.meter_true);
                userListviewItem.setIfChecked("已检");
            } else {
                Log.i("UserList=cursor", "安检状态为false");
                userListviewItem.setIfEdit(R.mipmap.meter_false);
                userListviewItem.setIfChecked("未检");
            }
            if (cursor.getString(cursor.getColumnIndex("ifUpload")).equals("true")) {
                userListviewItem.setIfUpload("已上传");
            } else {
                userListviewItem.setIfUpload("");
            }
            userListviewItemList.add(userListviewItem);
            Log.i("UserListActivityget=", "用户列表的长度为：" + userListviewItemList.size());
        }
        cursor.close(); //游标关闭
        handler.sendEmptyMessage(0);
    }

    public void getTempData(){
        for(int i = 0;i<20;i++){
            UserListviewItem userListviewItem = new UserListviewItem();
            userListviewItem.setSecurityNumber("001");
            userListviewItem.setUserName("马云");
            userListviewItem.setTaskId("007");
            userListviewItem.setNumber("002");
            userListviewItem.setPhoneNumber("123456789");
            userListviewItem.setSecurityType("年度安检");
            userListviewItem.setUserId("110");
            userListviewItem.setUserNewId("010");
            userListviewItem.setAdress("重庆市江北区鲁溉路星耀天地");
            userListviewItem.setUserProperty("居民");
            if(i == 0 || i == 2){
                userListviewItem.setIfEdit(R.mipmap.meter_true);
                userListviewItem.setIfChecked("已检");
                userListviewItem.setIfUpload("已上传");
            }else {
                userListviewItem.setIfEdit(R.mipmap.meter_false);
                userListviewItem.setIfChecked("未检");
                userListviewItem.setIfUpload("");
            }
            userListviewItemList.add(userListviewItem);
        }
        handler.sendEmptyMessage(0);
    }

    //更新用户表是否安检状态
    public void updateUserCheckedState() {
        ContentValues values = new ContentValues();
        values.put("ifChecked", "true");
        Log.i("UserList=update", "更新安检状态为true");
        db.update("User", values, "securityNumber=? and loginUserId=?", new String[]{item.getSecurityNumber(), sharedPreferences_login.getString("userId", "")});
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == currentPosition) {
                updateUserCheckedState(); //更新本地数据库用户表安检状态
                item.setIfEdit(R.mipmap.meter_true);
                item.setIfChecked("已检");
                userListviewAdapter.notifyDataSetChanged();
                Log.i("UserList=ActivityResult", "页面回调进来了");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        db.close(); //释放和数据库的连接
        UserListviewAdapter.searchContent="";
    }
}
