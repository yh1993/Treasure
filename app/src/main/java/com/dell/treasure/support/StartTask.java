package com.dell.treasure.support;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.dell.treasure.dao.DaoSession;
import com.dell.treasure.dao.Task;
import com.dell.treasure.dao.TaskDao;
import com.dell.treasure.service.MonitorService;
import com.dell.treasure.service.TraceService;
import com.orhanobut.logger.Logger;

import org.greenrobot.greendao.query.Query;

import java.util.Date;
import java.util.List;

import static com.dell.treasure.support.ToolUtil.dateToString;

/**
 * Created by DELL on 2017/7/19.
 * 执行任务的开始和结束
 */

public class StartTask {
    private static CurrentUser user;
    private static MyApp myApp;
    private static Task task;
    private static TaskDao taskDao;
    private static Intent serviceTrace = null;
    private static Intent serviceIntent = null;


    public static void init(){
        user = CurrentUser.getOnlyUser();
        myApp = MyApp.getInstance();
        task = Task.getInstance();

        initTaskDB();
    }

    public static void startTask(Context context) {
        user.setBeginTime(dateToString(new Date()));

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
//        Bundle mBundle = new Bundle();
//        mBundle.putParcelable("Task",task);
        Logger.d("任务标志 "+task.getFlag());
//        serviceIntent.putExtras(mBundle);
        context.startService(serviceIntent);
    }

    private static void initTaskDB() {
        DaoSession daoSession = myApp.getDaoSession();
        taskDao = daoSession.getTaskDao();

        if(task == null) {
            Log.d("result","task == null"+task.getId()+task.getLastId());
            Query<Task> taskQuery = taskDao.queryBuilder().whereOr(TaskDao.Properties.Flag.eq(-1),TaskDao.Properties.Flag.eq(0)).build();
            List<Task> tasks = taskQuery.list();
            if (tasks.size() > 0) {
                task.setTask(tasks.get(0));
            }
        }
        task.setLastId(user.getLastId());
        task.setBeginTime(user.getBeginTime());
        task.setFlag(0);
        Log.d("result",task.getId() + task.getLastId());
        taskDao.update(task);
    }

    public static void endTask(Context context){
        if(user.getEndTime().isEmpty()) {
            user.setEndTime(dateToString(new Date()));
        }
        task.setEndTime(user.getEndTime());
        task.setFlag(1);
        taskDao.update(task);

        // 停止监听service
        MonitorService.isCheck = false;
        if (null != serviceIntent) {
            context.stopService(serviceIntent);
        }
        if(serviceTrace != null){
            context.startService(serviceTrace);
        }

        Logger.d("endtime: "+task.getEndTime()+" 任务标志 "+task.getFlag());
    }
}
