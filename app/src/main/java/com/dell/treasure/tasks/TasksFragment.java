/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dell.treasure.tasks;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dell.treasure.R;
import com.dell.treasure.dao.Task;
import com.dell.treasure.dao.TaskDao;
import com.dell.treasure.rank.TaskRankActivity;
import com.dell.treasure.service.AdvertiserService;
import com.dell.treasure.service.ScannerService;
import com.dell.treasure.support.ActivityUtils;
import com.dell.treasure.support.CommonUtils;
import com.dell.treasure.support.CurrentUser;
import com.dell.treasure.support.JpushReceiver;
import com.dell.treasure.support.MyApp;
import com.mob.moblink.ActionListener;
import com.mob.moblink.MobLink;

import org.greenrobot.greendao.query.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.sharesdk.onekeyshare.OnekeyShare;


public class TasksFragment extends Fragment implements TasksContract.View {
    public static final String TAG = "TasksFragment";
    public final static String PAR_KEY = "treasure.task";
    private static CurrentUser user;
    private TasksContract.Presenter mPresenter;
    /**
     * Listener for clicks on tasks in the ListView.
     */
    TaskItemListener mItemListener = new TaskItemListener() {
        @Override
        public void onTaskClick(Task taskFlag) {
            mPresenter.openTaskDetails(taskFlag);
        }

//        @Override
//        public void onCompleteTaskClick(Task completedTask) {
//            mPresenter.completeTask(completedTask);
//        }
//
//        @Override
//        public void onActivateTaskClick(Task activatedTask) {
//            mPresenter.activateTask(activatedTask);
//        }
    };
    private TasksAdapter mListAdapter;
    private View mNoTasksView;
    private ImageView mNoTaskIcon;
    private TextView mNoTaskMainView;
    private LinearLayout mTasksView;
    private TextView mFilteringLabelView;
    private Task task;
    private TaskDao taskDao;
    private Context context;
    private String mobID;
    private String userId;
    private String taskId;
    private String strategy_text;
    private String money_text;
    private SharedPreferences sp;

    public TasksFragment() {

    }

    public static TasksFragment newInstance() {
        return new TasksFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = CurrentUser.getOnlyUser();
        userId = user.getUserId();
        context = getActivity();
        mListAdapter = new TasksAdapter(new ArrayList<Task>(0), mItemListener);
//        mListAdapter = new TasksAdapter(new ArrayList<Task>(0));
        sp = getActivity().getSharedPreferences(JpushReceiver.TASK, Context.MODE_PRIVATE);
        initData();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
        isReceiveTaskFromUser();
    }

    private void isReceiveTaskFromUser() {
    }

    private void isRestartService() {
        Intent startScanService = new Intent(context, ScannerService.class);
        switch (task.getFlag()){
            case -3:
//                user.setNetConn(false);
                user.setJoin(false);
                context.startService(startScanService);
                break;
            case 0:
            case -1:
                user.setJoin(true);
//                user.setNetConn(true);
                restartDialog(task.getFlag());
                break;
            case -2:
                user.setJoin(false);
//                user.setNetConn(true);
                break;
            default:
                break;
        }
    }

    void initData(){
        taskDao = MyApp.getInstance().getDaoSession().getTaskDao();
        task = Task.getInstance();
        Query<Task> taskQuery = taskDao.queryBuilder().where(TaskDao.Properties.Flag.ge(-3),TaskDao.Properties.Flag.le(0)).build();
        List<Task> tasks = taskQuery.list();
        if (tasks.size() > 0) {
            task.setTask(tasks.get(0));
            Log.d("result",TAG +"taskID: "+task.getTaskId());
            if(!ScannerService.running){
                isRestartService();
            }
        }
    }

//提示
    private void restartDialog(final int flag) {
        new AlertDialog.Builder(context)
                .setTitle("提示")
                .setMessage("您上次任务未结束，是否继续参与？")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent startScanService = new Intent(context, ScannerService.class);
                        context.startService(startScanService);
                        Intent startAdvService = new Intent(context, AdvertiserService.class);
                        if(flag == 0){
                            context.startService(startAdvService);
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        mPresenter.result(requestCode, resultCode);
//    }

    @Override
    public void setPresenter(@NonNull TasksContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.tasks_frag, container, false);
        // Set up tasks view
        ListView listView = (ListView) root.findViewById(R.id.tasks_list);
        listView.setAdapter(mListAdapter);
        mFilteringLabelView = (TextView) root.findViewById(R.id.filteringLabel);
        mTasksView = (LinearLayout) root.findViewById(R.id.tasksLL);

        // Set up  no tasks view
        mNoTasksView = root.findViewById(R.id.noTasks);
        mNoTaskIcon = (ImageView) root.findViewById(R.id.noTasksIcon);
        mNoTaskMainView = (TextView) root.findViewById(R.id.noTasksMain);

//        mNoTaskAddView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showAddTask();
//            }
//        });


//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mPresenter.addNewTask();
//            }
//        });

        // Set up progress indicator
        final ScrollChildSwipeRefreshLayout swipeRefreshLayout =
                (ScrollChildSwipeRefreshLayout) root.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.primary),
                ContextCompat.getColor(getActivity(), R.color.accent),
                ContextCompat.getColor(getActivity(), R.color.primary_dark)
        );
        // Set the scrolling view in the custom SwipeRefreshLayout.
        swipeRefreshLayout.setScrollUpChild(listView);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.loadTasks(false);
            }
        });

        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.menu_clear:
//                mPresenter.clearCompletedTasks();
//                break;
            case R.id.menu_filter:
                showFilteringPopUpMenu();
                break;
            case R.id.menu_refresh:
                mPresenter.loadTasks(true);
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tasks_fragment_menu, menu);
    }

    @Override
    public void showFilteringPopUpMenu() {
        PopupMenu popup = new PopupMenu(getContext(), getActivity().findViewById(R.id.menu_filter));
        popup.getMenuInflater().inflate(R.menu.filter_tasks, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.active:
                        mPresenter.setFiltering(TasksFilterType.ACTIVE_TASKS);
                        break;
                    case R.id.completed:
                        mPresenter.setFiltering(TasksFilterType.COMPLETED_TASKS);
                        break;
                    default:
                        mPresenter.setFiltering(TasksFilterType.ALL_TASKS);
                        break;
                }
                mPresenter.loadTasks(false);
                return true;
            }
        });

        popup.show();
    }

    @Override
    public void setLoadingIndicator(final boolean active) {

        if (getView() == null) {
            return;
        }
        final SwipeRefreshLayout srl =
                (SwipeRefreshLayout) getView().findViewById(R.id.refresh_layout);

        // Make sure setRefreshing() is called after the layout is done with everything else.
        srl.post(new Runnable() {
            @Override
            public void run() {
                srl.setRefreshing(active);
            }
        });
    }

    @Override
    public void showTasks(List<Task> tasks) {
        mListAdapter.replaceData(tasks);

        mTasksView.setVisibility(View.VISIBLE);
        mNoTasksView.setVisibility(View.GONE);
    }


    @Override
    public void showNoActiveTasks() {
        showNoTasksViews(
                getResources().getString(R.string.no_tasks_active),
                R.drawable.ic_check_circle_24dp
        );
    }

    @Override
    public void showNoTasks() {
        showNoTasksViews(
                getResources().getString(R.string.no_tasks_all),
                R.drawable.ic_assignment_turned_in_24dp
        );
    }

    @Override
    public void showNoCompletedTasks() {
        showNoTasksViews(
                getResources().getString(R.string.no_tasks_completed),
                R.drawable.ic_verified_user_24dp
        );
    }

//    @Override
//    public void showSuccessfullySavedMessage() {
//        showMessage(getString(R.string.successfully_saved_task_message));
//    }

    private void showNoTasksViews(String mainText, int iconRes) {
        mTasksView.setVisibility(View.GONE);
        mNoTasksView.setVisibility(View.VISIBLE);

        mNoTaskMainView.setText(mainText);
        mNoTaskIcon.setImageDrawable(getResources().getDrawable(iconRes));
    }

    @Override
    public void showActiveFilterLabel() {
        mFilteringLabelView.setText(getResources().getString(R.string.label_active));
    }

    @Override
    public void showCompletedFilterLabel() {
        mFilteringLabelView.setText(getResources().getString(R.string.label_completed));
    }

    @Override
    public void showAllFilterLabel() {
        mFilteringLabelView.setText(getResources().getString(R.string.label_all));
    }

//    @Override
//    public void showAddTask() {
//        Intent intent = new Intent(getContext(), AddEditTaskActivity.class);
//        startActivityForResult(intent, AddEditTaskActivity.REQUEST_ADD_TASK);
//    }
//
    @Override
    public void showTaskDetailsUi(Task taskFlag) {
        // in it's own Activity, since it makes more sense that way and it gives us the flexibility
        // to show some Intent stubbing.
        int flag = taskFlag.getFlag();
        Intent intent = new Intent();
        if(flag >= 2) {
            intent.setClass(getContext(), TaskRankActivity.class);
            intent.putExtra("TaskId",taskFlag.getTaskId());
            startActivity(intent);
        }else if(flag >=-1 && flag <2){
//            intent.setClass(getContext(), TaskRankActivity.class);
//            intent.putExtra("TaskId",taskFlag.getTaskId());
//            startActivity(intent);
            intent.setClass(getContext(), ActiveTackDetails.class);
            Bundle extra = new Bundle();
            extra.putParcelable(PAR_KEY,taskFlag);
            intent.putExtras(extra);
            startActivity(intent);
        }else if(flag <- 1){
            intent.setClass(getContext(), TaskDetails.class);
            startActivity(intent);
        }

    }

    @Override
    public void showTaskMarkedComplete() {
        showMessage(getString(R.string.task_marked_complete));
    }

//    @Override
//    public void showTaskMarkedActive() {
//        showMessage(getString(R.string.task_marked_active));
//    }

    @Override
    public void showCompletedTasksCleared() {
        showMessage(getString(R.string.completed_tasks_cleared));
    }

    @Override
    public void showLoadingTasksError() {
        showMessage(getString(R.string.loading_tasks_error));
    }

    private void showMessage(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean isActive() {
        return isAdded();
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
//        oks.setTitleUrl("http://sharesdk.cn");
        oks.setUrl("myApp://myweb.com/openApp");
        // text是分享文本，所有平台都需要这个字段
        oks.setText("邀请您参加寻宝任务，一起分享奖励");
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        oks.setImagePath(CommonUtils.copyImgToSD(getActivity(), R.drawable.demo_share_invite , "invite"));//确保SDcard下面存在此张图片
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
        oks.show(getActivity());
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
        String imgPath = CommonUtils.copyImgToSD(getActivity(), R.mipmap.ic_launcher , "invite");
        CommonUtils.showShare(getActivity(), title, text, shareUrl, imgPath);
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
                    Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        String shareUrl = CommonUtils.SHARE_URL + CommonUtils.MAIN_PATH_ARR;
        if (!TextUtils.isEmpty(mobID)) {
            shareUrl += "?mobid=" + mobID;
        }
        String title = getString(R.string.show_share_titel);
        String text = getString(R.string.share_text);
        String imgPath = CommonUtils.copyImgToSD(getActivity(), R.drawable.demo_share_moblink , "moblink");
        CommonUtils.showShare(getActivity(), title, text, shareUrl, imgPath);
    }
    public interface TaskItemListener {

        void onTaskClick(Task taskFlag);

//        void onCompleteTaskClick(Task completedTask);
//
//        void onActivateTaskClick(Task activatedTask);
    }

    private  class TasksAdapter extends BaseAdapter {

        private List<Task> mTasks;
        private TaskItemListener mItemListener;

        public TasksAdapter(List<Task> tasks, TaskItemListener itemListener) {
            setList(tasks);
            mItemListener = itemListener;
        }

        public TasksAdapter(List<Task> tasks) {
            setList(tasks);
        }

        public void replaceData(List<Task> tasks) {
            setList(tasks);
            notifyDataSetChanged();
        }

        private void setList(List<Task> tasks) {
            mTasks = tasks;
        }

        @Override
        public int getCount() {
            return mTasks.size();
        }

        @Override
        public Task getItem(int i) {
            return mTasks.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            final Task item = mTasks.get(i);
            final ViewHolder holder;
            if (view == null) {
                holder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                if(Integer.parseInt(item.getTaskId()) < 1){
                    view = inflater.inflate(R.layout.task_item_active, viewGroup, false);
                    holder.strategy = (TextView) view.findViewById(R.id.strategy_text);
                    holder.money = (TextView) view.findViewById(R.id.money);
                    strategy_text  = sp.getString("strategy", null);
                    money_text = sp.getString("money",null);
                    if(strategy_text != null){
                        holder.strategy.setText(strategy_text);
                    }
                    if(money_text != null){
                        holder.money.setText(money_text);
                    }
                }else {
                    view = inflater.inflate(R.layout.task_item, viewGroup, false);
                }
                holder.time = (TextView) view.findViewById(R.id.time);
                holder.pic = (ImageView) view.findViewById(R.id.imgView);
                holder.share = (ImageButton) view.findViewById(R.id.btn_share);

                holder.join = (Button) view.findViewById(R.id.join);
                view.setTag(holder);
            }else {
                holder = (ViewHolder) view.getTag();
            }

            holder.time.setText("开始时间:"+ getItem(i).getBeginTime());
            final Task task = getItem(i);
            switch (task.getFlag()){
                case -2:
                    holder.join.setText("未参与");
                    break;
                case -1:
                    holder.join.setText("已参与");
                    break;
                case 0:
                    holder.join.setText("已参与");
                    break;
                case 1:
                    holder.join.setText("未提交");
                    break;
                case 2:
//                    暂时注释
                    holder.join.setText("结束");
                    holder.share.setVisibility(View.GONE);
                    break;
                case 3:
                    holder.join.setText("任务过期");
                    holder.share.setVisibility(View.GONE);
                    break;
            }
            holder.share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    showShare();
//                    setDefault();
                    taskId = item.getTaskId();
                    share();
                }
            });

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mItemListener.onTaskClick(task);
                }
            });

            return view;
        }
         class ViewHolder{
             ImageView pic;
             TextView money;
             TextView time;
             TextView strategy;
             Button join;
             ImageButton share;
        }
    }

}
