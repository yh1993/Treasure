package com.dell.treasure.tasks;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.dell.treasure.R;
import com.dell.treasure.dao.Task;
import com.dell.treasure.dao.TaskDao;
import com.dell.treasure.service.AdvertiserService;
import com.dell.treasure.service.ScannerService;
import com.dell.treasure.support.CurrentUser;
import com.dell.treasure.support.MyApp;

import org.greenrobot.greendao.query.Query;

import java.util.List;

/**
 * Created by DELL on 2017/7/12.
 * 1.收到任务扩散的界面
 * 2.判断是否参与任务   参与: 转3   不参与: 不扫描，回到主界面
 * 3.开启扫描，判断是否开启广播，回到主界面
 * 4.主界面  显示任务
 */

public class TaskDetails extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "TaskDetails";
    private CurrentUser user;
    private Button game_yes;
    private Button game_no;
    private Intent scanIntent;
    private Task task;
    private TaskDao taskDao;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("result",TAG);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taskdetails);
        user = CurrentUser.getOnlyUser();
        user.setTasKind("0");
        initData();
        initView();
    }

    void initData(){
        taskDao = MyApp.getInstance().getDaoSession().getTaskDao();
        task = Task.getInstance();
        Query<Task> taskQuery = taskDao.queryBuilder().whereOr(TaskDao.Properties.Flag.eq(-3),TaskDao.Properties.Flag.eq(-2)).build();
        List<Task> tasks = taskQuery.list();
        if (tasks.size() > 0) {
            task.setTask(tasks.get(0));
        }
    }

    void initView(){
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
                onBack();
                break;
            case R.id.game_yes:
                joinTask();
                break;
        }
    }

    //加入任务
    private void joinTask() {
        task.setFlag(-1);
        Log.d("result",TAG + " "+task.getId());
        startService(scanIntent);
        new AlertDialog.Builder(TaskDetails.this)
                .setTitle("提示")
                .setMessage("是否愿意将任务消息扩散给更多的人？此操作会消耗一些电量，同时您也会得到更多的奖励。")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("接受", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startService(new Intent(TaskDetails.this, AdvertiserService.class));
                        startActivity(new Intent(TaskDetails.this,TasksActivity.class));
                        task.setFlag(0);
                        taskDao.update(task);
                        finish();
                    }
                })
                .setNegativeButton("放弃", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startActivity(new Intent(TaskDetails.this,TasksActivity.class));
                        taskDao.update(task);
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
        Log.d("result",TAG +"onDestroy");
    }
}