package com.dell.treasure.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.dell.treasure.support.CurrentUser;
import com.dell.treasure.support.NetUtil;

import net.sf.json.JSONArray;

import org.ksoap2.SoapFault;

import java.util.List;

/**
 * Created by DELL on 2016/6/16.
 */
public class UserInfo extends Service {

    private final Handler msgHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
                case 0x37:
                    Toast.makeText(UserInfo.this,"从服务器同步奖励失败",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            stopSelf();
        }
    };
    private Intent intent = new Intent("com.dell.treasure.RECEIVER_UserInfo");
    private LocalBroadcastManager localBroadcastManager;
    private String username;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("result","UserInfo start");
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        CurrentUser user = CurrentUser.getOnlyUser();
        username = user.getUsername();
        new UserTask().execute();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("result","UserInfo end");
    }

    private class UserTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... args) {
            String json = null;
            try {
                json = NetUtil.UserInfo(username);
            } catch (SoapFault | NullPointerException soapFault) {
                soapFault.printStackTrace();
            }
            if (json == null){
                Message msg = msgHandler.obtainMessage();
                msg.what = 0x37;
                msgHandler.sendMessage(msg);
            }else {
                JSONArray user_json = JSONArray.fromObject(json);
                List<String> str = (List<String>) JSONArray.toCollection(user_json, json.getClass());

                SharedPreferences.Editor editor = getSharedPreferences(username, Context.MODE_PRIVATE).edit();
                editor.putString("points", str.get(1));
                editor.putString("money",str.get(2));
                editor.apply();
                intent.putExtra("points",str.get(1));
                intent.putExtra("money",str.get(2));
                localBroadcastManager.sendBroadcast(intent);
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            stopSelf();
        }

    }
}
