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
import com.dell.treasure.service.AdvertiserService;
import com.dell.treasure.service.ScannerService;
import com.dell.treasure.support.CurrentUser;

/**
 * Created by DELL on 2017/7/12.
 */

public class TaskDetails extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "TaskDetails";
    private CurrentUser user;
    private Button game_yes;
    private Button game_no;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("result",TAG);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taskdetails);
        user = CurrentUser.getOnlyUser();
        user.setTasKind("0");
        initView();

    }

    void initView(){
        game_yes = (Button) findViewById(R.id.game_yes);
        game_no = (Button) findViewById(R.id.game_no);

        game_yes.setOnClickListener(this);
        game_no.setOnClickListener(this);
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
        new AlertDialog.Builder(TaskDetails.this)
                .setTitle("提示")
                .setMessage("是否愿意将任务消息扩散给更多的人？此操作会消耗一些电量，同时您也会得到更多的奖励。")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("帮助", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startService(new Intent(TaskDetails.this, AdvertiserService.class));
                        startActivity(new Intent(TaskDetails.this,TasksActivity.class));
                        finish();
                    }
                })
                .setNegativeButton("放弃", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startActivity(new Intent(TaskDetails.this,TasksActivity.class));
                        finish();
                    }
                })
                .show();
    }

    //拒绝参与任务
    public void onBack() {
        startActivity(new Intent(TaskDetails.this,TasksActivity.class));
        if(ScannerService.running){
            Intent scanIntent = new Intent(TaskDetails.this,ScannerService.class);
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