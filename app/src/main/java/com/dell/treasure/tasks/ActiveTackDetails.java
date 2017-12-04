package com.dell.treasure.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
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

import java.util.HashMap;

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

    private String mCurrentNum;
    private String mCurrentLevel;
    private String mStartTime;
    private String mStrategy;
    private Task task;
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

        task = (Task) getIntent().getParcelableExtra(TasksFragment.PAR_KEY);
        share = (Button) findViewById(R.id.btn_share);
        share.setOnClickListener(this);
        taskId = task.getTaskId();

        user = CurrentUser.getOnlyUser();
        userId = user.getUserId();
        mCurrentNum = user.getCurrentNum();
        mCurrentLevel = user.getCurrentLevel();
        mStartTime = user.getStartTime();

        sp = getSharedPreferences(JpushReceiver.TASK, Context.MODE_PRIVATE);

        if(mCurrentNum.isEmpty()||mCurrentNum == ""){
            mCurrentNum = sp.getString("num","0");
            user.setCurrentNum(mCurrentNum);
        }
        if(mCurrentLevel.isEmpty()||mCurrentLevel == ""){
            mCurrentLevel = sp.getString("level","0");
            user.setCurrentLevel(mCurrentLevel);
        }
        if(mStartTime.isEmpty()||mStartTime == ""){
            mStartTime = sp.getString("startTime","0");
            user.setCurrentLevel(mStartTime);
        }

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

    private void setDefault(){
        HashMap<String, Object> params = new HashMap<String, Object>();
        String source = "";
        String key1 = "userId";
        String key2 = "taskId";
        String value1 = user.getUserId();
        String value2 = task.getTaskId();
        params.put(key1, value1);
        params.put(key2, value2);


        MobLink.getMobID(params, CommonUtils.MAIN_PATH_ARR, source, new ActionListener() {
            public void onResult(HashMap<String, Object> params) {
                if (params != null && params.containsKey("mobID")) {
                    mobID = String.valueOf(params.get("mobID"));
                }
            }

            public void onError(Throwable t) {
                if (t != null) {
                    Toast.makeText(ActiveTackDetails.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        String shareUrl = CommonUtils.SHARE_URL + CommonUtils.MAIN_PATH_ARR;
        if (!TextUtils.isEmpty(mobID)) {
            shareUrl += "?mobid=" + mobID;
        }
        String title = getString(R.string.show_share_titel);
        String text = getString(R.string.share_text);
        String imgPath = CommonUtils.copyImgToSD(this, R.drawable.demo_share_moblink , "moblink");
        CommonUtils.showShare(this, title, text, shareUrl, imgPath);
    }

    private void share() {
        String shareUrl = "mlink://treasure.com"+ CommonUtils.MAIN_PATH_ARR;
        if (!TextUtils.isEmpty(userId)) {
            shareUrl += "?userId=" + userId;
        }
        if (!TextUtils.isEmpty(taskId)) {
            shareUrl += "&taskId=" + taskId;
        }
        String title = getString(R.string.invite_share_titel);
        String text = getString(R.string.share_text);
        String imgPath = CommonUtils.copyImgToSD(this, R.mipmap.ic_launcher , "invite");
        CommonUtils.showShare(this, title, text, shareUrl, imgPath);
    }
    private void showShare() {
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();

        // 分享时Notification的图标和文字  2.5.9以后的版本不     调用此方法
        //oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle(getString(R.string.share));
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        oks.setTitleUrl("http://sharesdk.cn");
        // text是分享文本，所有平台都需要这个字段
        oks.setText("邀请您参加寻宝任务，一起分享奖励");
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        oks.setImagePath(CommonUtils.copyImgToSD(ActiveTackDetails.this, R.drawable.demo_share_invite , "invite"));//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
//        oks.setUrl("http://sharesdk.cn");
        oks.setUrl("myApp://myweb.com/openApp");
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment("我是测试评论文本");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl("http://sharesdk.cn");

        // 启动分享GUI
        oks.show(ActiveTackDetails.this);
    }

}
