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
import com.dell.treasure.dao.TaskDao;
import com.dell.treasure.rank.RegisterActivity;
import com.dell.treasure.service.PrepareService;
import com.dell.treasure.service.UserInfo;
import com.dell.treasure.share.InviteActivity;
import com.dell.treasure.source.TasksRepository;
import com.dell.treasure.source.local.TasksLocalDataSource;
import com.dell.treasure.support.ActivityUtils;
import com.dell.treasure.support.CurrentUser;
import com.dell.treasure.support.JpushReceiver;
import com.dell.treasure.support.MyApp;
import com.dell.treasure.support.NetUtil;
import com.orhanobut.logger.Logger;

import org.greenrobot.greendao.query.Query;
import org.ksoap2.SoapFault;

import java.util.List;


public class TasksActivity extends AppCompatActivity {
    public static final String TAG = "TasksActivity";
    private static final String CURRENT_FILTERING_KEY = "CURRENT_FILTERING_KEY";
    private MyApp myApp;
    private String username;
    private DrawerLayout mDrawerLayout;
    private TextView points;
    private TextView money;
    private TextView user;
    private UserInfoReceiver userInfoReceiver;
    private TaskInfoReceiver taskInfoReceiver;
    private LocalBroadcastManager broadcastManager;
    private NavigationView navigationView;
    private Fragment currentFragment;
    private int currentIndex;
    private Boolean isFirst;
    private SharedPreferences sp;
    private TasksPresenter mTasksPresenter;
    private TasksLocalDataSource mtasksLocalDataSource;
    private TasksRepository mtasksRepository;

    private CurrentUser userInfo;
    private String currentState;
    private String taskTmp;
    private String fromUserIdTmp;
    private Task task;
    private TaskDao taskDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tasks_act);

        myApp = (MyApp)getApplication();
        userInfo= CurrentUser.getOnlyUser();
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

        currentState = userInfo.getCurrentState();
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
//        currentFragment = new FristFragment();
//        switchContent(currentFragment);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(mTasksPresenter != null){
            mTasksPresenter.start();
//            mTasksPresenter.loadTasks(true);

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
//        调试设置,记得注销
        userInfo.setCurrentState("003");
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
//        if(!userInfo.getTasKind().equals("0")){
//            startService(new Intent(TasksActivity.this,NetService.class));
//        }
//        judgeCurrentState();
        isReceiveTaskFromUser();
        mTasksPresenter.loadTasks(true);
    }

    private void isReceiveTaskFromUser() {
        taskDao = MyApp.getInstance().getDaoSession().getTaskDao();
        task = Task.getInstance();
        taskTmp = userInfo.getTaskIdTmp();
        fromUserIdTmp = userInfo.getFromUserId();
        Log.d("result",TAG + " isReceiveTaskFromUser: "+taskTmp+" "+fromUserIdTmp);
        if(taskTmp != ""&& taskTmp != null){
            Log.d("result",TAG + " isReceiveTaskFromUser: received task from user");
            Query<Task> taskQuery = taskDao.queryBuilder().where(TaskDao.Properties.TaskId.eq(taskTmp)).build();
            List<Task> tasks = taskQuery.list();
            if (tasks.size() > 0) {
                task.setTask(tasks.get(0));
                taskTODO();
            }else {
                //查询任务；
                new getTaskInfo().execute();
            }
        }
        userInfo.setTaskIdTmp(null);
        userInfo.setFromUserId(null);
    }

    void taskTODO(){
        if(task.getFlag() >= -1){
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TasksActivity.this);
            alertDialogBuilder.setTitle("提示");
            alertDialogBuilder.setMessage("你好，你已经参与了该任务。。。");
            alertDialogBuilder.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            alertDialogBuilder.create().show();
        }else {
            userInfo.setLastId(fromUserIdTmp);
            task.setLastId(fromUserIdTmp);
            taskDao.update(task);
            Log.d("result",TAG + " taskTODO: "+task.getLastId());
            startActivity(new Intent(this,TaskDetails.class));
        }
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

            Intent intent = new Intent();
            startService(new Intent(TasksActivity.this, UserInfo.class));
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
            String point = intent.getStringExtra("points");
            String Money = intent.getStringExtra("money");
            points.setText(point);
            money.setText(Money);
        }
    }


    public class TaskInfoReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Boolean isEnd = intent.getBooleanExtra("isEnd",false);
            if(mTasksPresenter != null){
//            mTasksPresenter.start();
               mTasksPresenter.loadTasks(true);
                Logger.d("收到 ");
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

            Log.d("result",TAG + bleId +" "+date+" "+money+" "+needNum);
            userInfo.setLastId(fromUserIdTmp);
            userInfo.setStartTime(date);
            userInfo.setTarget_ble(bleId);
            userInfo.setTaskId(taskTmp);
            userInfo.setNeedNum(needNum);

            SharedPreferences tasknum = getSharedPreferences(JpushReceiver.TASK, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = tasknum.edit();
            editor.putString("startTime",date);  //任务开始时间
            editor.putString("money",money);
            editor.putString("needNum",needNum);
            editor.apply();

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            Intent intent = new Intent(TasksActivity.this,PrepareService.class);
            TasksActivity.this.startService(intent);

        }
    }

//    private class isSignTask extends AsyncTask<Void, Void, String> {
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected String doInBackground(Void... params) {
//            Boolean isSign = false;
//            try {
//                isSign = NetUtil.isSignPeriod();
//            } catch (SoapFault soapFault) {
//                soapFault.printStackTrace();
//            }
//            if(!isSign){
//                currentState = "001";
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            if(currentState.equals("000")){
//                //招募阶段
//                Intent intent = new Intent(TasksActivity.this, InviteActivity.class);
//                startActivity(intent);
//            }
//        }
//    }
}
