package com.dell.treasure.service;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.orhanobut.logger.Logger;

import java.util.List;

/**
 * 判断广播服务是否运行，以广播时长作为用户参与任务时长 (废弃)
 *
 * 判断扫描服务是否运行，以参与任务后，开启的扫描时长作为用户参与任务时长
 */
public class MonitorService extends Service {
    private static final String SERVICE_NAME = "com.dell.treasure.service.ScannerService";
    public static boolean isCheck = false;
    public static boolean isRunning = false;
    private int times;
//    private static Task currentTask;
//    private static TasksRepository mtasksRepository;
//    private static TasksLocalDataSource mtasksLocalDataSource;


    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        times = 0;
        Logger.d("MonitorService onCreate");
        isRunning = true;
//        currentTask = CurrentUser.getOnlyUser().getCurrentTask();
//        mtasksLocalDataSource = TasksLocalDataSource.getInstance();
//        mtasksRepository = TasksRepository.getInstance(mtasksLocalDataSource);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Logger.d("MonitorService onStartCommand");
        new Thread() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (isCheck) {
                    if (!isServiceWork(getApplicationContext(), SERVICE_NAME)) {
                        Logger.d("扫描服务已停止");
                        isCheck = false;
                        stopSelf();
                    }
                    try {
                        if(times % 2 == 0){
                            Log.d("result", "if: times "+times);
                            Intent posIntent = new Intent(MonitorService.this, UploadService.class);
                            startService(posIntent);
                        }
                        times++;
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
        Logger.d("task over 4" );
        isRunning = false;
        stopService(new Intent(this,UploadService.class));

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
