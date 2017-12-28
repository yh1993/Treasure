package com.dell.treasure.support;

import android.content.Context;
import android.content.Intent;

import com.dell.treasure.service.MonitorService;
import com.dell.treasure.service.TraceService;

/**
 * Created by DELL on 2017/7/19.
 * 执行任务的开始和结束
 */

public class StartTask {

    private static Intent serviceTrace = null;
    private static Intent serviceIntent = null;


    public static void startTask(Context context) {
        serviceTrace = new Intent(context,TraceService.class);

        if(!TraceService.running) {
            context.startService(serviceTrace);
        }
        if(!MonitorService.isRunning) {
            // 开启监听service
            MonitorService.isCheck = true;
            startMonitorService(context);
        }

    }

    public static void startMonitorService(Context context) {
        serviceIntent = new Intent(context, MonitorService.class);
        context.startService(serviceIntent);
    }

    public static void endTask(Context context){
        // 停止监听service
        MonitorService.isCheck = false;
        if (null != serviceIntent) {
            context.stopService(serviceIntent);
        }
        if(serviceTrace != null){
            context.stopService(serviceTrace);
        }
    }
}
