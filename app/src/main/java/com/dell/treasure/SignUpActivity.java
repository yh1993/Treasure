package com.dell.treasure;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.dell.treasure.support.NetUtil;

import org.ksoap2.SoapFault;

import java.lang.ref.WeakReference;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;


/**
 * Created by hp on 2016/3/17 0017.
 * 注册
 */
public class SignUpActivity extends Activity{
    public static final String TAG = "SignUpActivity";
    private EditText username;
    private EditText password;
    private EditText alipay;
    private EditText invitation;
    private ProgressDialog pDialog;

    private TextInputLayout userTextInput,passwordTextInput,editAlipay,invitationInput;
    private String Sname,Spassword,SeditAlipay,Sinvitation;
    private MyHandler myHandler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_signup);
        Button buttonSignUp = (Button) findViewById(R.id.buttonSignUp);
        buttonSignUp.setOnClickListener(new SignUpClickListener());
        Button buttonCancel = (Button) findViewById(R.id.buttonSignUpCancel);
        buttonCancel.setOnClickListener(new CancelClickListener());
        userTextInput = (TextInputLayout) findViewById(R.id.userTextInput);
        passwordTextInput = (TextInputLayout) findViewById(R.id.passwordTextInput);
        editAlipay = (TextInputLayout) findViewById(R.id.alipay);
//        invitationInput = (TextInputLayout) findViewById(R.id.invitationTextInput);
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

    private void checkLogin() {
        editAlipay.setErrorEnabled(false);
        userTextInput.setErrorEnabled(false);
        passwordTextInput.setErrorEnabled(false);
//        invitationInput.setErrorEnabled(false);

        SeditAlipay = alipay.getText().toString();
        Sname = username.getText().toString();
        Spassword = password.getText().toString();
//        Sinvitation = invitation.getText().toString();

        if(TextUtils.isEmpty(SeditAlipay)){
            editAlipay.setError("请输入支付宝账号，用于之后发放奖励");
            return;
        }
        if (TextUtils.isEmpty(Sname)) {
            userTextInput.setError("请输入用户名");
            return;
        }
        if (TextUtils.isEmpty(Spassword)) {
            passwordTextInput.setError("请输入密码");
            return;
        }
        new SignUpTask().execute();
    }

    private static class MyHandler extends Handler{
        private WeakReference<Context> reference;
        MyHandler(Context context){
            reference = new WeakReference<>(context);
        }
        @Override
        public void handleMessage(Message msg) {
            SignUpActivity activity = (SignUpActivity) reference.get();
            if(activity != null) {
                switch (msg.what) {
                    case 0x34:
                        Toast.makeText(activity, "注册成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(activity,SignInActivity.class);
                        activity.startActivity(intent);
                        activity.finish();
                        break;
                    case 0x35:
                        Toast.makeText(activity, "该用户名已存在！", Toast.LENGTH_SHORT).show();
                        break;
                    case 0x36:
                        Toast.makeText(activity, "注册失败", Toast.LENGTH_SHORT).show();
                        break;
                    case 0x37:
                        Toast.makeText(activity, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;
                    case 0x38:
                        Toast.makeText(activity, "无法连接服务器", Toast.LENGTH_SHORT).show();
                        break;
                    case 0x39:
                        Toast.makeText(activity,"非常抱歉，系统注册阶段已经结束，任务期间已停止用户注册",Toast.LENGTH_LONG).show();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private class SignUpClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            username = (EditText)findViewById(R.id.editTextSignUpUser);
            password = (EditText)findViewById(R.id.editTextSignUpPaw);
            alipay = (EditText) findViewById(R.id.editAlipay);
//            invitation = (EditText) findViewById(R.id.editTextInvitation);
            checkLogin();
        }
    }

    private class CancelClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            finish();
        }
    }

    private class SignUpTask extends AsyncTask<Void, Void, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SignUpActivity.this);
            pDialog.setMessage("注册中...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            Boolean isSign = false;
            try {
                isSign = NetUtil.isSignPeriod();
            } catch (SoapFault soapFault) {
                soapFault.printStackTrace();
            }
            Message msg = myHandler.obtainMessage();
            if(!isSign){
                msg.what = 0x39;
                myHandler.sendMessage(msg);
            }else {
                String json = null;
                try {
                    json = NetUtil.signUp(Sname, Spassword, SeditAlipay);
                } catch (SoapFault | NullPointerException soapFault) {
                    soapFault.printStackTrace();
                }
                if (json == null) {
                    msg.what = 0x38;
                } else {
                    switch (json) {
                        case "1": {
                            JPushInterface.setAlias(SignUpActivity.this, Sname, new TagAliasCallback() {
                                @Override
                                public void gotResult(int i, String s, Set<String> set) {
                                    String logs;
                                    switch (i) {
                                        case 0:
                                            logs = "Set tag and alias success，Alias is " + Sname;
                                            Log.d(TAG, "Result: "+logs);
                                            // 建议这里往 SharePreference 里写一个成功设置的状态。成功设置一次后，以后不必再次设置了。
                                            break;
                                        case 6002:
                                            logs = "Failed to set alias and tags due to timeout. ";
                                            Log.d(TAG, "Result: "+logs);
                                            // 延迟 60 秒来调用 Handler 设置别名
                                            break;
                                        default:
                                            logs = "Failed with errorCode = " + i;
                                            Log.d(TAG, "Result: "+logs);
                                    }
                                }
                            });
                            msg.what = 0x34;
                            break;
                        }
                        case "2": {
                            msg.what = 0x35;
                            break;
                        }
                        case "3": {
                            msg.what = 0x36;
                            break;
                        }
                        default: {
                            msg.what = 0x37;
                            msg.obj = json;
                            break;
                        }
                    }
                }
                myHandler.sendMessage(msg);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(pDialog != null) {
                pDialog.dismiss();
                pDialog = null;
            }
        }
    }
}
