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
import android.os.AsyncTask;
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
import android.widget.ScrollView;
import android.widget.TextView;

import com.dell.treasure.R;
import com.dell.treasure.dao.Task;
import com.dell.treasure.dao.TaskDao;
import com.dell.treasure.rank.TaskRankActivity;
import com.dell.treasure.service.AdvertiserService;
import com.dell.treasure.service.ScannerService;
import com.dell.treasure.support.CommonUtils;
import com.dell.treasure.support.CurrentUser;
import com.dell.treasure.support.JpushReceiver;
import com.dell.treasure.support.NetUtil;
import com.mob.moblink.ActionListener;
import com.mob.moblink.MobLink;
import com.orhanobut.logger.Logger;

import org.ksoap2.SoapFault;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.dell.treasure.support.ToolUtil.dateToString;


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
    private Task currenTask;
    private Context context;
    private String mobID;
    private String userId;
    private String taskId;
    private String strategy_text;
    private SharedPreferences sp;
    private TextView tips;

    public TasksFragment() {

    }

    public static TasksFragment newInstance() {
        return new TasksFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = CurrentUser.getOnlyUser();
        currenTask = user.getCurrentTask();
        context = getActivity();
        mListAdapter = new TasksAdapter(new ArrayList<Task>(0), mItemListener,TasksFilterType.ACTIVE_TASKS);
//        mListAdapter = new TasksAdapter(new ArrayList<Task>(0));
        sp = getActivity().getSharedPreferences(JpushReceiver.TASK, Context.MODE_PRIVATE);

    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.d("5");
        mPresenter.start();
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
        tips = (TextView) root.findViewById(R.id.tips);
        tips.setText(R.string.task_tips);
        if(user.getSign()){
            mNoTaskMainView.setText(R.string.is_sign_period);
        }else {
            mNoTaskMainView.setText(R.string.no_tasks_all);
        }

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
                refresh();
                break;
        }
        return true;
    }

    private void refresh(){
        mPresenter.loadTasks(false);
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
//                    default:
//                        mPresenter.setFiltering(TasksFilterType.ALL_TASKS);
//                        break;
                }
                mPresenter.loadTasks(false);
                Logger.d(currenTask.toString());
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
    public void showTasks(List<Task> tasks,TasksFilterType type) {
        mListAdapter.replaceData(tasks,type);
        Logger.d("tasks: "+tasks.toString());
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
        if(flag == 2) {
            intent.setClass(getContext(), TaskRankActivity.class);
            intent.putExtra("TaskId",taskFlag.getTaskId());
            startActivity(intent);
        }else if(flag >=-1 && flag <2){
//            intent.setClass(getContext(), TaskRankActivity.class);
//            intent.putExtra("TaskId",taskFlag.getTaskId());
//            startActivity(intent);
            intent.setClass(getContext(), ActiveTackDetails.class);
//            Bundle extra = new Bundle();
//            extra.putParcelable(PAR_KEY,taskFlag);
//            intent.putExtras(extra);
            startActivity(intent);
        }else if(flag == -2){
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

    private void getMobID(String taskId){
        HashMap<String, Object> params = new HashMap<String, Object>();
        String source = "MobLinkDemo";
        String key1 = "userId";
        String key2 = "taskId";
        String value1 = user.getUserId();
        if(taskId == null){
            taskId = currenTask.getTaskId();
        }
        String value2 = taskId;
        params.put(key1, value1);
        params.put(key2, value2);
        Logger.d("share " + value1+" "+value2);

        MobLink.getMobID(params, CommonUtils.MAIN_PATH_ARR, source, new ActionListener() {
            public void onResult(HashMap<String, Object> params) {
                if (params != null && params.containsKey("mobID")) {
                    mobID = String.valueOf(params.get("mobID"));
                    Log.d("result", "mobId: "+ mobID);
                }
            }

            public void onError(Throwable t) {
            }
        });
    }

    private void share(String taskId) {
//        String shareUrl = "mlink://treasure.com"+ CommonUtils.MAIN_PATH_ARR;
        getMobID(taskId);
        if (TextUtils.isEmpty(mobID)) {
//            getMobID(taskId);
            CommonUtils.getMobIdDialog(getActivity()).show();
            return;
        }
        String shareUrl = CommonUtils.SHARE_URL;
        if (!TextUtils.isEmpty(mobID)) {
            shareUrl += "?mobid=" + mobID;
        }


        String title = getString(R.string.invite_share_titel);
        String text = getString(R.string.share_text);
        String imgPath = CommonUtils.copyImgToSD(getActivity(), R.mipmap.ic_launcher , "invite");
        CommonUtils.showShare(getActivity(), title, text, shareUrl, imgPath);
        Logger.d("share "+shareUrl);

    }
    public interface TaskItemListener {

        void onTaskClick(Task taskFlag);

//        void onCompleteTaskClick(Task completedTask);
//
//        void onActivateTaskClick(Task activatedTask);
    }

    private  class TasksAdapter extends BaseAdapter {
        public static final int TYPE_ACTIVE = 0;
        public static final int TYPE_COMPLETED = 1;

        private List<Task> mTasks;
        private TaskItemListener mItemListener;
        private TasksFilterType mType;

        public TasksAdapter(List<Task> tasks, TaskItemListener itemListener,TasksFilterType type) {
            setList(tasks);
            mItemListener = itemListener;
            mType = type;
        }

        public TasksAdapter(List<Task> tasks) {
            setList(tasks);
        }

        public void replaceData(List<Task> tasks,TasksFilterType type) {
            setList(tasks);
            mType = type;
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
        public int getItemViewType(int position) {
            int flag = mTasks.get(position).getFlag();
            if( flag < 1){
                return TYPE_ACTIVE;
            }else {
                return TYPE_COMPLETED;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            final Task task = getItem(i);
            final ViewHolder holder;
            final CompletedHolder comHolder;
            switch (getItemViewType(i)) {
                case TYPE_ACTIVE:
                    if (view == null) {
                        holder = new ViewHolder();
                        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                        view = inflater.inflate(R.layout.task_item_active, viewGroup, false);
                        holder.strategy = (TextView) view.findViewById(R.id.strategy_text);
                        holder.money = (TextView) view.findViewById(R.id.money);
                        holder.scrollView = (ScrollView) view.findViewById(R.id.scrollView);
                        holder.share = (ImageButton) view.findViewById(R.id.btn_share);
                        holder.time = (TextView) view.findViewById(R.id.time);
                        holder.pic = (ImageView) view.findViewById(R.id.imgView);
                        holder.join = (Button) view.findViewById(R.id.join);
                        view.setTag(holder);
                    } else {
                        holder = (ViewHolder) view.getTag();
                    }

                    strategy_text = sp.getString("strategy", null);
                    if (strategy_text != null) {
                        holder.strategy.setText(strategy_text);
                    }
                    holder.money.setText("任务金额:" + getItem(i).getMoney());
                    holder.time.setText("开始时间:" + getItem(i).getBeginTime());

                    switch (task.getFlag()) {
                        case -2:
                            holder.join.setText("未参与");
                            break;
                        case -1:
                            holder.join.setText("已参与");
                            break;
                        case 0:
                            holder.join.setText("已参与");
                            break;
//                        case 1:
//                            holder.join.setText("结束");
//                            break;
//                        case 2:
//                            holder.join.setText("结束");
//                            break;
//                        case -3:
//                            holder.join.setText("参与任务人数已达上限");
//                            break;
                        default:
                            break;
                    }
                    holder.share.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(task.getFlag() == -2){
                                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                alertDialogBuilder.setTitle("提示");
                                alertDialogBuilder.setMessage("只有参与到任务中，才可以分享任务");
                                alertDialogBuilder.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
                                    }
                                });
                                alertDialogBuilder.create().show();
                            }else {
//                                taskId = task.getTaskId();
//                                share(taskId);
//                                Logger.d("share "+ taskId+" "+task.toString());
                                Intent intent = new Intent(getContext(), ActiveTackDetails.class);
                                startActivity(intent);
                            }
                        }
                    });
                    break;
                case TYPE_COMPLETED:
                    if (view == null) {
                        comHolder = new CompletedHolder();
                        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

                        view = inflater.inflate(R.layout.task_item, viewGroup, false);
                        comHolder.time = (TextView) view.findViewById(R.id.time1);
                        comHolder.pic = (ImageView) view.findViewById(R.id.imgView1);
                        comHolder.join = (Button) view.findViewById(R.id.join1);
                        view.setTag(comHolder);
                    } else {
                        comHolder = (CompletedHolder) view.getTag();
                    }
                    comHolder.time.setText("开始时间:" + getItem(i).getBeginTime());
                    switch (task.getFlag()) {
//                        case -2:
//                            comHolder.join.setText("未参与");
//                            break;
//                        case -1:
//                            comHolder.join.setText("已参与");
//                            break;
//                        case 0:
//                            comHolder.join.setText("已参与");
//                            break;
                        case 1:
                            comHolder.join.setText("结束");
                            break;
                        case 2:
                            comHolder.join.setText("结束");
                            break;
                        case -3:
                            comHolder.join.setText("参与任务人数已达上限");
                            break;
                        default:
                            break;
                    }
                    break;
                }

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
             ScrollView scrollView;
        }
        class CompletedHolder{
            ImageView pic;
            TextView time;
            Button join;
        }
    }

}
