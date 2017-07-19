package com.dell.treasure.support;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.dell.treasure.service.AdvertiserService;
import com.dell.treasure.service.AlarmService;
import com.dell.treasure.service.PrepareService;
import com.dell.treasure.service.ScannerService;
import com.dell.treasure.service.TraceService;

import cn.jpush.android.api.JPushInterface;


/**
 * 推送广播接收
 */
public class JpushReceiver extends BroadcastReceiver {

    public static final String TAG = "JpushReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("result",TAG);
        CurrentUser user = CurrentUser.getOnlyUser();
        Bundle bundle = intent.getExtras();

        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {

        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction()) ) {
            /**
             * 自定义消息，用户不可见，推送求助任务，所有人都能接收到，并保存到数据库中(保存数据库也移到PrepareService中)
             * 自定义消息中包括，bleId,经纬度log,lat,date,taskId,以'+'分隔
             *
             * 将判断任务是否过期移到PrepareService中
             */
            String extra = bundle.getString(JPushInterface.EXTRA_MESSAGE);
            if(extra!= null && extra.length() > 3 && extra.substring(0,3).equals("all")) {
                if(!AdvertiserService.running) {
                    String[] split = extra.split("\\+");
                    String taskId = split[1];
                    String bleId = split[2];
                    String log = split[3];
                    String lat = split[4];
                    String date = split[5];
                    String money = split[6];

                    user.setLastId("0");
                    user.setStartTime(date);
                    user.setTarget_ble(bleId);
                    user.setTaskId(taskId);

                    Log.d("result",TAG + "收到task");

                    Intent posIntent = new Intent(context, PrepareService.class);
                    context.startService(posIntent);
                }
            }
            // 自定义消息不会展示在通知栏，完全要开发者写代码去处理
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {

            if(bundle.getString(JPushInterface.EXTRA_ALERT).length()>20) {
//            if(bundle.getString(JPushInterface.EXTRA_ALERT).equals(user.getTaskId()+" is Done")) {
                if(TraceService.running){
                    Intent i = new Intent(context,TraceService.class);
                    context.stopService(i);
                }
                user.setTarget_ble("");
                user.setLastId("");
                ScannerService.isFirst = 0;
                if (ScannerService.running) {
                    Intent scanIntent = new Intent(context, ScannerService.class);
                    context.stopService(scanIntent);
                }
                if (AdvertiserService.running) {
                    Intent i = new Intent(context, AdvertiserService.class);
                    context.stopService(i);
                }
                user.setBeginTime("");
                user.setEndTime("");
                Intent intentAlarm = new Intent(context,AlarmService.class);
                context.startService(intentAlarm);
            }

            // 在这里可以做些统计，或者做些其他工作
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            // 在这里可以自己写代码去定义用户点击后的行为
        } else {
            Log.d("result", "Unhandled intent - " + intent.getAction());
        }
    }

}

