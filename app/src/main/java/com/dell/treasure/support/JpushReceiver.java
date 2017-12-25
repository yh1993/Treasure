package com.dell.treasure.support;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.dell.treasure.SignInActivity;
import com.dell.treasure.dao.Task;
import com.dell.treasure.dao.TaskDao;
import com.dell.treasure.service.AdvertiserService;
import com.dell.treasure.service.AlarmService;
import com.dell.treasure.service.PrepareService;
import com.dell.treasure.service.ScannerService;
import com.dell.treasure.service.TraceService;
import com.orhanobut.logger.Logger;

import org.greenrobot.greendao.query.Query;

import java.util.List;

import cn.jpush.android.api.JPushInterface;


/**
 * 推送广播接收
 */
public class JpushReceiver extends BroadcastReceiver {

    public static final String TAG = "JpushReceiver";
    public static final String TASK = "task";
    public static boolean running = false;
    private Task task;
    private TaskDao taskDao;
    private SharedPreferences sp;
    private CurrentUser user;
    private LocalBroadcastManager localBroadcastManager;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("result",TAG);
        user = CurrentUser.getOnlyUser();
        Bundle bundle = intent.getExtras();
        sp = context.getSharedPreferences(TASK, Context.MODE_PRIVATE);
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {

        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())){
            /**
             * 自定义消息，用户不可见，推送求助任务，所有人都能接收到，并保存到数据库中(保存数据库也移到PrepareService中)
             * 自定义消息中包括，bleId,经纬度log,lat,date,taskId,以'+'分隔
             *
             * 将判断任务是否过期移到PrepareService中
             */
            //获取自定义消息的内容字段
            String extra = bundle.getString(JPushInterface.EXTRA_MESSAGE);
            if(extra!= null && extra.length() >= 3 ){

                String mFlag = extra.substring(0,3);
                if (mFlag.equals("all") && !running) {
                    Logger.d(TAG+"onReceive: "+AdvertiserService.running);
                    String[] split = extra.split("\\+");
                    String taskId = split[1];
                    String bleId = split[2];
                    String log = split[3];
                    String lat = split[4];
                    String date = split[5];
                    String money = split[6];
                    String needNum = split[7];

                    taskDao = MyApp.getInstance().getDaoSession().getTaskDao();

                    Query<Task> taskQueryCur = taskDao.queryBuilder().where(TaskDao.Properties.TaskId.eq(taskId)).build();
                    List<Task> tasksCur = taskQueryCur.list();
                    if(tasksCur.size() > 0){
                        return;
                    }
                    Query<Task> taskQuery = taskDao.queryBuilder().where(TaskDao.Properties.Flag.ge(-3),TaskDao.Properties.Flag.le(1)).build();
                    List<Task> tasks = taskQuery.list();
                    for(Task task: tasks){
                        task.setFlag(3);
                        Logger.d(task.getTaskId()+task.getFlag());
                        taskDao.update(task);
                        Logger.d(task.getTaskId()+task.getFlag());
                    }
                    stopTask(context);
                    if (!AdvertiserService.running) {

                        user.setLastId("0");
                        user.setStartTime(date);
                        user.setTarget_ble(bleId);
                        user.setTaskId(taskId);
                        user.setNeedNum(needNum);

                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("startTime",date);  //任务开始时间
                        editor.putString("money",money);
                        editor.putString("needNum",needNum);
                        editor.apply();
                        Log.d("result", TAG + "收到task id date needNum" +taskId + date+needNum);

                        Intent posIntent = new Intent(context, PrepareService.class);
                        context.startService(posIntent);
                    }
                }else if(mFlag.equals("001")){
                    CurrentUser.getOnlyUser().setCurrentState("001");
                }else if(mFlag.equals("004")){

//                    user.setTarget_ble("");
//                    user.setLastId("");
                    stopTask(context);
                    running = false;

                    taskDao = MyApp.getInstance().getDaoSession().getTaskDao();
                    Query<Task> taskQuery = taskDao.queryBuilder().where(TaskDao.Properties.Flag.ge(-3),TaskDao.Properties.Flag.le(1)).build();
                    List<Task> tasks = taskQuery.list();
                    for(Task task : tasks){
                        task.setFlag(3);
                        Logger.d(task.getTaskId()+task.getFlag());
                        taskDao.update(task);
                        Logger.d(task.getTaskId()+task.getFlag());
                    }
                    Intent taskEnd = new Intent("com.dell.treasure.RECEIVER_TaskInfo");
                    taskEnd.putExtra("isEnd",true);
                    localBroadcastManager.sendBroadcast(taskEnd);
                }
                running = false;
            }
            // 自定义消息不会展示在通知栏，完全要开发者写代码去处理
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            //通过推送确定任务进行阶段， 000 注册招募阶段  001 招募阶段结束，等待任务到来  002 任务开始阶段  003 任务结束

            //获取通知的内容字段
//            String extra = bundle.getString(JPushInterface.EXTRA_ALERT);
//
//            if(bundle.getString(JPushInterface.EXTRA_ALERT).length()>20) {
////            if(bundle.getString(JPushInterface.EXTRA_ALERT).equals(user.getTaskId()+" is Done")) {
////                if(TraceService.running){
////                    Intent i = new Intent(context,TraceService.class);
////                    context.stopService(i);
////                }
//                user.setTarget_ble("");
//                user.setLastId("0");
//                ScannerService.isFirst = 0;
//                if (ScannerService.running) {
//                    Intent scanIntent = new Intent(context, ScannerService.class);
//                    context.stopService(scanIntent);
//                }
//                if (AdvertiserService.running) {
//                    Intent i = new Intent(context, AdvertiserService.class);
//                    context.stopService(i);
//                }
//                user.setBeginTime("");
//                user.setEndTime("");
//                Intent intentAlarm = new Intent(context,AlarmService.class);
//                context.startService(intentAlarm);
//            }

            // 在这里可以做些统计，或者做些其他工作
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            // 在这里可以自己写代码去定义用户点击后的行为
        } else {
            Log.d("result", "Unhandled intent - " + intent.getAction());
        }
    }

    void stopTask(Context context){
        ScannerService.isFirst = 0;
        if (ScannerService.running) {
            Intent scanIntent = new Intent(context, ScannerService.class);
            context.stopService(scanIntent);
        }
        if (AdvertiserService.running) {
            Intent i = new Intent(context, AdvertiserService.class);
            context.stopService(i);
        }
//                    user.setBeginTime("");
//                    user.setEndTime("");
//                    Intent intentAlarm = new Intent(context,AlarmService.class);
//                    context.startService(intentAlarm);
        user.clear();
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("isFound", false);
        editor.putString("level", "0");
        editor.putString("num","0");
        editor.apply();
    }

}

