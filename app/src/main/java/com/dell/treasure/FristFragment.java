package com.dell.treasure;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dell.treasure.dao.DaoSession;
import com.dell.treasure.dao.Task;
import com.dell.treasure.dao.TaskDao;
import com.dell.treasure.service.AlarmService;
import com.dell.treasure.support.MyApp;

import org.greenrobot.greendao.query.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DELL on 2017/1/5.
 */

public class FristFragment extends ListFragment {
    private TaskDao taskDao;
    private Query<Task> taskQuery;
    private List<Task> tasks = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApp myApp = (MyApp) getActivity().getApplication();
        DaoSession daoSession = myApp.getDaoSession();
        taskDao = daoSession.getTaskDao();
//        taskQuery = taskDao.queryBuilder().orderAsc(TaskDao.Properties.Id).build();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText(getString(R.string.empty_list));
    }

    @Override
    public void onResume() {
        super.onResume();
        taskQuery = taskDao.queryBuilder().orderAsc(TaskDao.Properties.Id).build();
        tasks = taskQuery.list();
        DataBaseAdapter adapter = new DataBaseAdapter(getActivity());
        setListAdapter(adapter);
    }

    class DataBaseAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        DataBaseAdapter(Context context){
            this.mInflater = LayoutInflater.from(context);
        }
        @Override
        public int getCount() {
            return tasks.size();
        }
        @Override
        public Object getItem(int arg0) {
            return tasks.get(arg0);
        }
        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        final class ViewHolder{
            ImageView dataPic;
            TextView dataName;
            TextView dataTime;
            TextView dataSumit;
            RelativeLayout dataLayout;
            ImageView dataUpload;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder= new ViewHolder();
                convertView = mInflater.inflate(R.layout.myhelp_listdata, parent,false);
                holder.dataPic = (ImageView)convertView.findViewById(R.id.dataImg);
                holder.dataTime = (TextView)convertView.findViewById(R.id.dataTime);
                holder.dataName = (TextView)convertView.findViewById(R.id.dataName);
                holder.dataSumit = (TextView)convertView.findViewById(R.id.dataSubmit);
                holder.dataLayout = (RelativeLayout)convertView.findViewById(R.id.dataRelativeLayout);
                holder.dataUpload = (ImageView)convertView.findViewById(R.id.upload);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder)convertView.getTag();
            }

            holder.dataName.setText("任务 "+tasks.get(position).getId());
            holder.dataTime.setText(tasks.get(position).getEndTime());
            if(tasks.get(position).getFlag() == 2){
                holder.dataSumit.setText("已上报");
                holder.dataUpload.setVisibility(View.GONE);
            }else if(tasks.get(position).getFlag() == 1) {
                holder.dataSumit.setText("未上报");
                holder.dataUpload.setVisibility(View.VISIBLE);
            }else if(tasks.get(position).getFlag() == 0){
                holder.dataSumit.setText("进行中");
                holder.dataUpload.setVisibility(View.GONE);
                holder.dataSumit.setTextColor(Color.parseColor("#33B5E5"));
            }
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                convertView.setBackgroundColor(getResources().getColor(android.R.color.background_light));
            }else {
                convertView.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.background_light));
            }
            final int p = position;
            holder.dataLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(tasks.get(p).getFlag() == 0){
//                        Intent mainIntent = new Intent(getActivity(), TraceActivity.class);
//                        getActivity().startActivity(mainIntent);
                    }
                    if(tasks.get(p).getFlag() == 1){
                        Intent intent = new Intent(getActivity(), AlarmService.class);
                        Toast.makeText(getActivity(),"任务上报中，请稍后。。。",Toast.LENGTH_SHORT).show();
                        getActivity().startService(intent);
                    }else if(tasks.get(p).getFlag() == 2){
                        Toast.makeText(getActivity(),"任务已经上报，请勿重复提交。",Toast.LENGTH_SHORT).show();
                    }
                }
            });
            return convertView;
        }
    }
}
