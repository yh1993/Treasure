package com.dell.treasure.service;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.IBinder;
import android.view.WindowManager;

import com.dell.treasure.R;
import com.dell.treasure.support.Adaptive_Inquiry;
import com.dell.treasure.support.CurrentUser;
import com.dell.treasure.tasks.TasksActivity;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.dell.treasure.support.NotificationHelper.sendExpandedNotice;


/**
 * Created by DELL on 2016/8/23.
 * 注意 TargetBle 是否为null
 * 是否联网
 */
public class ScannerService extends Service {
    public static boolean running = false;
    public static int isFirst = 0;      //0 首次上报  1 不需要再次上报  2需要再次上报

    private DeviceLiveThread deviceLiveThread;  //循环扫描线程

    public CurrentUser user;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanCallback mScanCallback;

    private String bleId;

    @Override
    public void onCreate() {
        running = true;
        user = CurrentUser.getOnlyUser();
        bleId = user.getTarget_ble();
        initialize();
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        user.setNetConn(intent.getBooleanExtra("NetConn",false));
        return super.onStartCommand(intent, flags, startId);
    }

    private void resetting(){
        isFirst = 0;      //首次上报
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
        resetting();
        if(deviceLiveThread!=null){
            deviceLiveThread.stopThread();
        }
        Logger.d("scan stop");
    }

//    初始态
    private void initialize() {
        if (mBluetoothLeScanner == null) {
            BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager != null) {
                BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
                if (mBluetoothAdapter != null) {
                    mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                }
            }
        }
        startDeviceLiving();
    }

    private void startScanning(){
        if (mScanCallback == null) {
            // Kick off a new scan.
            mScanCallback = new SampleScanCallback();
        }
        mBluetoothLeScanner.startScan(buildScanFilters(), buildScanSettings(), mScanCallback);
    }

    private List<ScanFilter> buildScanFilters() {
        List<ScanFilter> scanFilters = new ArrayList<>();
        ScanFilter.Builder builder = new ScanFilter.Builder();
        scanFilters.add(builder.build());
        return scanFilters;
    }

    private ScanSettings buildScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        return builder.build();
    }

    private class SampleScanCallback extends ScanCallback {
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public  void onScanResult(int callbackType, final ScanResult result) {
            String address = result.getDevice().getAddress();
//            获取广播数据包
            if(!user.isNetConn()){
                if(!AdvertiserService.running){}
                try {
                    ScanRecord scanRecord = result.getScanRecord();
                    final byte[] bytes = scanRecord.getManufacturerSpecificData(1);
                    if (bytes != null) {
//                        只上报上线
                        String lastId = Arrays.toString(bytes);
                        Logger.d("从上线接收到消息 ：" + lastId);
                        user.setLastId(lastId);
                        user.setNetConn(true);

                        Intent passiveIntent = new Intent(getApplicationContext(), TasksActivity.class);
                        passiveIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        PendingIntent passivePi = PendingIntent.getActivity(getApplicationContext(), 0, passiveIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                        Intent activeIntent = new Intent(getApplicationContext(), AdvertiserService.class);
                        PendingIntent activePi = PendingIntent.getService(getApplicationContext(), 0, activeIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                        sendExpandedNotice(getApplicationContext(), "求助", "是否愿意帮助扩散求助，此操作会开启蓝牙广播，消耗少许电量。", R.mipmap.ic_launcher, activePi, passivePi);

                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            } else {
                if (bleId != null && !bleId.isEmpty() && address.equals(bleId)){ //&& AdvertiserService.running) {
                    if (isFirst == 0 || isFirst == 2) {
                        isFirst = 1;
                        final Intent positonIntent = new Intent(ScannerService.this, Location.class);
                        Logger.d("3、找到了 bleid " + bleId);

                        startService(positonIntent);
//                        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
//                        builder.setTitle("恭喜");
//                        builder.setMessage("宝物就在你的附近，是否愿意上报你的位置，进行确认？");
//                        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                positonIntent.putExtra("flag", 1);
//                                startService(positonIntent);
//                            }
//                        });
//                        builder.setNeutralButton("否", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                positonIntent.putExtra("flag", 0);
//                                startService(positonIntent);
//                            }
//                        });
//                        AlertDialog alertDialog = builder.create();
//                        alertDialog.setCanceledOnTouchOutside(false);//点击外面区域不会让dialog消失
//                        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//                        alertDialog.show();
                    }
                }
            }
            Adaptive_Inquiry.add(result);
            super.onScanResult(callbackType, result);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    }

    public class DeviceLiveThread extends Thread {
        private boolean isRunning=true;
        private void stopThread(){
            isRunning=false;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            while(isRunning){
                startScanning();
                try {
                    Thread.sleep(Adaptive_Inquiry.getInquiry_window() * 1280);
                    Logger.d("scan period:" + Adaptive_Inquiry.getInquiry_window() * 1.280);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                mBluetoothLeScanner.stopScan(mScanCallback);
                Adaptive_Inquiry.getR();
                Adaptive_Inquiry.getPeers();
                try {
                    Thread.sleep(Adaptive_Inquiry.getInquiry_interval() * 1280);
                    Logger.d("scan interval:" + Adaptive_Inquiry.getInquiry_interval() * 1.280);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private void startDeviceLiving(){
        if(deviceLiveThread==null){
            deviceLiveThread=new DeviceLiveThread();
            deviceLiveThread.start();
        }
    }
}












