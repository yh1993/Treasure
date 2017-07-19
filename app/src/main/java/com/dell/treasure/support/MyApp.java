package com.dell.treasure.support;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.LocationMode;
import com.baidu.trace.Trace;
import com.dell.treasure.dao.DaoMaster;
import com.dell.treasure.dao.DaoSession;
import com.dell.treasure.dao.Task;
import com.dell.treasure.dao.TaskDao;
import com.dell.treasure.service.LocationService;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.Query;

import java.util.List;

import cn.jpush.android.api.JPushInterface;

/**
 * 全局信息
 */
public class MyApp extends Application {
//    private String username;
//    private String userId;
//    private String lastId;       //上线的id
//    private String taskId ="";       //任务Id
//    private String target_ble ="";   //目标蓝牙Mac
//    private String startTime = "";    //任务开始时间
//    private String beginTime = "";    //接受任务并开始的时间
//    private String endTime = "";      //任务结束
//    private String distance ;
//    public boolean isNetConn = true;

    public static final String TAG = "Myapp";
    public LocationService locationService;
    public BluetoothAdapter mBluetoothAdapter;
    private DaoSession daoSession = null;
    private Context mContext = null;
    private static MyApp instances;
    private int appCount = 0;
    private CurrentUser user;
    //轨迹服务客户端
    private LBSTraceClient client = null;

    // 鹰眼服务ID，开发者创建的鹰眼服务对应的服务ID
    private int serviceId = 130380;

    //entity标识
    private String entityName;

    //轨迹服务类型（0 : 不建立socket长连接， 1 : 建立socket长连接但不上传位置数据，2 : 建立socket长连接并上传位置数据）
    private int traceType = 2;

//    public String getUsername() { return username; }
//
//    public String getUserId() {
//        return userId;
//    }
//
//    public String getLastId() {
//        return lastId;
//    }
//
//    public String getTaskId() {
//        return taskId;
//    }
//
//    public String getTarget_ble() {
//        return target_ble;
//    }
//
//    public String getBeginTime() {
//        return beginTime;
//    }
//
//    public String getEndTime() {
//        return endTime;
//    }
//
//    public String getDistance() {
//        return distance;
//    }
//
//    public String getStartTime() {
//        return startTime;
//    }
//
//    public boolean isNetConn() {
//        return isNetConn;
//    }
//
//    public void setUsername(String name) { this.username = name; }
//
//    public void setUserId(String userId) {
//        this.userId = userId;
//    }
//
//    public void setTarget_ble(String target_ble) {
//        this.target_ble = target_ble;
//    }
//
//    public void setLastId(String lastId) { this.lastId = lastId; }
//
//    public void setTaskId(String taskId) {
//        this.taskId = taskId;
//    }
//
//    public void setBeginTime(String beginTime) {
//        this.beginTime = beginTime;
//    }
//
//    public void setNetConn(boolean isNetConn){
//        this.isNetConn = isNetConn;
//    }
//
//    public void setEndTime(String endTime) {
//        this.endTime = endTime;
//    }
//
//    public void setDistance(String distance) {
//        this.distance = distance;
//    }
//
//    public void setStartTime(String startTime) {
//        this.startTime = startTime;
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("result",TAG);
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                appCount++;
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                appCount--;
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

        mContext = getApplicationContext();
        locationService = new LocationService(mContext);
        SDKInitializer.initialize(mContext);

        // 初始化轨迹服务客户端
        client = new LBSTraceClient(mContext);
        // 设置定位模式
        client.setLocationMode(LocationMode.High_Accuracy);

        instances = this;

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        user = CurrentUser.getOnlyUser();
        JPushInterface.init(this);
    }

    public static MyApp getInstance(){
        return instances;
    }

    /**
     * 以username作为数据库名称
     */
    private void setDatabase(String username){
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this,username);
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
    }
    public DaoSession getDaoSession(){
        if(daoSession == null){
            setDatabase(user.getUsername());
        }
        return daoSession;
    }

    public Trace getTrace(String name) {
        // 初始化轨迹服务
        entityName = name;
        return new Trace(mContext, serviceId, entityName, traceType);
    }

    public LBSTraceClient getClient() {
        return client;
    }

    public String getEntityName() {
        return entityName;
    }

    public int getAppCount(){
        return appCount;
    }

    public void initData(){
        Query<Task> taskQuery = getDaoSession().getTaskDao().queryBuilder().where(TaskDao.Properties.Flag.eq(0)).build();
        List<Task> tasks = taskQuery.list();
        if(tasks.size() > 0) {
            Task task = tasks.get(0);
            task.setLastId(user.getLastId());
            task.setBeginTime(user.getBeginTime());
            task.setFlag(0);
        }
    }

}
