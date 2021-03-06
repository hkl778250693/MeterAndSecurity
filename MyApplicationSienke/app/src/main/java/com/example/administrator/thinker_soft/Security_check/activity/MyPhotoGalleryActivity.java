package com.example.administrator.thinker_soft.Security_check.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.administrator.thinker_soft.R;
import com.example.administrator.thinker_soft.Security_check.adapter.MyGalleryAdapter;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/4/14 0014.
 */
public class MyPhotoGalleryActivity extends Activity {
    private Gallery myGallery;
    private ImageView back;
    private ImageView delete;
    private MyGalleryAdapter adapter;
    private ArrayList<String> cropPathLists = new ArrayList<>();  //原始的图片路径集合
    private LayoutInflater inflater;  //转换器
    private View popupwindowView,deleteView;
    private PopupWindow popupWindow;
    private TextView photoCurrentNumber,photoNumber;  //当前的照片位置，照片总数
    private RelativeLayout rootLinearlayout;
    private int currentPosition = 0;
    private long currentId;
    private int getCurrentPosition = 0; //上个页面穿过来的位置
    private TextView tips;
    private RadioButton cancelRb,saveRb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail_gallery);

        bindView();
        defaultSetting();
        setOnClickListener();
    }

    //绑定控件
    private void bindView() {
        myGallery = (Gallery) findViewById(R.id.gallery);
        rootLinearlayout = (RelativeLayout) findViewById(R.id.root_linearlayout);
    }

    //初始化设置
    private void defaultSetting() {
        Intent intent = getIntent();
        getCurrentPosition = intent.getIntExtra("currentPosition",0);
        cropPathLists = intent.getStringArrayListExtra("cropPathLists");
        Log.i("MyPhotoGalleryActivity", "获取到的图片路径为："+cropPathLists);
    }

    //点击事件
    private void setOnClickListener() {
        adapter = new MyGalleryAdapter(MyPhotoGalleryActivity.this,cropPathLists);
        myGallery.setAdapter(adapter);
        myGallery.setSelection(getCurrentPosition);
        myGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentId = id;
                currentPosition = position;
                createPopupwindow();
            }
        });
    }

    //弹出详细操作的popupwindow
    public void createPopupwindow() {
        inflater = LayoutInflater.from(MyPhotoGalleryActivity.this);
        popupwindowView = inflater.inflate(R.layout.popupwindow_photo_detail, null);
        popupWindow = new PopupWindow(popupwindowView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //绑定控件ID
        back = (ImageView) popupwindowView.findViewById(R.id.back);
        delete = (ImageView) popupwindowView.findViewById(R.id.delete);
        photoCurrentNumber = (TextView) popupwindowView.findViewById(R.id.photo_current_number);
        photoNumber = (TextView) popupwindowView.findViewById(R.id.photo_number);
        //设置点击事件
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                Intent intent = new Intent();
                intent.putStringArrayListExtra("cropPathLists_back",cropPathLists);
                setResult(RESULT_OK,intent);
                finish();
            }
        });
        photoCurrentNumber.setText(String.valueOf(currentPosition+1));
        photoNumber.setText(String.valueOf(cropPathLists.size()));
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                showDeletePopup();
            }
        });
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.update();
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.color.white_transparent));
        popupWindow.setAnimationStyle(R.style.camera);
        popupWindow.showAtLocation(rootLinearlayout, Gravity.BOTTOM, 0, 0);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {

            }
        });
    }

    //弹出删除照片前提示popupwindow
    public void showDeletePopup() {
        inflater = LayoutInflater.from(MyPhotoGalleryActivity.this);
        deleteView = inflater.inflate(R.layout.popupwindow_user_detail_info_save, null);
        popupWindow = new PopupWindow(deleteView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        //绑定控件ID
        tips = (TextView) deleteView.findViewById(R.id.tips);
        cancelRb = (RadioButton) deleteView.findViewById(R.id.cancel_rb);
        saveRb = (RadioButton) deleteView.findViewById(R.id.save_rb);
        //设置点击事件
        tips.setText("确定要删除该照片吗？");
        saveRb.setText("确定");
        cancelRb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        saveRb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                if(cropPathLists.size() == 1){   //如果还有一张图片，则删除了直接返回上个页面
                    popupWindow.dismiss();
                    File file = new File(cropPathLists.get((int) currentId));
                    file.delete();
                    Log.i("showDeletePopup", "删除本地图片："+currentId);
                    cropPathLists.remove(currentPosition);
                    Log.i("showDeletePopup", "图片集合移除了："+currentPosition);
                    adapter.notifyDataSetChanged();
                    Intent intent = new Intent();
                    intent.putStringArrayListExtra("cropPathLists_back",cropPathLists);
                    setResult(RESULT_OK,intent);
                    finish();
                }else {  //删除刷新
                    popupWindow.dismiss();
                    File file = new File(cropPathLists.get((int) currentId));
                    file.delete();
                    Log.i("showDeletePopup", "多张时删除本地图片："+currentId);
                    cropPathLists.remove(currentPosition);
                    Log.i("showDeletePopup", "多张时图片集合移除了："+currentPosition);
                    adapter.notifyDataSetChanged();
                }
            }
        });
        popupWindow.update();
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.color.white_transparent));
        popupWindow.setAnimationStyle(R.style.camera);
        backgroundAlpha(0.6F);   //背景变暗
        popupWindow.showAtLocation(rootLinearlayout, Gravity.CENTER, 0, 0);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1.0F);
            }
        });
    }

    //设置背景透明度
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = MyPhotoGalleryActivity.this.getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        if (bgAlpha == 1) {
            MyPhotoGalleryActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//不移除该Flag的话,在有视频的页面上的视频会出现黑屏的bug
        } else {
            MyPhotoGalleryActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//此行代码主要是解决在华为手机上半透明效果无效的bug
        }
        MyPhotoGalleryActivity.this.getWindow().setAttributes(lp);
    }

    /**
     * 监听Back键按下事件,方法1:
     * 注意:
     * super.onBackPressed()会自动调用finish()方法,关闭
     * 当前Activity.
     * 若要屏蔽Back键盘,注释该行代码即可
     */
    /*@Override
    public void onBackPressed() {
        super.onBackPressed();
        System.out.println("按下了back键   onBackPressed()");
    }*/

    /**
     * 监听Back键按下事件,方法2:
     * 注意:
     * 返回值表示:是否能完全处理该事件
     * 在此处返回false,所以会继续传播该事件.
     * 在具体项目中此处的返回值视情况而定.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Intent intent = new Intent();
            intent.putStringArrayListExtra("cropPathLists_back",cropPathLists);
            setResult(RESULT_OK,intent);
            finish();
            return false;
        }else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
