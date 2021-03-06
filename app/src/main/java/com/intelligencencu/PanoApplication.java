package com.intelligencencu;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.baidu.lbsapi.BMapManager;
import com.baidu.lbsapi.MKGeneralListener;
import com.baidu.mapapi.SDKInitializer;

public class PanoApplication extends Application {

    private static PanoApplication mInstance = null;
    public BMapManager mBMapManager = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        SDKInitializer.initialize(getApplicationContext());
        initEngineManager(this);
    }

    public void initEngineManager(Context context) {
        if (mBMapManager == null) {
            mBMapManager = new BMapManager(context);
        }

        if (!mBMapManager.init(new MyGeneralListener())) {
            Toast.makeText(PanoApplication.getInstance().getApplicationContext(), "BMapManager  初始化错误!",
                    Toast.LENGTH_LONG).show();
        }
    }

    public static PanoApplication getInstance() {
        return mInstance;
    }

    // 常用事件监听，用来处理通常的网络错误，授权验证错误等
    public static class MyGeneralListener implements MKGeneralListener {

        @Override
        public void onGetPermissionState(int iError) {
            // 非零值表示key验证未通过
            if (iError != 0) {
                // 授权Key错误：
                Toast.makeText(PanoApplication.getInstance().getApplicationContext(),
                        "请检查您的网络连接是否正常！error: " + iError, Toast.LENGTH_LONG).show();
            } else {
                Log.d("key","key 认证成功！");
            }
        }
    }
}