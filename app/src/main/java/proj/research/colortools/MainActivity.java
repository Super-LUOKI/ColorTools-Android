package proj.research.colortools;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.VectorDrawable;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.nio.ByteBuffer;

import proj.research.colortools.bean.ColorValue;
import proj.research.colortools.fragment.ColorFragment;
import proj.research.colortools.fragment.FuncFragment;
import proj.research.colortools.util.ColorUtils;
import proj.research.colortools.util.Local;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //导航栏控件
    private ImageView mColorIv;
    private TextView mColorTv;
    private View mColorTab;

    private ImageView mFuncIv;
    private TextView mFuncTv;
    private View mFuncTab;

    private FrameLayout mContentFl;

    //界面
    private ColorFragment mColorFragment;
    private FuncFragment mFuncFragment;

    // 标记
    private static final String TAG = "MainActivity";
    private static final String TABBAR_COLOR = "Color";
    private static final String TABBAR_FUNC = "Func";
    private static final int REQUEST_MEDIA_PROJECTION = 1;
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private ImageReader mImageReader;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initPermission();
        initEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCVLoader.initDebug(), not working.");
        } else {
            Log.d(TAG, "OpenCVLoader.initDebug(), working.");
        }
        //OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, getApplicationContext(), mLoaderCallback);
    }

    private void initPermission() {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        if (Local.checkPermission(this, permissions) != null) {
            Toast.makeText(this, "请授权权限", Toast.LENGTH_SHORT).show();
            Local.requestPermission(this, Local.checkPermission(this, permissions));
        } else {
            changeTabItem(TABBAR_COLOR);
            //changeTabItem(TABBAR_FUNC);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Local.REQUEST_CODE_ASK_PERMISSIONS) {
            if (Local.checkPermission(this, permissions) == null) {
                changeTabItem(TABBAR_COLOR);
            } else {
                Toast.makeText(this, "请授权权限", Toast.LENGTH_SHORT).show();
                Local.requestPermission(this, Local.checkPermission(this, permissions));
            }
        }
    }

    private void initEvent() {
        mColorTab.setOnClickListener(this);
        mFuncTab.setOnClickListener(this);
    }

    private void initView() {
        mColorIv = findViewById(R.id.iv_tabbar_color);
        mColorTv = findViewById(R.id.tv_tabbar_color);
        mFuncIv = findViewById(R.id.iv_tabbar_func);
        mFuncTv = findViewById(R.id.tv_tabbar_func);

        mColorTab = findViewById(R.id.ll_tabbar_color);
        mFuncTab = findViewById(R.id.ll_tabbar_func);

        mContentFl = findViewById(R.id.fl_content);


        mColorFragment = new ColorFragment(this);
        mFuncFragment = new FuncFragment(this);

    }

    /**
     * 切换导航栏选中状态
     *
     * @param activeFlag
     */
    private void changeTabItem(String activeFlag) {
        //导航栏图片控件列表
        ImageView[] ivList = {mColorIv, mFuncIv};
        //导航栏文字控件列表
        TextView[] tvList = {mColorTv, mFuncTv};
        //导航栏图片资源列表
        int[] ivResList = {R.drawable.ic_shape_color, R.drawable.ic_shape_func};
        int[] ivResActiveList = {R.drawable.ic_shape_color_active, R.drawable.ic_shape_func_active};
        //激活/未激活颜色
        int activeColor = getColor(R.color.primary);
        int inactiveColor = getColor(R.color.light_grey);

        //将所有导航栏图标和文字设置为未选中状态
        for (int i = 0; i < ivList.length; i++) {
            VectorDrawable drawable = (VectorDrawable) getDrawable(ivResList[i]);
            ivList[i].setImageDrawable(drawable);
            tvList[i].setTextColor(inactiveColor);
        }


        //设置选中样式，并切换页面
        VectorDrawable activeDrawable = null;
        switch (activeFlag) {
            case TABBAR_COLOR:
                replaceFragment(mColorFragment);
                activeDrawable = (VectorDrawable) getDrawable(ivResActiveList[0]);
                mColorIv.setImageDrawable(activeDrawable);
                mColorTv.setTextColor(activeColor);
                break;
            case TABBAR_FUNC:
                replaceFragment(mFuncFragment);
                activeDrawable = (VectorDrawable) getDrawable(ivResActiveList[1]);
                mFuncIv.setImageDrawable(activeDrawable);
                mFuncTv.setTextColor(activeColor);
                break;
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.fl_content, fragment).commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_tabbar_color:
                changeTabItem(TABBAR_COLOR);
                break;
            case R.id.ll_tabbar_func:
                changeTabItem(TABBAR_FUNC);
                break;
        }
    }
}