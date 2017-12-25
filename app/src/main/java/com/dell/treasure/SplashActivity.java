package com.dell.treasure;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.dell.treasure.support.CurrentUser;
import com.dell.treasure.tasks.TasksActivity;
import com.orhanobut.logger.Logger;

/**
 * Created by hp on 2016/3/17 0017.
 * 开始界面，判断进入登录界面，or 扫描界面
 */
public class SplashActivity extends Activity{
    public static final String TAG = "SplashActivity";
    private SharedPreferences sp;

    private CurrentUser user;
    private String username = null;
    private String userid = null;
//    private String currentState = "000";

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
//        currentState = sp.getString(SignInActivity.CURRENT_STATE,"000");

        int SPLASH_DISPLAY_LENGTH = 1000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                if (sp.getBoolean(SignInActivity.AUTO_SIGNIN, false) && username != null) {
                    user = CurrentUser.getOnlyUser();
                    user.setUsername(username);
                    user.setUserId(userid);
//                    user.setCurrentState(currentState);

//                    if(getIntent().getStringExtra("tasKind") != null){
//                        user.setTasKind(getIntent().getStringExtra("tasKind"));
//                    }
                    intent.setClass(SplashActivity.this, TasksActivity.class);

                } else {
                    intent.setClass(SplashActivity.this, SignInActivity.class);
                }
                SplashActivity.this.startActivity(intent);
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}