package com.dell.treasure.support;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

import com.dell.treasure.SignInActivity;
import com.dell.treasure.service.AdvertiserService;
import com.dell.treasure.service.ScannerService;
import com.orhanobut.logger.Logger;

import java.util.Date;

import static com.dell.treasure.support.ToolUtil.stringToDate;

/**
 * Created by yh on 2017/12/26.
 */

public class TaskRelated {

    //任务是否过期
    public static boolean isOverdue(String startTime) {
        if(startTime ==""||startTime == null){
            return true;
        }
        Date lostTime = stringToDate(startTime);
        long lostT = new Date().getTime() - lostTime.getTime();

        if(lostT > 60*60*1000*2){
            return true;
        }else{
            return false;
        }
    }
    public static void endTask(Context context,CurrentUser user){
        SharedPreferences sp = context.getSharedPreferences(JpushReceiver.TASK, Context.MODE_PRIVATE);
        SharedPreferences sp1 = context.getSharedPreferences(SignInActivity.USER_INFO, Context.MODE_PRIVATE);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        Intent taskEnd = new Intent("com.dell.treasure.RECEIVER_TaskInfo");
        taskEnd.putExtra("isEnd",true);
        localBroadcastManager.sendBroadcast(taskEnd);

        ScannerService.isFirst = 0;
        if (ScannerService.running) {
            Intent scanIntent = new Intent(context, ScannerService.class);
            context.stopService(scanIntent);
        }
        if (AdvertiserService.running) {
            Intent i = new Intent(context, AdvertiserService.class);
            context.stopService(i);
        }

//        user.currentTaskClear();
//        user.setTaskId(null);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("isFound", false);
        editor.apply();

        SharedPreferences.Editor editor1 = sp1.edit();
        editor1.putString("taskId",null);
        editor1.apply();

        JpushReceiver.running = false;
    }

}
