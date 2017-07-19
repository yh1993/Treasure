package com.dell.treasure.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.dell.treasure.support.CurrentUser;
import com.dell.treasure.support.MyApp;
import com.dell.treasure.support.NetUtil;
import com.orhanobut.logger.Logger;

import org.ksoap2.SoapFault;

/**
 * Created by DELL on 2016/6/7.
 */
public class Location extends Service {
    private String username;
    private String taskId;
    private String bleId;
    private String location;
    private String lat;
    private String lon;

    private LocationService locationService;
    private BDLocationListener listener = new MyLocationListener();
    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d("Location: onCreate");
        MyApp myApp = MyApp.getInstance();
        CurrentUser user = CurrentUser.getOnlyUser();
        username = user.getUsername();
        bleId = user.getTarget_ble();
        taskId = user.getTaskId();

        locationService = myApp.locationService;
        locationService.setLocationOption(locationService.getDefaultLocationClientOption());
        //注册监听
        locationService.registerListener(listener);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            Logger.d("Location: onStartCommand");
            locationService.start();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationService.unregisterListener(listener); //注销掉监听
        locationService.stop();
        Logger.d( "Service: Location end");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if(bdLocation == null){
                return;
            }else{
                lat = String.valueOf(bdLocation.getLatitude());
                lon = String.valueOf(bdLocation.getLongitude());
                location = bdLocation.getLocationDescribe();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String json = null;
                        try {
                            json = NetUtil.HaveFound(username,taskId,bleId,"1234",lon,lat);
                            Logger.d("4、找回:"+username+bleId+location+lon+lat);
                        } catch (SoapFault | NullPointerException soapFault) {
                            soapFault.printStackTrace();
                        }
                        if (json == null){
                            Message msg = msgHandler.obtainMessage();
                            msg.what = 0x38;
                            msgHandler.sendMessage(msg);
                        }else {
                            switch (json) {
                                case "1": {
                                    Message msg = msgHandler.obtainMessage();
                                    msg.what = 0x34;
                                    msgHandler.sendMessage(msg);
                                    break;
                                }
                                case "2": {
                                    Message msg = msgHandler.obtainMessage();
                                    msg.what = 0x35;
                                    msgHandler.sendMessage(msg);
                                    break;
                                }
                                default: {
                                    Message msg = msgHandler.obtainMessage();
                                    msg.what = 0x36;
                                    msgHandler.sendMessage(msg);
                                    break;
                                }
                            }
                        }
                    }
                }).start();
            }
        }
    }

   private final Handler msgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x34:
                    Toast.makeText(getApplicationContext(), "恭喜，您已经发现宝物。", Toast.LENGTH_SHORT).show();
                    ScannerService.isFirst = 1;
                    break;
                case 0x35:
                    Toast.makeText(getApplicationContext(), "其他人已经发现宝物。", Toast.LENGTH_SHORT).show();
                    ScannerService.isFirst = 1;
                    break;
                case 0x36:
//                    Toast.makeText(getApplicationContext(), "其他情况！", Toast.LENGTH_SHORT).show();
                    ScannerService.isFirst = 1;
                    break;
                case 0x38:
                    Toast.makeText(getApplicationContext(), "无法连接服务器，请确认网络状况。", Toast.LENGTH_SHORT).show();
                    ScannerService.isFirst = 2;
                    break;
                default:
                    break;
            }
            stopSelf();
        }
    };
}
