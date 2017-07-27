package com.dell.treasure.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.dell.treasure.R;
import com.dell.treasure.support.AppSurvice;
import com.dell.treasure.support.CurrentUser;
import com.orhanobut.logger.Logger;

import java.util.Date;

import static com.dell.treasure.support.NotificationHelper.sendDefaultNotice;
import static com.dell.treasure.support.ToolUtil.stringToDate;

/**
 * 判断任务是否过期
 * 先判断程序是否启动
 * 确定是否联网
 * 任务插入数据库
 */

public class PrepareService extends Service {
    private CurrentUser user;
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        user = CurrentUser.getOnlyUser();
        context = getApplicationContext();

        isOverdue();

    }

    //任务是否过期
    private void isOverdue() {
        Date lostTime = stringToDate(user.getStartTime());

        long lostT = new Date().getTime() - lostTime.getTime();

        Logger.d(new Date()+" "+ lostTime +" "+lostT);
        if(lostT > 60*60*1000*2){
            user.setStartTime("");
            user.setTarget_ble("");
            user.setTaskId("");
            stopSelf();
        }else{
            isAppSurvive();
        }

    }
    //app 是否存活
    private void isAppSurvive() {
        boolean appState = AppSurvice.isAppAlive(context, "com.dell.treasure");
        if(appState){
            startService(new Intent(PrepareService.this,NetService.class));
        }else{
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.dell.treasure");
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            launchIntent.putExtra("tasKind","1");
            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, launchIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            sendDefaultNotice(context,"任务","收到任务，点击进入程序查看详情。",R.mipmap.ic_launcher, pi);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}