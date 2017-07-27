package com.dell.treasure.service;

/**
 * 任务的开始和结束不再以广播的开始与结束为标准
 * 主要就是开启BLE 广播，将自己的userId，加入广播进行扩散
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
import android.os.IBinder;

import com.dell.treasure.support.CurrentUser;
import com.orhanobut.logger.Logger;

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
    private CurrentUser user;


    @Override
    public void onCreate() {
        user = CurrentUser.getOnlyUser();
        initVariable();
        initialize();
        startAdvertising();

        super.onCreate();
    }

    private void initVariable() {
        running = true;
        userId = user.getUserId().getBytes();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
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
            mAdvertiseCallback = new SampleAdvertiseCallback();
        }

        if (mBluetoothLeAdvertiser != null) {
            AdvertiseSettings settings = buildAdvertiseSettings();
            AdvertiseData data = buildAdvertiseData();
            mBluetoothLeAdvertiser.startAdvertising(settings, data,mAdvertiseCallback);
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