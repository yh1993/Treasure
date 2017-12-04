package com.dell.treasure.service;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.LocationMode;
import com.baidu.trace.OnEntityListener;
import com.baidu.trace.OnStartTraceListener;
import com.baidu.trace.OnStopTraceListener;
import com.baidu.trace.Trace;
import com.baidu.trace.TraceLocation;
import com.dell.treasure.support.CurrentUser;
import com.dell.treasure.support.MyApp;
import com.orhanobut.logger.Logger;

/**
 * Created by DELL on 2016/11/30.
 * 轨迹上传
 */

public class TraceService extends Service {
    private static final String TAG = "TraceService";
    public static boolean running = false;
    static boolean isTraceStart = false;
    public MyApp myApp;
    //entity标识
    String entityName = null;
    // 轨迹服务客户端
    LBSTraceClient client = null;
    // 轨迹服务
    Trace trace = null;
    // Entity监听器
    OnEntityListener entityListener = null;
    // 开启轨迹服务监听器
    OnStartTraceListener startTraceListener = null;
    // 停止轨迹服务监听器
    OnStopTraceListener stopTraceListener = null;
    private String userId;

    @Override
    public void onCreate() {
        super.onCreate();
        running = true;
        Logger.d(" TraceService start");
        myApp=(MyApp)getApplication();
        userId = CurrentUser.getOnlyUser().getUserId();

        init();
    }

    private void init() {
        //实例化轨迹服务客户端
        client = myApp.getClient();
        // 采集周期
        int gatherInterval = 10;
        // 打包周期
        int packInterval = 60;
        // http协议类型
        int protocolType = 1;
        // 设置采集和打包周期
        client.setInterval(gatherInterval, packInterval);
        // 设置定位模式
        client.setLocationMode(LocationMode.High_Accuracy);
        // 设置http协议类型
        client.setProtocolType (protocolType);
        entityName = userId;
        //实例化轨迹服务
        trace = myApp.getTrace(entityName);
//      初始化监听器
        initListener();
//      启动轨迹上传
        startTrace();
    }

    private void startTrace() {
        //开启轨迹服务
        Log.d("result",TAG+" 开启"+userId+"轨迹");
        client.startTrace(trace, startTraceListener);
    }

    private void initListener() {
        //Entity监听器
        initOnEntityListener();
        // 初始化开启轨迹服务监听器
        initOnStartTraceListener();
        // 初始化停止轨迹服务监听器
        initOnStopTraceListener();
    }

    private void initOnEntityListener() {
        entityListener = new OnEntityListener() {
            @Override
            public void onRequestFailedCallback(String s) {
                Log.d(TAG, "entity请求失败回调接口消息 : " + s);
            }

            @Override
            public void onQueryEntityListCallback(String s) {
                super.onQueryEntityListCallback(s);
            }

            @Override
            public void onAddEntityCallback(String s) {
                super.onAddEntityCallback(s);
                Log.d(TAG, "添加entity回调接口消息 : " + s);
            }

            @Override
            public void onUpdateEntityCallback(String s) {
                super.onUpdateEntityCallback(s);
            }

            @Override
            public void onReceiveLocation(TraceLocation traceLocation) {
                super.onReceiveLocation(traceLocation);
            }
        };
    }

    private void initOnStartTraceListener(){
        //实例化开启轨迹服务回调接口
        startTraceListener = new OnStartTraceListener() {
            //开启轨迹服务回调接口（arg0 : 消息编码，arg1 : 消息内容，详情查看类参考）
            @Override
            public void onTraceCallback(int arg0, String arg1) {
                Log.d("result",arg0 + " "+arg1);
                Log.d(TAG,"开启轨迹回调接口 [消息编码 : " + arg0 +" ,消息内容 : " + arg1 + "]");
                if(arg0 == 0 || arg0 == 10006){
                    isTraceStart = true;
                }
            }
            //轨迹服务推送接口（用于接收服务端推送消息，arg0 : 消息类型，arg1 : 消息内容，详情查看类参考）
            @Override
            public void onTracePushCallback(byte arg0, String arg1) {
            }
        };
    }

    private void initOnStopTraceListener(){
        //实例化停止轨迹服务回调接口
        stopTraceListener = new OnStopTraceListener(){
            // 轨迹服务停止成功
            @Override
            public void onStopTraceSuccess() {
                Log.d(TAG,"轨迹服务停止");
                isTraceStart = false;
                stopSelf();
            }
            // 轨迹服务停止失败（arg0 : 错误编码，arg1 : 消息内容，详情查看类参考）
            @Override
            public void onStopTraceFailed(int arg0, String arg1) {
                Log.d(TAG,"轨迹服务停止失败[错误编码: " + arg0 +" ,消息内容: " + arg1 + "]");
            }
        };

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTrace();
        running = false;
    }

    private void stopTrace() {
        //停止轨迹服务
        if(isTraceStart) {
            client.stopTrace(trace, stopTraceListener);
        }
    }
}
