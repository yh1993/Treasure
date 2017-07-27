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
import com.dell.treasure.service.LocationService;
import com.mob.MobApplication;

import org.greenrobot.greendao.database.Database;

import cn.jpush.android.api.JPushInterface;

/**
 * 全局信息
 */
public class MyApp extends MobApplication {

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


}
