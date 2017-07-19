package com.dell.treasure.service;

/**
 * 主要在广播中设置名字的形式，进行扩散 （改）
 * 设置广播包的内容，进行扩散
 * 广播时间问题 ？
 */

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.dell.treasure.dao.DaoSession;
import com.dell.treasure.dao.Task;
import com.dell.treasure.dao.TaskDao;
import com.dell.treasure.support.CurrentUser;
import com.dell.treasure.support.MyApp;
import com.orhanobut.logger.Logger;

import org.greenrobot.greendao.query.Query;

import java.util.Date;
import java.util.List;

import static com.dell.treasure.support.ToolUtil.dateToString;

/**
 * Manages BLE Advertising independent of the main app.
 * If the app goes off screen (or gets killed completely) advertising can continue because this
 * Service is maintaining the necessary Callback in memory.
 */
public class AdvertiserService extends Service {

    /**
     * A global variable to let AdvertiserFragment check if the Service is running without needing
     * to start or bind to it.
     * This is the best practice method as defined here:
     * https://groups.google.com/forum/#!topic/android-developers/jEvXMWgbgzE
     */
    private static final String TAG = AdvertiserService.class.getSimpleName();
    public static boolean running = false;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private AdvertiseCallback mAdvertiseCallback;
    private BluetoothAdapter mBluetoothAdapter;


    private byte [] userId;
    private String bleName;
    private MyApp myApp;

    private CurrentUser user;

    private TaskDao taskDao;
    private Task task;
    private Intent serviceIntent = null;
    private Intent serviceTrace = null;


    @Override
    public void onCreate() {
//        clearNotification(AdvertiserService.this, NotificationHelper.NOTICE_ID);
        myApp = MyApp.getInstance();
        user = CurrentUser.getOnlyUser();
        initVariable();
        initTaskDB();
        startAdvService();

        super.onCreate();
    }

    private void initVariable() {
        running = true;
//        if(myApp.getBeginTime().isEmpty()) {
//            isFirst = true;
//            myApp.setBeginTime(dateToString(new Date()));
//        }
        user.setBeginTime(dateToString(new Date()));
        userId = user.getUserId().getBytes();

    }

    private void initTaskDB() {
        DaoSession daoSession = myApp.getDaoSession();
        taskDao = daoSession.getTaskDao();

        Query<Task> taskQuery = taskDao.queryBuilder().where(TaskDao.Properties.Flag.eq(-1)).build();
        List<Task> tasks = taskQuery.list();
        if(tasks.size() > 0) {
            task = tasks.get(0);
            task.setLastId(user.getLastId());
            task.setBeginTime(user.getBeginTime());
            task.setFlag(0);
            taskDao.update(task);
        }else{
            Toast.makeText(this,"该任务丢失了。。。",Toast.LENGTH_SHORT).show();
            stopSelf();
        }

    }

    private void startAdvService() {
        serviceTrace = new Intent(this,TraceService.class);
        if(!TraceService.running) {
            startService(serviceTrace);
        }
        if(!MonitorService.isRunning) {
            // 开启监听service
            MonitorService.isCheck = true;
            startMonitorService();
        }
        initialize();
        startAdvertising();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void startMonitorService() {
        serviceIntent = new Intent(this, MonitorService.class);
        Bundle mBundle = new Bundle();
        mBundle.putParcelable("Task",task);
        Logger.d("任务标志 "+task.getFlag());
        serviceIntent.putExtras(mBundle);
        startService(serviceIntent);
    }
    @Override
    public void onDestroy() {
        /**
         * Note that onDestroy is not guaranteed to be called quickly or at all. Services exist at
         * the whim of the system, and onDestroy can be delayed or skipped entirely if memory need
         * is critical.
         */
        mBluetoothAdapter.setName(bleName);
        running = false;
        stopAdvertising();

        if(user.getEndTime().isEmpty()) {
            user.setEndTime(dateToString(new Date()));
        }
        task.setEndTime(user.getEndTime());
        task.setFlag(1);
        taskDao.update(task);

        // 停止监听service
        MonitorService.isCheck = false;
        if (null != serviceIntent) {
            stopService(serviceIntent);
        }
        if(serviceTrace != null){
            startService(serviceTrace);
        }

        Logger.d("endtime: "+task.getEndTime()+" 任务标志 "+task.getFlag());
        super.onDestroy();
    }

    /**
     * Required for extending service, but this will be a Started Service only, so no need for
     * binding.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Get references to system Bluetooth objects if we don't have them already.
     */
    private void initialize() {

        if (mBluetoothLeAdvertiser == null) {
            BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager != null) {
                mBluetoothAdapter = mBluetoothManager.getAdapter();
                if (mBluetoothAdapter != null) {
                    mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
                    bleName = mBluetoothAdapter.getName();
                    mBluetoothAdapter.setName("BLE");//形式
                }
            }
        }
    }


    /**
     * Starts BLE Advertising.
     */
    private void startAdvertising() {
        Logger.d("Service: Starting Advertising");

        if (mAdvertiseCallback == null) {
            AdvertiseSettings settings = buildAdvertiseSettings();
            AdvertiseData data = buildAdvertiseData();
            mAdvertiseCallback = new SampleAdvertiseCallback();

            if (mBluetoothLeAdvertiser != null) {
                mBluetoothLeAdvertiser.startAdvertising(settings, data,mAdvertiseCallback);
            }
        }
    }

    /**
     * Stops BLE Advertising.
     */
    private void stopAdvertising() {
        Logger.d( "Service: Stopping Advertising");
        if (mBluetoothLeAdvertiser != null) {
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
            mAdvertiseCallback = null;
        }
    }

    /**
     * Returns an AdvertiseData object which includes the Service UUID and Device Name.
     */
    private AdvertiseData buildAdvertiseData() {
        /**
         * Note: There is a strict limit of 31 Bytes on packets sent over BLE Advertisements.
         *  This includes everything put into AdvertiseData including UUIDs, device info, &
         *  arbitrary service or manufacturer data.
         *  Attempting to send packets over this limit will result in a failure with error code
         *  AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE. Catch this error in the
         *  onStartFailure() method of an AdvertiseCallback implementation.
         */

        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
//        dataBuilder.addServiceUuid(Constants.Service_UUID);

//        1、通过蓝牙名称来传递上家和丢失Ble数据
//        dataBuilder.setIncludeDeviceName(true);

//        2、通过广播数据包扩散
        dataBuilder.setIncludeDeviceName(false);
        dataBuilder.setIncludeTxPowerLevel(false);
        if(userId!=null) {
            dataBuilder.addManufacturerData(1, userId);
        }
        return dataBuilder.build();
    }


    /**
     * Returns an AdvertiseSettings object set to use low power (to help preserve battery life)
     * and disable the built-in timeout since this code uses its own timeout runnable.
     */
    private AdvertiseSettings buildAdvertiseSettings() {
        //广播模式：平衡   广播功率： 高
        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        settingsBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        settingsBuilder.setTimeout(0);
        return settingsBuilder.build();
    }

    /**
     * Custom callback after Advertising succeeds or fails to start. Broadcasts the error code
     * in an Intent to be picked up by AdvertiserFragment and stops this Service.
     */
    private class SampleAdvertiseCallback extends AdvertiseCallback {

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Logger.d("Advertising failed");
            stopSelf();
        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
        }
    }
}