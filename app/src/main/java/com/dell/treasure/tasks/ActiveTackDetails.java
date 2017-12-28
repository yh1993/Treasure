package com.dell.treasure.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dell.treasure.R;
import com.dell.treasure.dao.Task;
import com.dell.treasure.share.BaseActivity;
import com.dell.treasure.support.CommonUtils;
import com.dell.treasure.support.CurrentUser;
import com.dell.treasure.support.JpushReceiver;
import com.mob.moblink.ActionListener;
import com.mob.moblink.MobLink;
import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.Map;

import cn.sharesdk.onekeyshare.OnekeyShare;

/**
 * Created by yh on 2017/11/22.
 */

public class ActiveTackDetails extends BaseActivity{
    private TextView currentNum;
    private TextView currentLevel;
    private TextView startTime;
    private TextView taskLong;
    private TextView strategy_text;
    private Button share;

    private SharedPreferences sp;
    private CurrentUser user;
    private Task currenTask;

    private String mCurrentNum;
    private String mCurrentLevel;
    private String mStartTime;
    private String mStrategy;
    private String mobID;
    private String userId;
    private String taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.task_detail);
        currentNum = (TextView) findViewById(R.id.current_num);
        currentLevel = (TextView) findViewById(R.id.current_level);
        startTime = (TextView) findViewById(R.id.time_start);
        taskLong = (TextView) findViewById(R.id.length);
        strategy_text = (TextView)findViewById(R.id.strategy_text);

//        task = (Task) getIntent().getParcelableExtra(TasksFragment.PAR_KEY);
        share = (Button) findViewById(R.id.btn_share);
        share.setOnClickListener(this);


        user = CurrentUser.getOnlyUser();
        currenTask = user.getCurrentTask();
        userId = user.getUserId();
        taskId = currenTask.getTaskId();
        mCurrentNum = currenTask.getCurrentNum();
        mCurrentLevel = currenTask.getCurrentLevel();
        mStartTime = currenTask.getStartTime();

        Logger.d(currenTask.toString());
        sp = getSharedPreferences(JpushReceiver.TASK, Context.MODE_PRIVATE);

        mStrategy = sp.getString("strategy",null);

    }

    @Override
    protected void onResume() {
        super.onResume();
        currentNum.setText(mCurrentNum);
        currentLevel.setText(mCurrentLevel);
        startTime.setText(mStartTime);
        taskLong.setText("2小时");
        if(mStrategy != null){
            strategy_text.setText(mStrategy);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_share:
//                showShare();
//                setDefault();
                share();
                break;
            default:
                super.onClick(v);
                break;
        }
    }


    private void getMobID(){
        HashMap<String, Object> params = new HashMap<String, Object>();
        String source = "MobLinkDemo";
        String key1 = "userId";
        String key2 = "taskId";
        String value1 = user.getUserId();
        String value2 = currenTask.getTaskId();
        params.put(key1, value1);
        params.put(key2, value2);


        MobLink.getMobID(params, CommonUtils.MAIN_PATH_ARR, source, new ActionListener() {
            public void onResult(HashMap<String, Object> params) {
                if (params != null && params.containsKey("mobID")) {
                    mobID = String.valueOf(params.get("mobID"));
                    Logger.d("mobId: "+ mobID);
                }
            }

            public void onError(Throwable t) {
                if (t != null) {
                    Toast.makeText(ActiveTackDetails.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void share() {
//        String shareUrl = "mlink://treasure.com"+ CommonUtils.MAIN_PATH_ARR;
        getMobID();
        if(TextUtils.isEmpty(mobID)) {
            CommonUtils.getMobIdDialog(this).show();
            return;
//            getMobID();
        }
        String shareUrl = CommonUtils.SHARE_URL;
        if (!TextUtils.isEmpty(mobID)) {
            shareUrl += "?mobid=" + mobID;
        }


        String title = getString(R.string.invite_share_titel);
        String text = getString(R.string.share_text);
        String imgPath = CommonUtils.copyImgToSD(this, R.mipmap.ic_launcher , "invite");
        CommonUtils.showShare(this, title, text, shareUrl, imgPath);

    }

}
