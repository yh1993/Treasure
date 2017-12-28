package com.dell.treasure.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.dell.treasure.R;
import com.dell.treasure.dao.Task;
import com.dell.treasure.source.TasksRepository;
import com.dell.treasure.source.local.TasksLocalDataSource;
import com.dell.treasure.support.CurrentUser;
import com.dell.treasure.tasks.TaskDetails;
import com.orhanobut.logger.Logger;

import java.util.Date;

import static com.dell.treasure.support.NotificationHelper.sendDefaultNotice;
import static com.dell.treasure.support.ToolUtil.stringToDate;

/**
 * 判断任务是否已参与
 * 判断任务是否过期
 * 加入数据库
 */

public class PrepareService extends Service {
    private CurrentUser user;
    private Task currentTask;
    private Context context;

    private String taskId;
    private String bleId;
    private String date;
    private String money;
    private String needNum;

    private TasksRepository mtasksRepository;
    private TasksLocalDataSource mtasksLocalDataSource;

    @Override
    public void onCreate() {
        super.onCreate();
        user = CurrentUser.getOnlyUser();
        currentTask = user.getCurrentTask();
        Logger.d(currentTask.toString()+" "+currentTask.hashCode());
        context = getApplicationContext();

        mtasksLocalDataSource = TasksLocalDataSource.getInstance();
        mtasksRepository = TasksRepository.getInstance(mtasksLocalDataSource);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        if(bundle != null){
            taskId = bundle.getString("taskId");
            bleId = bundle.getString("bleId");
            date = bundle.getString("date");
            money = bundle.getString("money");
            needNum = bundle.getString("needNum");
            isHaveParti();
        }
        return super.onStartCommand(intent, flags, startId);
    }


    private void isHaveParti(){
        if(currentTask.getTaskId() != null && currentTask.getTaskId().equals(taskId)){
            //已经参与该任务
            stopSelf();
        }else if(mtasksRepository.isTaskExist(taskId)) {
            stopSelf();
        }else {
            isOverdue();
        }
    }

    //任务是否过期
    private void isOverdue() {
        Date lostTime = stringToDate(date);
        long lostT = new Date().getTime() - lostTime.getTime();

        Logger.d(new Date()+" "+ lostTime +" "+lostT);
        if(lostT > 60*60*1000*2){
            stopSelf();
        }else{
//            isAppSurvive();
            start();
        }
    }

    private void start(){
        insertTask();
        Intent taskIntent = new Intent(context, TaskDetails.class);
//        user.setTasKind("1");
        PendingIntent pi = PendingIntent.getActivity(context, 0, taskIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        sendDefaultNotice(getApplicationContext(), "任务", "收到任务，点击进入程序查看详情。", R.mipmap.ic_launcher, pi);
        stopSelf();
    }

    private void insertTask(){
        currentTask.setTask(new Task(null,"0",taskId,bleId,date,null,0.0,"0",needNum,"0","0",money,-2));
        Logger.d(currentTask.toString()+" "+currentTask.hashCode());
        mtasksRepository.saveTask(currentTask);

    }
    //app 是否存活
//    private void isAppSurvive() {
//        boolean appState = AppSurvice.isAppAlive(context, "com.dell.treasure");
//        if(appState){
//            startService(new Intent(PrepareService.this,NetService.class));
//        }else{
//            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.dell.treasure");
//            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//            launchIntent.putExtra("tasKind","1");
//            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, launchIntent, PendingIntent.FLAG_CANCEL_CURRENT);
//            sendDefaultNotice(context,"任务","收到任务，点击进入程序查看详情。",R.mipmap.ic_launcher, pi);
//        }
//        stopSelf();
//    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}