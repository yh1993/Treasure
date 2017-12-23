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
import com.dell.treasure.dao.Task;
import com.dell.treasure.dao.TaskDao;
import com.dell.treasure.service.AdvertiserService;
import com.dell.treasure.service.ScannerService;
import com.dell.treasure.share.ShareableActivity;
import com.dell.treasure.support.CurrentUser;
import com.dell.treasure.support.JpushReceiver;
import com.dell.treasure.support.MyApp;
import com.dell.treasure.support.NetUtil;
import com.dell.treasure.support.NotificationHelper;
import com.orhanobut.logger.Logger;

import org.greenrobot.greendao.query.Query;
import org.ksoap2.SoapFault;

import java.util.List;

import static com.dell.treasure.support.NotificationHelper.NOTICE_ID;

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
    private Button game_yes;
    private Button game_no;
    private Intent scanIntent;
    private Task task;
    private TaskDao taskDao;
    private String taskId;
    private String userId;
    private String fromuserId;
    private String fromuserIdTmp;
    private String level;
    private String num;
    private String strategy = null;
    private TextView strategy_text;
    private TextView level_text;
    private TextView num_text;
    private ProgressDialog pDialog = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taskdetails);
        NotificationHelper.clearNotification(this, NOTICE_ID);

        user = CurrentUser.getOnlyUser();
        user.setTasKind("0");

        initView();
        initData();

        queryData();
        taskId = task.getTaskId();
        userId = user.getUserId();
        fromuserId = task.getLastId();
        new JoinSubmit().execute();
    }

    void initData(){
        taskDao = MyApp.getInstance().getDaoSession().getTaskDao();
        task = Task.getInstance();
    }

    void queryData(){
        Query<Task> taskQuery = taskDao.queryBuilder().whereOr(TaskDao.Properties.Flag.eq(-3),TaskDao.Properties.Flag.eq(-2)).build();
        List<Task> tasks = taskQuery.list();
        if (tasks.size() > 0) {
            task.setTask(tasks.get(0));
        }
    }

    void taskTODO(){
        if(task.getFlag() >= -1){
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TaskDetails.this);
            alertDialogBuilder.setTitle("提示");
            alertDialogBuilder.setMessage("你好，你已经参与了该任务。。。");
            alertDialogBuilder.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(TaskDetails.this,TasksActivity.class));
                    dialogInterface.cancel();

                }
            });
            alertDialogBuilder.create().show();
        }else {
            fromuserId = fromuserIdTmp;
            user.setLastId(fromuserId);
            task.setLastId(fromuserId);
            taskDao.update(task);
            new JoinSubmit().execute();
        }
    }

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
                user.setJoin(false);
                onBack();
                break;
            case R.id.game_yes:
                SharedPreferences sp = getSharedPreferences(JpushReceiver.TASK, Context.MODE_PRIVATE);
                String needNum = sp.getString("needNum", null);
                Log.d("result","num needNum"+Integer.parseInt(num)+" "+Integer.parseInt(needNum));
                if(Integer.parseInt(num) < Integer.parseInt(needNum)){
                    joinTask();
                    user.setJoin(true);
                }else{
                    Toast.makeText(TaskDetails.this,"非常抱歉，本次任务参与人数已达上线，请下次任务早点参与！",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    //加入任务
    private void joinTask() {
        task.setFlag(-1);
        Logger.d(TAG + " "+task.getId());
        new AlertDialog.Builder(TaskDetails.this)
                .setTitle("提示")
                .setMessage("是否愿意将任务消息扩散给更多的人？此操作会消耗一些电量，同时您也会得到更多的奖励。")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("接受", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        task.setFlag(0);
                        taskDao.update(task);
                        startService(scanIntent);
                        startService(new Intent(TaskDetails.this, AdvertiserService.class));
                        startActivity(new Intent(TaskDetails.this,TasksActivity.class));
                        finish();
                    }
                })
                .setNegativeButton("放弃", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        taskDao.update(task);
                        startService(scanIntent);
                        startActivity(new Intent(TaskDetails.this,TasksActivity.class));
                        dialog.dismiss();
                        finish();
                    }
                })
                .show();
    }

    //拒绝参与任务
    public void onBack() {
        task.setFlag(-2);
        taskDao.update(task);
        startActivity(new Intent(TaskDetails.this,TasksActivity.class));
        if(ScannerService.running){
            stopService(scanIntent);
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        onBack();
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
                user.setCurrentLevel(level);
                user.setCurrentNum(num);
                SharedPreferences.Editor editor = getSharedPreferences(JpushReceiver.TASK, Context.MODE_PRIVATE).edit();
                editor.putString("level", level);
                editor.putString("num", num);
                if(strategy != null){
                    editor.putString("strategy",strategy);
                }
                editor.apply();
                Logger.d(TAG +"level num"+ level+" "+num);
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