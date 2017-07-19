package com.dell.treasure.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.dell.treasure.R;
import com.dell.treasure.dao.Task;
import com.dell.treasure.dao.TaskDao;
import com.dell.treasure.support.CurrentUser;
import com.dell.treasure.support.MyApp;
import com.dell.treasure.support.NetUtil;
import com.dell.treasure.support.ToolUtil;
import com.dell.treasure.tasks.TaskDetails;
import com.dell.treasure.tasks.TasksActivity;
import com.orhanobut.logger.Logger;

import org.ksoap2.SoapFault;

import java.util.Date;

import static com.dell.treasure.support.NotificationHelper.sendDefaultNotice;
import static com.dell.treasure.support.ToolUtil.stringToDate;

/**
 * 判断任务是否过期
 * 先判断程序是否启动
 * 确定是否联网
 * 确定是否参与任务
 * 参与：开启广播、扫描、轨迹查询
 */

public class PrepareService extends Service {
    TaskDao taskDao;
    Task task;
    private MyApp myApp;
    private CurrentUser user;
    private boolean isNet = false;       //判断是否联网,默认不联网
    //app 当前状态  0 未打开  (1 打开在登录界面  2 在主界面) 统一为打开    参与人数确定后，不再有新参者
    private boolean appState;
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        myApp=MyApp.getInstance();
        user = CurrentUser.getOnlyUser();
        context = getApplicationContext();

        isOverdue();
        isAppSurvive();
        isNetConn();
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
        }
    }
    //app 是否存活
    private void isAppSurvive() {
        appState = ToolUtil.isAppAlive(context,"com.dell.treasure");
        if(appState){

        }
    }

    private void isNetConn(){
        new NetWorkTask().execute();
    }

    private void insertTask(){
        taskDao = myApp.getDaoSession().getTaskDao();
        task = new Task(null,"0",user.getTaskId(),user.getTarget_ble(),"","",-1);
        taskDao.insert(task);
    }

//    private void showDialogs() {
//        Intent passiveIntent = new Intent(getApplicationContext(), TasksActivity.class);
//        PendingIntent passivePi = PendingIntent.getActivity(getApplicationContext(),0,passiveIntent,PendingIntent.FLAG_CANCEL_CURRENT);
//
//        Intent activeIntent = new Intent(getApplicationContext(), AdvertiserService.class);
//        PendingIntent activePi = PendingIntent.getService(getApplicationContext(),0,activeIntent,PendingIntent.FLAG_CANCEL_CURRENT);
//        sendExpandedNotice(getApplicationContext(),"求助","是否愿意帮助扩散求助，此操作会开启蓝牙广播，消耗少许电量。", R.mipmap.ic_launcher,activePi,passivePi);
//    }

    private void showDialogs() {
        PendingIntent pi = null;
        if(appState){
//            Intent mainIntent = new Intent(context,TasksActivity.class);

            Intent taskIntent = new Intent(context,TaskDetails.class);
            user.setTasKind("1");

//            Intent[] intents = {mainIntent,taskIntent};
//            pi = PendingIntent.getActivities(getApplicationContext(),0,intents,PendingIntent.FLAG_CANCEL_CURRENT);
            pi = PendingIntent.getActivity(getApplicationContext(),0,taskIntent,PendingIntent.FLAG_CANCEL_CURRENT);

        }else{
            Intent launchIntent = context.getPackageManager()
                    .getLaunchIntentForPackage("com.dell.treasure");
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            launchIntent.putExtra("tasKind","1");
            pi = PendingIntent.getActivity(getApplicationContext(),0,launchIntent,PendingIntent.FLAG_CANCEL_CURRENT);
        }
        sendDefaultNotice(context,"任务","收到任务，点击进入程序查看详情。",R.mipmap.ic_launcher,pi);

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

    private void start(){
        startScan();
        user.setNetConn(isNet);
        insertTask();
        if(!isNet) {
//            int flag = MyApp.getInstance().getAppCount();
//            if(flag > 0) {
//                showDialogs();
//            }else{
//                //通知
//            }
            showDialogs();
        }
    }

    private void startScan(){  //服务器扩散
        Intent scanIntent = new Intent(PrepareService.this, ScannerService.class);
        scanIntent.putExtra("NetConn",isNet);
        startService(scanIntent);
        Logger.d("2、服务器扩散 开始扫描");
    }

    private class NetWorkTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... args) {
            String json = null;
            try {
                json = NetUtil.isReciveMeg(user.getUsername());
            } catch (SoapFault | NullPointerException soapFault) {
                soapFault.printStackTrace();
            }
            Log.d("result",json);
            if (json == null){
                isNet = false;
            }else {
                if(json.equals("1"))
                    isNet = true;
                else
                    isNet = false;
            }
            start();
            return null;
        }

        protected void onPostExecute(String file_url) {
        }
    }
}