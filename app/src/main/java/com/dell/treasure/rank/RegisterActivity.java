package com.dell.treasure.rank;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
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
import com.dell.treasure.SignInActivity;
import com.dell.treasure.share.BaseActivity;
import com.dell.treasure.support.CurrentUser;
import com.dell.treasure.support.NetUtil;

import net.sf.json.JSONArray;

import org.ksoap2.SoapFault;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yh on 2017/11/19.
 */

public class RegisterActivity extends BaseActivity {
    private static final String TAG = "RegisterActivity";
    private RecyclerView mRecyclerView;

    private ArrayList<RegisterItem> mRegisterItems;
    private RegisterRankAdapter adapter = null;
    private ProgressDialog mProgressDialog;
    private int flag = 0;
    private String userName;
//    private SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register_rank);
        userName = CurrentUser.getOnlyUser().getUsername();
        mRegisterItems = new ArrayList<RegisterItem>();
        mRecyclerView=(RecyclerView) findViewById(R.id.recylerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);

//        sp = this.getSharedPreferences(SignInActivity.USER_INFO, Context.MODE_PRIVATE);
//        CurrentUser.getOnlyUser().setCurrentState("002");
//        SharedPreferences.Editor editor = sp.edit();

//        editor.putString(SignInActivity.CURRENT_STATE, "002");
//        editor.apply();
        new getRegisterRankTask().execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private class getRegisterRankTask extends AsyncTask<Void ,Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(RegisterActivity.this);
            mProgressDialog.setMessage("刷新中..");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();
        }


        @Override
        protected Void doInBackground(Void... params) {
            String json = null;
            try {
                json = NetUtil.signBoard();
            } catch (SoapFault | NullPointerException soapFault) {
                soapFault.printStackTrace();
            }
            if (json == null){
                flag = 1;
            }else {
                mRegisterItems.clear();
                /**
                 * 这个json 解析的太傻逼了，浪费这么多时间，感觉自己像个智障
                 */
                JSONArray user_json = JSONArray.fromObject(json);
                List<Map<String,Integer>> list = JSONArray.toList(user_json,Map.class);

                int size = list.size();
                for(int i = 0; i< size;i++){
                    Map<String,Integer> item = list.get(i);
                    String name = ""+item.get("key");
                    int num = item.get("value").intValue();

                    RegisterItem mReItem = new RegisterItem(name,num);
                    mRegisterItems.add(mReItem);

                    Log.d(TAG, "doInBackground: "+" "+num + item.get("key"));
                }


//                Gson gson = new Gson();
//                List<Map.Entry<String, Integer>> retMap = gson.fromJson(json,
//                        new TypeToken<List<Map.Entry<String, Integer>>>() {}.getType());
//
//
//                for (Map.Entry<String, Integer> registerItem: retMap) {
//                    Log.d(TAG, "doInBackground: "+"key:" + registerItem.getKey() + " values:" + registerItem.getValue());
//                }

//                for (Map.Entry<String, Integer> registerItem: list) {
//                    Log.d(TAG, "doInBackground: "+"key:" + registerItem.getKey() + " values:" + registerItem.getValue());
//                }
//                Iterator it = list.iterator();
//                while(it.hasNext()){
//                    HashMap<String,Integer> tmp = new HashMap<>();
//                    Log.d("result", "Register doInBackground: "+ it.next().);
////                    RegisterItem item = (RegisterItem)it.next();
////                    if(item.getKey().equals(userName)){
////                        mRegisterItems.add(item);
////                        break;
////                    }
//                }
//                mRegisterItems.addAll(list);



//                List<RegisterItem> list = (List<RegisterItem>) JSONArray.toCollection(user_json, RegisterItem.class);
//                Log.d("result", "Register doInBackground: "+ list.toArray().toString());
//                Iterator it = list.iterator();
//                while(it.hasNext()){
//                    RegisterItem item = (RegisterItem)it.next();
//                    if(item.getKey().equals(userName)){
//                        mRegisterItems.add(item);
//                        break;
//                    }
//                }
//                mRegisterItems.addAll(list);
//                Log.d("result", "Register doInBackground: "+ list.toArray().toString() +" "+ mRegisterItems.toArray().toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(flag == 1){
                Toast.makeText(RegisterActivity.this,"数据获取失败，请尝试重新进入该界面",Toast.LENGTH_LONG).show();
            }else {
                RegisterActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter = new RegisterRankAdapter(RegisterActivity.this, mRegisterItems);
                        mRecyclerView.setAdapter(adapter);
                    }
                });
            }
            mProgressDialog.dismiss();
        }
    }

    class RegisterRankAdapter extends RecyclerView.Adapter<RegisterRankAdapter.MyViewHolder>{

        private Context context;
        private List<RegisterItem> list;

        public RegisterRankAdapter(Context context, List<RegisterItem> list){
            this.context = context;
            this.list = list;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new MyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_register_rank_item, viewGroup,false));
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final int i = position;
            MyViewHolder myViewHolder = (MyViewHolder) holder;
            if(i%2!=0){
                myViewHolder.getRoot().setBackgroundColor(Color.argb(255,223,223,223));
            }
            String netName= mRegisterItems.get(i).getKey();
            myViewHolder.getNameText().setText(netName);
            if(netName.equals(userName)){
                myViewHolder.getNameText().setBackgroundColor(Color.argb(255,0,150,255));
                myViewHolder.getNameText().setTextColor(Color.WHITE);
            }else{
                myViewHolder.getNameText().setBackgroundColor(Color.TRANSPARENT);
                myViewHolder.getNameText().setTextColor(Color.BLACK);
            }

            // setText() 直接放入整形，会出现资源找不到错误
            myViewHolder.getNumberText().setText(""+mRegisterItems.get(i).getValue());
//            myViewHolder.getScoreText().setText(score == 0 ? "--" : score +  "");
//            myViewHolder.getTotalText().setText("" + Float.parseFloat(new DecimalFormat("0.##").format(mRegisterItems.get(i).getTotal()) ));
        }

        @Override
        public int getItemCount() {
            return (null != list ? list.size() : 0);
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            private View mRoot;
            private TextView mNameText;
            private TextView mNumberText;
//            private TextView mScoreText;
//            private TextView mTotalText;


            public MyViewHolder(View root) {
                super(root);
                mRoot = root;
                mNameText = (TextView) root.findViewById(R.id.item_name_text);
                mNumberText = (TextView) root.findViewById(R.id.item_number_text);
//                mScoreText = (TextView) root.findViewById(R.id.item_score_text);
//                mTotalText = (TextView) root.findViewById(R.id.item_total_text);
            }




            public TextView getNameText() {
                return mNameText;
            }

            public TextView getNumberText() {
                return mNumberText;
            }

            public View getRoot() {
                return mRoot;
            }

//            public TextView getScoreText() {
//                return mScoreText;
//            }
//
//            public TextView getTotalText() {
//                return mTotalText;
//            }
        }
    }
}
