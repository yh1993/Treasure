package com.dell.treasure;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.dell.treasure.share.ShareableActivity;
import com.dell.treasure.support.CurrentUser;
import com.dell.treasure.support.JpushReceiver;
import com.dell.treasure.tasks.TasksActivity;

import java.util.HashMap;

/**
 * Created by yh on 2017/12/4.
 */

public class SplashActivity2 extends ShareableActivity {
    public static final String TAG = "SplashActivity2";
    private SharedPreferences sp;

    private CurrentUser user;
    private String username = null;
    private String userid = null;
    private String currentState = "000";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);

        JpushReceiver.running = true;
        sp = getSharedPreferences(SignInActivity.USER_INFO, Context.MODE_PRIVATE);
        username = sp.getString(SignInActivity.USERNAME, null);
        userid = sp.getString(SignInActivity.USERID,null);
        currentState = sp.getString(SignInActivity.CURRENT_STATE,"000");

        user = CurrentUser.getOnlyUser();
    }
    @Override
    public void onReturnSceneData(HashMap<String, Object> res) {
        super.onReturnSceneData(res);
        HashMap<String, Object> params = (HashMap<String, Object>) res.get("params");
        if (null != params) {
            if (params.containsKey("userId")) {
                user.setFromUserId(""+params.get("userId"));
            }
            if (params.containsKey("taskId")) {
                user.setTaskIdTmp(""+params.get("taskId"));
            }
            Log.d("result",TAG+"onCreate: "+user.getFromUserId() +" "+user.getTaskIdTmp());
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

                    if(getIntent().getStringExtra("tasKind") != null){
                        user.setTasKind(getIntent().getStringExtra("tasKind"));
                    }
                    intent.setClass(SplashActivity2.this, TasksActivity.class);
                } else {
                    intent.setClass(SplashActivity2.this, SignInActivity.class);
                }
                SplashActivity2.this.startActivity(intent);
                SplashActivity2.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
