package com.dell.treasure.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.baidu.trace.OnTrackListener;
import com.dell.treasure.dao.DaoSession;
import com.dell.treasure.dao.Task;
import com.dell.treasure.dao.TaskDao;
import com.dell.treasure.support.CurrentUser;
import com.dell.treasure.support.JpushReceiver;
import com.dell.treasure.support.MyApp;
import com.dell.treasure.support.NetUtil;
import com.orhanobut.logger.Logger;

import org.greenrobot.greendao.query.Query;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapFault;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.dell.treasure.support.ToolUtil.stringToDate;

/**
 * Created by yh on 2017/11/26.
 */

public class UploadService extends Service {
    public  static final String TAG = "UploadService";
    /**
     * Track监听器
     */
    protected static OnTrackListener trackListener = null;
    private MyApp myApp;
    private CurrentUser user;
    private String userId;
    private String lastId;
    private String taskId;
    private String beginTime;
    private String endTime;
    private double timeLong;
    private String way;
    private String isFound;
    private SharedPreferences sp;
    private TaskDao taskDao;
    private Query<Task> taskQuery;
    private List<Task> tasks;
    private Task task;
    private long ownTime;
    private long sysTime;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("result", TAG +" onCreate");
        myApp = (MyApp) getApplication();
        user = CurrentUser.getOnlyUser();

        initOnTrackListener();

        DaoSession daoSession = myApp.getDaoSession();
        taskDao = daoSession.getTaskDao();
        task = Task.getInstance();
        taskQuery = taskDao.queryBuilder().whereOr(TaskDao.Properties.Flag.eq(-1),TaskDao.Properties.Flag.eq(0)).build();
        tasks = taskQuery.list();
        sp = UploadService.this.getSharedPreferences(JpushReceiver.TASK, Context.MODE_PRIVATE);
        if(tasks.size() > 0) {
            task.setTask(tasks.get(0));
        }else{
            stopSelf();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(sp.getBoolean("isFound",false)){
            isFound = "1";
        }else {
            isFound = "0";
        }

        userId = user.getUserId();
        lastId = task.getLastId();
        taskId = task.getTaskId();
        beginTime = task.getBeginTime();
        endTime = task.getEndTime();
        if(Objects.equals(lastId, "0")){
            way = "1";
        }else{
            way = "2";
        }

        ownTime = stringToDate(endTime).getTime();

        long start = stringToDate(beginTime).getTime();
        queryDistance(start,ownTime);
        timeLong = (ownTime- start) / 1000.0 / 60.0;
//            new GetTimeTask().execute();
        Log.d("result",TAG + timeLong);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

//    private class GetTimeTask extends AsyncTask<String, Integer, String> {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        protected String doInBackground(String... args) {
//            String json = null;
//            try {
//                json = NetUtil.GetFinish(taskId);
//            } catch (SoapFault | NullPointerException soapFault) {
//                soapFault.printStackTrace();
//            }
//            if (json == null){
//
//            }else if (json.equals("0")){
//                Logger.d("任务没有完成");
//                ownTime = stringToDate(endTime).getTime();
//            } else{
//                Logger.d("endTime: "+endTime+" , sysTime: "+json);
//                ownTime = stringToDate(endTime).getTime();
//                sysTime = stringToDate(json).getTime();
//                if(ownTime > sysTime){
//                    ownTime = sysTime;
//                }
//            }
//            long start = stringToDate(beginTime).getTime();
//            queryDistance(start,ownTime);
//            timeLong = (ownTime- start) / 1000.0 / 60.0;
//            return null;
//        }
//
//        protected void onPostExecute(String file_url) {
//
//        }
//
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // 查询里程
    public void queryDistance(long startTime,long endTime) {

        // 是否返回纠偏后轨迹（0 : 否，1 : 是）
        int Processed = 1;
        //纠偏选项
        String processOption = "need_denoise=1,need_vacuate=1,need_mapmatch=0";
        // 里程补充
        String supplementMode = "walking";

        Logger.d(" "+userId+" "+(int)(startTime / 1000)+" "+(int)(endTime / 1000));
        myApp.getClient().queryDistance(130380, userId, Processed, processOption,
                supplementMode, (int)(startTime / 1000), (int)(endTime / 1000), trackListener);
    }

    private void initOnTrackListener() {

        trackListener = new OnTrackListener() {

            // 请求失败回调接口
            @Override
            public void onRequestFailedCallback(String arg0) {
                // TODO Auto-generated method stub
            }

            // 查询历史轨迹回调接口
            @Override
            public void onQueryHistoryTrackCallback(String arg0) {
                // TODO Auto-generated method stub
                super.onQueryHistoryTrackCallback(arg0);
            }

            @Override
            public void onQueryDistanceCallback(String arg0) {
                // TODO Auto-generated method stub
                Logger.d(" "+arg0);
                try {
                    JSONObject dataJson = new JSONObject(arg0);
                    if (null != dataJson && dataJson.has("status") && dataJson.getInt("status") == 0) {
                        double distance = dataJson.getDouble("distance") / 1000;
                        DecimalFormat df = new DecimalFormat("#.0");
                        user.setDistance(df.format(distance));  //千米
                        Logger.d(" "+user.getDistance()+" km");
                    }
                    // TODO Auto-generated catch block
                } catch (JSONException e) {
                    Logger.d("queryDistance回调消息 : " + arg0);
                }
                new UserTask().execute();
            }

            @Override
            public Map<String, String> onTrackAttrCallback() {
                // TODO Auto-generated method stub
                System.out.println("onTrackAttrCallback");
                return null;
            }

        };
    }

    private class UserTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            endTime = task.getEndTime();
            ownTime = stringToDate(endTime).getTime();
            long start = stringToDate(beginTime).getTime();
            timeLong = (ownTime- start) / 1000.0 / 60.0;
        }

        protected String doInBackground(String... args) {

            DecimalFormat df = new DecimalFormat("#.0");
            String json = null;
            Logger.d("5、上报参与信息 canyu "+userId+" "+taskId+" "+lastId+" "+way+" "+beginTime+" "+ endTime+" "+user.getDistance()+" "+df.format(timeLong));
            try {
                json = NetUtil.RecordParti(userId, taskId, df.format(timeLong), user.getDistance(), way, lastId,isFound);
                Logger.d("5、上报参与信息 canyu "+userId+" "+taskId+" "+lastId+" "+way+" "+beginTime+" "+ endTime+" "+user.getDistance()+" "+json);
            } catch (SoapFault | NullPointerException soapFault) {
                soapFault.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            stopSelf();
        }

    }
}
