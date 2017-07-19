package com.dell.treasure.publisher;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.dell.treasure.R;
import com.dell.treasure.SignInActivity;
import com.dell.treasure.support.CurrentUser;
import com.dell.treasure.support.NetUtil;

import org.ksoap2.SoapFault;

import java.lang.ref.WeakReference;

/**
 * Created by DELL on 2016/5/14.
 * 将绑定的蓝牙数据，保存本地SharedPreferences文件中和上传服务器
 * 跳转主界面
 */
public class Ble_record extends Activity {
    public static final String BIND_NAME = "bindName";
    public static final String TAG_BLE = "targetBle";
    private EditText name;
    private ProgressDialog pDialog;

    private String typeFlag;  //丢失标志
    private String ble_id;    //任务ble

    private String username;
    private MyHandler myHandler = new MyHandler(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.ble_bind_record);

        Button save = (Button) findViewById(R.id.save_button);
        save.setOnClickListener(new saveOnClickListener());

        name = (EditText)findViewById(R.id.name);

        ImageView back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(new backOnClickListener());

        Spinner typeInfo = (Spinner) findViewById(R.id.typeInfo);
        typeInfo.setOnItemSelectedListener(new OnItemSelectedListenerInfo());

        CurrentUser user = CurrentUser.getOnlyUser();
        username = user.getUsername();
        ble_id = user.getTarget_ble();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
            pDialog = null;
        }
        myHandler.removeCallbacksAndMessages(null);
    }

    private class OnItemSelectedListenerInfo implements AdapterView.OnItemSelectedListener{
        //下拉选择
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if(position == 0){
                typeFlag = "1";
            }else
                typeFlag = "2";
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    private class saveOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            SubmitTask asyncTask = new SubmitTask();
            asyncTask.execute();
        }
    }
    private class backOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Ble_record.this,DeviceScanActivity.class);
            startActivity(intent);
            finish();
        }
    }


    private class SubmitTask extends AsyncTask<String, Integer, String> {
        private String bindName = name.getText().toString();

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Ble_record.this);
            pDialog.setMessage("绑定中..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
        protected String doInBackground(String... args) {
            String json = null;
            try {
                json = NetUtil.boundEqu(username,typeFlag,ble_id);
            } catch (SoapFault | NullPointerException soapFault) {
                soapFault.printStackTrace();
            }
            Message msg = myHandler.obtainMessage();
            if (json == null){
                msg.what = 0x38;
            }else {
                switch (json) {
                    case "1": {
                        msg.what = 0x34;
                        SharedPreferences sp = getSharedPreferences(SignInActivity.USER_INFO, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString(BIND_NAME, bindName);
                        editor.putString(TAG_BLE, ble_id);
                        editor.apply();
                        break;
                    }
                    case "2": {
                        msg.what = 0x35;
                        break;
                    }
                    case "3":{
                        msg.what = 0x37;
                        break;
                    }
                    default: {
                        msg.what = 0x36;
                        msg.obj = json;
                        break;
                    }
                }
            }
            myHandler.sendMessage(msg);
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            if(pDialog != null) {
                pDialog.dismiss();
                pDialog = null;
            }
        }
    }
    private static class MyHandler extends Handler{
        private WeakReference<Context> reference;
        MyHandler(Context context){
            reference = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            Ble_record activity = (Ble_record) reference.get();
            if(activity != null) {
                switch (msg.what) {
                    case 0x34:
                        Toast.makeText(activity, "绑定成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(activity, MainActivity.class);
                        activity.startActivity(intent);
                        activity.finish();
                        break;
                    case 0x35:
                        Toast.makeText(activity, "该蓝牙已经被绑定", Toast.LENGTH_SHORT).show();
                        break;
                    case 0x36:
                        Toast.makeText(activity, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;
                    case 0x37:
                        Toast.makeText(activity, "绑定失败", Toast.LENGTH_SHORT).show();
                        break;
                    case 0x38:
                        Toast.makeText(activity, "无法连接服务器", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        }
    }

}