package com.dell.treasure.tasks;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.dell.treasure.R;
import com.dell.treasure.SignInActivity;
import com.dell.treasure.dao.Task;
import com.dell.treasure.service.AdvertiserService;
import com.dell.treasure.service.ScannerService;
import com.dell.treasure.service.UserInfo;
import com.dell.treasure.source.TasksRepository;
import com.dell.treasure.source.local.TasksLocalDataSource;
import com.dell.treasure.support.ActivityUtils;
import com.dell.treasure.support.CurrentUser;
import com.dell.treasure.support.MyApp;
import com.dell.treasure.support.NetUtil;
import com.orhanobut.logger.Logger;

import org.ksoap2.SoapFault;

import java.util.Date;

import static com.dell.treasure.support.TaskRelated.endTask;
import static com.dell.treasure.support.TaskRelated.isOverdue;
import static com.dell.treasure.support.ToolUtil.dateToString;


public class TasksActivity extends AppCompatActivity {
    public static final String TAG = "TasksActivity";
    private static final String CURRENT_FILTERING_KEY = "CURRENT_FILTERING_KEY";
    private MyApp myApp;
    private DrawerLayout mDrawerLayout;
    private TextView points;
    private TextView money;
    private TextView user;
    private UserInfoReceiver userInfoReceiver;
    private TaskInfoReceiver taskInfoReceiver;
    private LocalBroadcastManager broadcastManager;
    private NavigationView navigationView;
    private Boolean isFirst;
    private SharedPreferences sp;
    private TasksPresenter mTasksPresenter;
    private TasksLocalDataSource mtasksLocalDataSource;
    private TasksRepository mtasksRepository;

    private CurrentUser userInfo;
    private Task currentTask;
//    private String currentState;
    private String taskId;
    private String username;
    private String taskTmp;
    private String fromUserIdTmp;
    private String taskIdTemp;
    private String lastIdTemp;
    private int isCurrFlag;  //  0 没有任务  1 有任务  2 有任务，且正在执行

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tasks_act);
        myApp = (MyApp)getApplication();
        userInfo= CurrentUser.getOnlyUser();
        currentTask = userInfo.getCurrentTask();
        Logger.d("1 "+currentTask.toString());
        username = userInfo.getUsername();
        mtasksLocalDataSource = TasksLocalDataSource.getInstance();
        mtasksRepository = TasksRepository.getInstance(mtasksLocalDataSource);

//      侧边栏
        initNavigationViewHeader();
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        // 标题的文字需在setSupportActionBar之前，不然会无效
        mToolbar.setTitle("校园寻宝");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar,
                R.string.abc_action_bar_home_description,
                R.string.abc_action_bar_home_description_format);
        mDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(mDrawerToggle);
//      fragment
        initFragment(savedInstanceState);

        //注册本地广播
        broadcastManager = LocalBroadcastManager.getInstance(TasksActivity.this);
        userInfoReceiver = new UserInfoReceiver();
        taskInfoReceiver = new TaskInfoReceiver();
        IntentFilter intentFilter = new IntentFilter();
        IntentFilter taskFilter = new IntentFilter();
        intentFilter.addAction("com.dell.treasure.RECEIVER_UserInfo");
        taskFilter.addAction("com.dell.treasure.RECEIVER_TaskInfo");
        broadcastManager.registerReceiver(userInfoReceiver, intentFilter);
        broadcastManager.registerReceiver(taskInfoReceiver, taskFilter);

//        currentState = userInfo.getCurrentState();
//        if(currentState.equals("000")||currentState.equals("005")){
//            new isSignTask().execute();
//        }

        //判断是否第一次启动
        sp = getSharedPreferences(username, Context.MODE_PRIVATE);

    }

    private void initFragment(Bundle savedInstanceState) {
        navigationView.getMenu().getItem(0).setChecked(true);

        TasksFragment tasksFragment =
                (TasksFragment) getSupportFragmentManager().findFragmentById(R.id.realtabcontent);
        if (tasksFragment == null) {
            // Create the fragment
            tasksFragment = TasksFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), tasksFragment, R.id.realtabcontent);
        }

        // Create the presenter
        mTasksPresenter = new TasksPresenter(mtasksRepository, tasksFragment);

        // Load previously saved state, if available.
        if (savedInstanceState != null) {
            TasksFilterType currentFiltering =
                    (TasksFilterType) savedInstanceState.getSerializable(CURRENT_FILTERING_KEY);
            mTasksPresenter.setFiltering(currentFiltering);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Logger.d("2");
        if(mTasksPresenter != null){
            Logger.d("2.1");
            mTasksPresenter.start();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d("3");
        taskIdTemp = null;
        lastIdTemp = null;
        isFirst = sp.getBoolean("FIRST", true);
        String points1 = sp.getString("points", "");
        String money1 = sp.getString("money", "");
        money.setText(money1);
        points.setText(points1);
        user.setText(username);
        if (!myApp.mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
        isFirstSign();
        initCurrenTask();
        isReceiveTaskFromUser();

//        if(!userInfo.getTasKind().equals("0")){
//            startService(new Intent(TasksActivity.this,NetService.class));
//        }
//        judgeCurrentState();

//        mTasksPresenter.loadTasks(true);
    }

    public void initCurrenTask(){
        taskId = userInfo.getTaskId();
        if(taskId != null){
            Logger.d(taskId);
            //有任务
            if(mtasksRepository.isTaskExist(taskId)) {
                currentTask.setTask(mtasksRepository.getTask(taskId));
                if(isOverdue(currentTask.getStartTime())){
                    //任务过期
                    endTask(TasksActivity.this,userInfo);
                    isCurrFlag = 0;
                }else {
                    //有任务
                    if(currentTask.getFlag() == -2){
                        isCurrFlag = 1;
                    }else{
                        //正在执行
                        isCurrFlag = 2;
                        initData();
                    }
                }
            }
        }else{
            Logger.d("当前没接收到任务");
            //当前没接收到任务
            Task taskTmp = mtasksRepository.getActivieTask();
            isCurrFlag = 0;
            if(taskTmp != null) {
                currentTask.setTask(taskTmp);
                if (isOverdue(currentTask.getStartTime())) {
                    //任务过期
                    endTask(TasksActivity.this, userInfo);
                    isCurrFlag = 0;
                } else {
                    //有任务
                    if (currentTask.getFlag() == -2) {
                        isCurrFlag = 1;
                    } else {
                        //正在执行
                        isCurrFlag = 2;
                        initData();
                    }
                }
            }

        }
    }
    void initData(){
        Logger.d("4");
        if(!ScannerService.running){
            restartDialog(currentTask.getFlag());
        }
    }

    //提示
    private void restartDialog(final int flag) {
        Logger.d("重新开始");
        new getRecordTimeDis().execute();
        Intent startScanService = new Intent(this, ScannerService.class);
        startService(startScanService);
        Intent startAdvService = new Intent(this, AdvertiserService.class);
        if(flag == 0){
            startService(startAdvService);
        }
//        new AlertDialog.Builder(context)
//                .setTitle("提示")
//                .setMessage("您上次任务未结束，是否继续参与？")
//                .setIcon(android.R.drawable.ic_dialog_info)
//                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        Intent startScanService = new Intent(context, ScannerService.class);
//                        context.startService(startScanService);
//                        Intent startAdvService = new Intent(context, AdvertiserService.class);
//                        if(flag == 0){
//                            context.startService(startAdvService);
//                        }
//
//                    }
//                })
//                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        task.setFlag(-2);
//                        taskDao.update(task);
//                        refresh();
//                        dialog.dismiss();
//                    }
//                })
//                .show();
    }



    private void isReceiveTaskFromUser() {
        taskTmp = userInfo.getTaskIdTmp();
        fromUserIdTmp = userInfo.getFromUserId();
        Logger.d("isReceiveTaskFromUser: "+taskTmp+" "+fromUserIdTmp);
        if(taskTmp != ""&& taskTmp != null){
            // 收到分享
            if(isCurrFlag == 2){
                Logger.d("不接受分享");
                String dialog = "你好，当前正在参与任务。。。";
                PopupDialog(dialog);
            }else if(isCurrFlag == 1){
                Logger.d("从本地获取详细信息");
                if(taskTmp.equals(currentTask.getTaskId())){
                    currentTask.setLastId(fromUserIdTmp);
                    startActivity(new Intent(this,TaskDetails.class));
                }else {
                    String dialog = "你好，该任务已过期。。。";
                    PopupDialog(dialog);
                }
            }else if(isCurrFlag == 0){
                Logger.d("从服务端获取详细信息");
                taskIdTemp = taskTmp;
                lastIdTemp = fromUserIdTmp;
                getShareInfo();
            }
        }
        userInfo.setTaskIdTmp(null);
        userInfo.setFromUserId(null);
    }

    private void getShareInfo(){
        if (mtasksRepository.isTaskExist(taskIdTemp)) {
            currentTask.setTask(mtasksRepository.getTask(taskIdTemp));
            if(isOverdue(currentTask.getStartTime())){
                currentTask.setTask(new Task());
                String dialog = "你好，该任务已过期。。。";
                PopupDialog(dialog);
            }else {
                currentTask.setLastId(lastIdTemp);
                startActivity(new Intent(this,TaskDetails.class));
            }
        }else {
            //查询任务；
            new getTaskInfo().execute();
        }
    }

    private void PopupDialog(String dialog){
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TasksActivity.this);
        alertDialogBuilder.setTitle("提示");
        alertDialogBuilder.setMessage(dialog);
        alertDialogBuilder.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        alertDialogBuilder.create().show();
    }


//    private void judgeCurrentState() {
//        Intent intent = new Intent();
//        currentState = userInfo.getCurrentState();
//        switch (currentState){
//            case "001":
//                //招募结束
//                intent.setClass(TasksActivity.this,RegisterActivity.class);
//                startActivity(intent);
//                break;
//            case "002":
//                //任务等待
//            case "004":
//                //任务结束
//            case "003":
//                //任务进行中
//                mTasksPresenter.setFiltering(TasksFilterType.ACTIVE_TASKS);
//                mTasksPresenter.loadTasks(false);
//                break;
//            default:
//                break;
//        }
//    }

    private void isFirstSign() {
        if(isFirst){
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("FIRST",false);
            editor.apply();
            Intent get_info = new Intent(TasksActivity.this, UserInfo.class);
            startService(get_info);
            firstSign();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    private void initNavigationViewHeader() {
        navigationView = (NavigationView) findViewById(R.id.navigation);
        //设置头像，布局app:headerLayout="@layout/drawer_header"所指定的头布局
        View view = navigationView.inflateHeaderView(R.layout.drawer_header);
        points = (TextView)view.findViewById(R.id.points);
        money = (TextView)view.findViewById(R.id.money);
        user = (TextView)view.findViewById(R.id.user);
        //菜单点击事件
        navigationView.setNavigationItemSelectedListener(new NavigationItemSelected());
    }

    private void completedTasks() {
        //调转到已完成任务清单
    }

    public void switchContent(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.realtabcontent, fragment).commit();
        invalidateOptionsMenu();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(CURRENT_FILTERING_KEY, mTasksPresenter.getFiltering());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        broadcastManager.unregisterReceiver(userInfoReceiver);
        broadcastManager.unregisterReceiver(taskInfoReceiver);
    }

    private void firstSign() {
        new AlertDialog.Builder(TasksActivity.this)
                .setTitle("")
                .setMessage(R.string.notice)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("了解", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        navigationView.getMenu().getItem(0).setChecked(true);
                        dialog.dismiss();

                    }
                })
                .show();
    }

    private void exit() {
        new AlertDialog.Builder(TasksActivity.this)
                .setTitle("提示")
                .setMessage("确定退出？")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sp = getSharedPreferences(SignInActivity.USER_INFO, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putBoolean(SignInActivity.AUTO_SIGNIN, false);
                        editor.apply();
                        Intent intent = new Intent(TasksActivity.this,SignInActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        navigationView.getMenu().getItem(0).setChecked(true);
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {  //获取 back键
            exit();
        }
        return false;
    }

    private class NavigationItemSelected implements NavigationView.OnNavigationItemSelectedListener{

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

//            Intent intent = new Intent();
//            startService(new Intent(TasksActivity.this, UserInfo.class));
            switch (item.getItemId()){
                case R.id.navigation_item_yes:
//                    currentIndex = 0;
                    item.setChecked(true);
                    mTasksPresenter.setFiltering(TasksFilterType.ACTIVE_TASKS);
                    mTasksPresenter.loadTasks(false);

//                    currentFragment = new FristFragment();
//                    switchContent(currentFragment);
                    break;
                case R.id.navigation_item_query:
                    firstSign();
                    break;
                case R.id.navigation_item_no:
                    exit();
                    break;
//                case R.id.navigation_item_register:
//                    if(currentState.equals("000")){
//                        Toast.makeText(TasksActivity.this,"注册阶段结束，方可查看",Toast.LENGTH_LONG).show();
//                    }else {
//                        intent.setClass(TasksActivity.this, RegisterActivity.class);
//                        startActivity(intent);
//                        item.setCheckable(false);
//                    }
//                    break;
                case R.id.navigation_item_task:
                    mTasksPresenter.setFiltering(TasksFilterType.COMPLETED_TASKS);
                    mTasksPresenter.loadTasks(false);

                    break;
//                case R.id.navigation_item_share:
//                    intent.setClass(TasksActivity.this, InviteActivity.class);
//                    startActivity(intent);
//                    item.setCheckable(false);
//                    break;
                default:
                    break;
            }
            mDrawerLayout.closeDrawers();
            return true;
        }
    }

    public class UserInfoReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("result","UserInfo start");
            String point = intent.getStringExtra("points");
            String Money = intent.getStringExtra("money");
            points.setText(point);
            money.setText(Money);
        }
    }


    public class TaskInfoReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("result","TaskInfo start");
            Boolean isEnd = intent.getBooleanExtra("isEnd",false);
            if(mTasksPresenter != null){
//            mTasksPresenter.start();
                if(currentTask.getTaskId()!= null && currentTask.getFlag() <2) {
                    currentTask.setFlag(2);
                    mtasksRepository.completeTask(currentTask);
                }
                userInfo.setTaskId(null);
                userInfo.currentTaskClear();
//                currentTask = userInfo.getCurrentTask();

//                mtasksRepository.init();
                mTasksPresenter.loadTasks(false);
            }
        }
    }

    private class getTaskInfo extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            String json = null;
            taskTmp = taskIdTemp;
            try {
                json = NetUtil.getTaskInfo(taskTmp);
            } catch (SoapFault | NullPointerException soapFault) {
                soapFault.printStackTrace();
            }
            //获取消息
            String[] split = json.split(";");
            String bleId = split[0];
            String date = split[1];
            String money = split[2];
            String needNum = split[3];

            currentTask.setTask(new Task(null,fromUserIdTmp,taskTmp,bleId,date,null,0.0,"0",needNum,"0","0",money,-2));
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(isOverdue(currentTask.getStartTime())){
                currentTask.setTask(new Task());
                String dialog = "你好，该任务已过期。。。";
                PopupDialog(dialog);
            }else {
                mtasksRepository.saveTask(currentTask);
                Intent intent = new Intent(TasksActivity.this,TaskDetails.class);
                TasksActivity.this.startActivity(intent);
                Logger.d("执行顺序 4 "+currentTask.toString());
            }
        }
    }

    private class getRecordTimeDis extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            String json = null;
            String time = "0";
            String distance = "0";
            try {
                json = NetUtil.getRecordTimeDis(currentTask.getTaskId(),userInfo.getUserId());
            } catch (SoapFault | NullPointerException soapFault) {
                soapFault.printStackTrace();
            }

            if(json.equals("0")){

            }else {
                //获取消息
                String[] split = json.split(";");
                time = split[0];
                distance = split[1];


                currentTask.setLength(Double.parseDouble(time));
                currentTask.setDistance(distance);
                currentTask.setBeginTime(dateToString(new Date()));

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {

        }
    }
}
