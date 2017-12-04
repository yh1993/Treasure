package com.dell.treasure.service;

import android.app.PendingIntent;
import android.app.Service;
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
import com.dell.treasure.tasks.TaskDetails;
import com.orhanobut.logger.Logger;

import org.greenrobot.greendao.query.Query;
import org.ksoap2.SoapFault;

import java.util.List;

import static com.dell.treasure.support.NotificationHelper.sendDefaultNotice;

/**
 * Created by DELL on 2017/7/20.
 */

public class NetService extends Service {
    TaskDao taskDao;
    Task task;
    private CurrentUser user;
    private boolean isNet;
    private MyApp myApp;

    @Override
    public void onCreate() {
        super.onCreate();
        user = CurrentUser.getOnlyUser();
        myApp=MyApp.getInstance();
        isNet = false;
        if(user.getUsername() != null) {
            new NetWorkTask().execute();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void start(){
//        user.setNetConn(isNet);
        insertTask();
//        if(!isNet) {
//            startScan();
//        }else {
            Intent taskIntent = new Intent(getApplicationContext(), TaskDetails.class);
            user.setTasKind("1");
            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, taskIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            sendDefaultNotice(getApplicationContext(), "任务", "收到任务，点击进入程序查看详情。", R.mipmap.ic_launcher, pi);
//        }
        stopSelf();
    }

    private void startScan(){  //服务器扩散
        Intent scanIntent = new Intent(NetService.this, ScannerService.class);
//        scanIntent.putExtra("NetConn",isNet);
        startService(scanIntent);
        Logger.d("2、服务器扩散 开始扫描");
    }

    private void insertTask(){
        taskDao = myApp.getDaoSession().getTaskDao();
        task = Task.getInstance();
        Task task1 = new Task(null,"0",user.getTaskId(),user.getTarget_ble(),"","",-3);

        taskDao.insert(task1);
        Query<Task> taskQuery = taskDao.queryBuilder().where(TaskDao.Properties.Flag.eq(-3)).build();
        List<Task> tasks = taskQuery.list();
        if (tasks.size() > 0) {
            task.setTask(tasks.get(0));
            Log.d("result","NetService "+ task.getId() + " " + task.getFlag());
        }

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
            Log.d("result","isNet "+json);
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
