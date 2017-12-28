package com.dell.treasure.rank;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.dell.treasure.R;
import com.dell.treasure.service.UserInfo;
import com.dell.treasure.share.BaseActivity;
import com.dell.treasure.support.CurrentUser;
import com.dell.treasure.support.NetUtil;
import com.orhanobut.logger.Logger;

import net.sf.json.JSONArray;

import org.ksoap2.SoapFault;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yh on 2017/11/20.
 */

public class TaskRankActivity extends BaseActivity {
    private RecyclerView mRecyclerView;

    private ArrayList<TaskRankItem> mTaskItems;
    private TaskRankAdapter adapter = null;
    private ProgressDialog mProgressDialog;
    private int flag = 0;
    private TextView money_text;
    private String userId;
    private String userName;
    private String taskId;
    private String money;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fragment_main_rank);
        userName = CurrentUser.getOnlyUser().getUsername();

        userId = CurrentUser.getOnlyUser().getUserId();
        mTaskItems = new ArrayList<TaskRankItem>();
        mRecyclerView=(RecyclerView) findViewById(R.id.recylerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);

        money_text = (TextView) findViewById(R.id.money_text);

        taskId = getIntent().getStringExtra("TaskId");
        new getTaskRankTask().execute();
        startService(new Intent(this, UserInfo.class));
    }

    class getTaskRankTask extends AsyncTask<Void ,Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(TaskRankActivity.this);
            mProgressDialog.setMessage("刷新中..");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();
        }


        @Override
        protected Void doInBackground(Void... params) {
            String json = null;
            try {
                money = NetUtil.getTaskReward(userId,taskId);
            } catch (SoapFault | NullPointerException soapFault) {
                soapFault.printStackTrace();
            }
            try {
                json = NetUtil.TaskPartInfo(taskId);
            } catch (SoapFault | NullPointerException soapFault) {
                soapFault.printStackTrace();
            }
            if (json == null || money.isEmpty()){
                flag = 1;
            }else {
                mTaskItems.clear();
                JSONArray user_json = JSONArray.fromObject(json);
                List<ArrayList<String>> list = JSONArray.toList(user_json,ArrayList.class);
                int size = list.size();
                for(int i = 0; i< size;i++){
                    JSONArray user = JSONArray.fromObject(list.get(i));
                    List<String> item = JSONArray.toList(user,ArrayList.class);
                    String mUserName = item.get(0);
                    String mtime = item.get(1);
                    String mlength = item.get(2);
                    String mfind = item.get(3);
                    String mRegisNum = item.get(4);
                    String mReward = item.get(5);

                    TaskRankItem mTaskItem = new TaskRankItem(mUserName,mtime,mlength,mfind,mRegisNum,mReward);
                    Log.d("result", "task item: "+mTaskItem.toString());
                    mTaskItems.add(mTaskItem);

                }


//                JSONArray user_json = JSONArray.fromObject(json);
//                List list = (List) JSONArray.toCollection(user_json, TaskRankItem.class);
//                Iterator it = list.iterator();
//                while(it.hasNext()){
//                    TaskRankItem item = (TaskRankItem) it.next();
//                    if(item.getUserName().equals(userName)){
//                        mTaskItems.add(item);
//                        break;
//                    }
//                }
//                mTaskItems.addAll(list);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(flag == 1){
                Toast.makeText(TaskRankActivity.this,"数据获取失败，请尝试重新进入该界面",Toast.LENGTH_LONG).show();
            }else {
                money_text.setText(money);
                TaskRankActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter = new TaskRankAdapter(TaskRankActivity.this, mTaskItems);
                        mRecyclerView.setAdapter(adapter);
                    }
                });
            }
            mProgressDialog.dismiss();
        }
    }

    class TaskRankAdapter extends RecyclerView.Adapter<TaskRankAdapter.MyViewHolder>{

        private Context context;
        private List<TaskRankItem> list;

        public TaskRankAdapter(Context context, List<TaskRankItem> list){
            this.context = context;
            this.list = list;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new MyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_main_rank_item, viewGroup,false));
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final int i = position;
            MyViewHolder myViewHolder = (MyViewHolder) holder;
            if(i%2!=0){
                myViewHolder.getRoot().setBackgroundColor(Color.argb(255,223,223,223));
            }
            String netName= mTaskItems.get(i).getUserName();
            myViewHolder.getNameText().setText(netName);
            if(netName.equals(userName)){
                myViewHolder.getNameText().setBackgroundColor(Color.argb(255,0,150,255));
                myViewHolder.getNameText().setTextColor(Color.WHITE);
            }else{
                myViewHolder.getNameText().setBackgroundColor(Color.TRANSPARENT);
                myViewHolder.getNameText().setTextColor(Color.BLACK);
            }

            myViewHolder.getTimeText().setText(mTaskItems.get(i).gettime());
            myViewHolder.getLengthText().setText(mTaskItems.get(i).getlength());
            myViewHolder.getFindText().setText(mTaskItems.get(i).getfind());
            myViewHolder.getRegisNumText().setText(mTaskItems.get(i).getRegisNum());
            myViewHolder.getRewardText().setText(mTaskItems.get(i).getReward());
//            myViewHolder.getmMoneyText().setText(mTaskItems.get(i).getmMoney());
        }

        @Override
        public int getItemCount() {
            return (null != list ? list.size() : 0);
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            private View mRoot;
            private TextView mNameText;
            private TextView mTimeText;
            private TextView mLengthText;
            private TextView mFindText;
            private TextView mRegisNumText;
            private TextView mRewardText;
//            private TextView mMoneyText;


            public MyViewHolder(View root) {
                super(root);
                mRoot = root;
                mNameText = (TextView) root.findViewById(R.id.item_name_text);
                mTimeText = (TextView) root.findViewById(R.id.item_time_text);
                mLengthText = (TextView) root.findViewById(R.id.item_length_text);
                mFindText = (TextView) root.findViewById(R.id.item_find_text);
                mRegisNumText = (TextView)root.findViewById(R.id.item_regisNum_text);
                mRewardText = (TextView)root.findViewById(R.id.item_reward_text);
//                mMoneyText = (TextView) root.findViewById(R.id.item_money);
            }




            public TextView getNameText() {
                return mNameText;
            }

            public TextView getTimeText() {
                return mTimeText;
            }

            public TextView getLengthText() {
                return mLengthText;
            }

            public TextView getFindText() {
                return mFindText;
            }

            public View getRoot() {
                return mRoot;
            }

            public TextView getRegisNumText() {
                return mRegisNumText;
            }

            public TextView getRewardText() {
                return mRewardText;
            }

//            public TextView getmMoneyText() {return mMoneyText;}
        }
    }
}
