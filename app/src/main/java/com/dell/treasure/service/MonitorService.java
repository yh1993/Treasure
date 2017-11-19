package com.dell.treasure.service;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.dell.treasure.support.MyApp;
import com.dell.treasure.dao.DaoSession;
import com.dell.treasure.dao.Task;
import com.dell.treasure.dao.TaskDao;
import com.orhanobut.logger.Logger;

import java.util.Date;
import java.util.List;

import static com.dell.treasure.support.ToolUtil.dateToString;

/**
 * 判断广播服务是否运行，以广播时长作为用户参与任务时长 (废弃)
 *
 * 判断扫描服务是否运行，以参与任务后，开启的扫描时长作为用户参与任务时长
 */
public class MonitorService extends Service {
    private TaskDao taskDao;
    private Task task;
    public static boolean isCheck = false;
    public static boolean isRunning = false;
    private static final String SERVICE_NAME = "com.dell.treasure.service.ScannerService";

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        Logger.d("MonitorService onCreate");
        isRunning = true;
        MyApp myApp = MyApp.getInstance();
        DaoSession daoSession = myApp.getDaoSession();
        taskDao = daoSession.getTaskDao();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
//        task = intent.getParcelableExtra("Task");
        task = Task.getInstance();
        Logger.d("任务标志 "+task.getFlag());
        Logger.d("MonitorService onStartCommand");
        new Thread() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (isCheck) {
                    if (!isServiceWork(getApplicationContext(), SERVICE_NAME)) {
                        Logger.d("扫描服务已停止");
                        stopSelf();
                    } else {
                        task.setEndTime(dateToString(new Date()));
                        taskDao.update(task);
                        Logger.d(" "+task.getEndTime());
                        Logger.d("任务标志 "+task.getFlag());
                    }
                    try {
                        Thread.sleep(60 * 1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Logger.d("thread sleep failed");
                    }
                }
            }

        }.start();

        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    /**
     * 判断某个服务是否正在运行的方法
     *
     * @param mContext
     * @param serviceName 是包名+服务的类名（例如：com.baidu.trace.LBSTraceService）
     * @return true代表正在运行，false代表服务没有正在运行
     */
    public boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> myList = myAM.getRunningServices(80);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }
}
