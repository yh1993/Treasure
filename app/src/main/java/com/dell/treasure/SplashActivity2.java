package com.dell.treasure;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.dell.treasure.dao.Task;
import com.dell.treasure.dao.TaskDao;
import com.dell.treasure.publisher.DeviceScanActivity;
import com.dell.treasure.support.CurrentUser;
import com.dell.treasure.support.MyApp;
import com.dell.treasure.tasks.TasksActivity;
import com.orhanobut.logger.Logger;

import org.greenrobot.greendao.query.Query;

import java.util.List;

/**
 * Created by yh on 2017/12/4.
 */

public class SplashActivity2 extends Activity {
    public static final String TAG = "SplashActivity2";
    private SharedPreferences sp;

    private CurrentUser user;
    private String username = null;
    private String userid = null;
    private String currentState = "000";
    private String fromuserId;
    private String taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);
        Logger.init("result");
        sp = getSharedPreferences(SignInActivity.USER_INFO, Context.MODE_PRIVATE);
        username = sp.getString(SignInActivity.USERNAME, null);
        userid = sp.getString(SignInActivity.USERID,null);
        currentState = sp.getString(SignInActivity.CURRENT_STATE,"000");
        user = CurrentUser.getOnlyUser();
        Intent i = getIntent();
        Uri uri = i.getData();

        if (uri != null) {
            Log.d("result", TAG + uri);
            fromuserId = uri.getQueryParameter("userId");
            taskId = uri.getQueryParameter("taskId");
            user.setFromUserId(fromuserId);
            user.setTaskIdTmp(taskId);
        }

        int SPLASH_DISPLAY_LENGTH = 1000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                if (sp.getBoolean(SignInActivity.AUTO_SIGNIN, false) && username != null) {

                    user.setUsername(username);
                    user.setUserId(userid);
                    user.setCurrentState(currentState);
                    Log.d("result",TAG +" user id name state: "+userid+" "+username+" "+currentState);

                    if(getIntent().getStringExtra("tasKind") != null){
                        user.setTasKind(getIntent().getStringExtra("tasKind"));
                    }

                    if(username.equals("yh")){
                        intent.setClass(SplashActivity2.this, DeviceScanActivity.class);
                    }else{
                        intent.setClass(SplashActivity2.this, TasksActivity.class);
                    }
                } else {
                    intent.setClass(SplashActivity2.this, SignInActivity.class);
                }
                SplashActivity2.this.startActivity(intent);
                SplashActivity2.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
