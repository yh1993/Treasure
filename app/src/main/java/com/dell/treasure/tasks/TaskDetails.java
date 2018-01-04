package com.dell.treasure.tasks;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dell.treasure.R;
import com.dell.treasure.SignInActivity;
import com.dell.treasure.dao.Task;
import com.dell.treasure.service.AdvertiserService;
import com.dell.treasure.service.ScannerService;
import com.dell.treasure.share.ShareableActivity;
import com.dell.treasure.source.TasksRepository;
import com.dell.treasure.source.local.TasksLocalDataSource;
import com.dell.treasure.support.CurrentUser;
import com.dell.treasure.support.JpushReceiver;
import com.dell.treasure.support.NetUtil;
import com.dell.treasure.support.NotificationHelper;
import com.orhanobut.logger.Logger;

import org.ksoap2.SoapFault;

import java.util.Date;

import static com.dell.treasure.support.NotificationHelper.NOTICE_ID;
import static com.dell.treasure.support.ToolUtil.dateToString;

/**
 * Created by DELL on 2017/7/12.
 * 1.收到任务扩散的界面
 * 2.判断是否参与任务   参与: 转3   不参与: 不扫描，回到主界面
 * 3.开启扫描，判断是否开启广播，回到主界面
 * 4.主界面  显示任务
 */

public class TaskDetails extends ShareableActivity implements View.OnClickListener{
    private static final String TAG = "TaskDetails";
    private CurrentUser user;
    private Task currenTask;
    private Button game_yes;
    private Button game_no;
    private Intent scanIntent;
    private String taskId;
    private String fromuserId;
    private String level;
    private String num;
    private String strategy = null;
    private TextView strategy_text;
    private TextView level_text;
    private TextView num_text;
    private ProgressDialog pDialog = null;

    private TasksRepository mtasksRepository;
    private TasksLocalDataSource mtasksLocalDataSource;
    private SharedPreferences sp;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taskdetails);
        NotificationHelper.clearNotification(this, NOTICE_ID);

        user = CurrentUser.getOnlyUser();
        currenTask = user.getCurrentTask();
//        user.setTasKind("0");

        mtasksLocalDataSource = TasksLocalDataSource.getInstance();
        mtasksRepository = TasksRepository.getInstance(mtasksLocalDataSource);

        initView();
        taskId = currenTask.getTaskId();
        fromuserId = currenTask.getLastId();
        sp = getSharedPreferences(SignInActivity.USER_INFO, Context.MODE_PRIVATE);
        new JoinSubmit().execute();
    }

//    void taskTODO(){
//        if(currenTask.getFlag() >= -1){
//            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TaskDetails.this);
//            alertDialogBuilder.setTitle("提示");
//            alertDialogBuilder.setMessage("你好，你已经参与了该任务。。。");
//            alertDialogBuilder.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    startActivity(new Intent(TaskDetails.this,TasksActivity.class));
//                    dialogInterface.cancel();
//
//                }
//            });
//            alertDialogBuilder.create().show();
//        }else {
//            fromuserId = fromuserIdTmp;
//            currenTask.setLastId(fromuserId);
//            new JoinSubmit().execute();
//        }
//    }

    void initView(){
        strategy_text = (TextView)findViewById(R.id.strategy_text);
        level_text = (TextView) findViewById(R.id.current_level);
        num_text = (TextView)findViewById(R.id.current_num);
        game_yes = (Button) findViewById(R.id.game_yes);
        game_no = (Button) findViewById(R.id.game_no);

        game_yes.setOnClickListener(this);
        game_no.setOnClickListener(this);

        scanIntent = new Intent(TaskDetails.this,ScannerService.class);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.game_no:
                setCurrenTaskIdToUser();
                onBack();
                break;
            case R.id.game_yes:
                String needNum = currenTask.getNeedNum();
                if(Integer.parseInt(num) <= Integer.parseInt(needNum)){
                    currenTask.setFlag(-1);
                    joinTask();

                }else{
                    currenTask.setFlag(-3);
                    mtasksRepository.updateTask(currenTask);
                    Toast.makeText(TaskDetails.this,"非常抱歉，本次任务参与人数已达上线，请下次任务早点参与！",Toast.LENGTH_LONG).show();
                    onBack();
                }
                break;
        }
    }
    private void setCurrenTaskIdToUser(){
        user.setTaskId(""+taskId);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("taskId", taskId);
        editor.apply();
    }

    //加入任务
    private void joinTask() {
        currenTask.setBeginTime(dateToString(new Date()));
        setCurrenTaskIdToUser();
        new AlertDialog.Builder(TaskDetails.this)
                .setTitle("提示")
                .setMessage("是否愿意将任务消息扩散给更多的人？此操作会消耗一些电量，同时您也会得到更多的奖励。")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("接受", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currenTask.setFlag(0);
                        mtasksRepository.updateTask(currenTask);
                        startService(scanIntent);
                        startService(new Intent(TaskDetails.this, AdvertiserService.class));
                        startActivity(new Intent(TaskDetails.this,TasksActivity.class));
                        finish();
                    }
                })
                .setNegativeButton("放弃", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startService(scanIntent);
                        mtasksRepository.updateTask(currenTask);
                        startActivity(new Intent(TaskDetails.this,TasksActivity.class));
                        dialog.dismiss();
                        finish();
                    }
                })
                .show();
    }



    //拒绝参与任务
    public void onBack() {
        if(ScannerService.running){
            stopService(scanIntent);
        }
        startActivity(new Intent(TaskDetails.this,TasksActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        onBack();
//        new AlertDialog.Builder(TaskDetails.this)
//                .setTitle("提示")
//                .setMessage("确定要退出吗，您还没有选择是否参与当前任务？")
//                .setIcon(android.R.drawable.ic_dialog_info)
//                .setPositiveButton("参与", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        String needNum = currenTask.getNeedNum();
//                        if(Integer.parseInt(num) <= Integer.parseInt(needNum)){
//                            joinTask();
//                            currenTask.setFlag(-1);
//                        }else{
//                            currenTask.setFlag(-3);
//                            Toast.makeText(TaskDetails.this,"非常抱歉，本次任务参与人数已达上线，请下次任务早点参与！",Toast.LENGTH_LONG).show();
//                            onBack();
//                        }
//                        finish();
//                    }
//                })
//                .setNegativeButton("放弃", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        onBack();
//                    }
//                })
//                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class JoinSubmit extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(TaskDetails.this);
            pDialog.setMessage("任务获取中..");
            pDialog.setIndeterminate(false);//setIndeterminate(true)的意思就是不明确具体进度,进度条在最大值与最小值之间来回移动,形成一个动画效果
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... args) {

            try {
                strategy = NetUtil.getInfoDetail();
                Logger.d(TAG + strategy);
            } catch (SoapFault | NullPointerException soapFault) {
                soapFault.printStackTrace();
            }


            String json = null;
            try {
                json = NetUtil.levelAndNum(taskId,fromuserId);
            } catch (SoapFault | NullPointerException soapFault) {
                soapFault.printStackTrace();
            }
            if (json != null){
                String[] split = json.split(",");
                level = split[0];
                num = split[1];
                currenTask.setCurrentNum(num);
                currenTask.setCurrentLevel(level);

                SharedPreferences.Editor editor = getSharedPreferences(JpushReceiver.TASK, Context.MODE_PRIVATE).edit();
                if(strategy != null){
                    editor.putString("strategy",strategy);
                }
                editor.apply();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once donea
            if(!strategy.isEmpty()) {
                strategy_text.setText(strategy);
            }
            level_text.setText(level);
            num_text.setText(num);
            game_yes.setVisibility(View.VISIBLE);
            game_no.setVisibility(View.VISIBLE);
            game_yes.setEnabled(true);

            if(pDialog != null) {
                pDialog.dismiss();
                pDialog = null;
            }

        }

    }
}