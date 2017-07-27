package com.dell.treasure;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.dell.treasure.publisher.DeviceScanActivity;
import com.dell.treasure.support.CurrentUser;
import com.dell.treasure.support.NetUtil;
import com.dell.treasure.tasks.TasksActivity;

import org.ksoap2.SoapFault;

import java.lang.ref.WeakReference;



/**
 * Created by hp on 2016/3/17 0017.
 * 登录
 * 默认记住密码和自动登录
 */
public class SignInActivity extends Activity{
    private EditText TextUsername;
    private EditText TextPassword;
//    private CheckBox checkBoxRemPSW;
//    private CheckBox checkBoxAutoSignIn;
    private ProgressDialog pDialog = null;

    public static final String USER_INFO = "password";
    public static final String REM_PSW = "remPsw";
    public static final String AUTO_SIGNIN = "autoSignIn";
    public static final String USERNAME = "username";
    public static final String PSW = "password";
    public static final String USERID = "userid";
    private SharedPreferences sp;

    private TextInputLayout userTextInput,passwordTextInput;
    private static String Sname;
    private String Spassword;

    private MyHandler myHandler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_signin);

        Button buttonSignIn = (Button) findViewById(R.id.buttonSignIn);
        buttonSignIn.setOnClickListener( new SignInClickListener());
        Button buttonSignUp = (Button) findViewById(R.id.buttonSignUp);
        buttonSignUp.setOnClickListener( new SignUpClickListener());
        Button mForgetPasswordButton = (Button) findViewById(R.id.login_forget_password_button);
        mForgetPasswordButton.setOnClickListener( new ForgetPasswordListener());
        TextUsername = (EditText) findViewById(R.id.editTextSignInUser);
        TextPassword = (EditText) findViewById(R.id.editTextSignInPsw);
//        checkBoxRemPSW = (CheckBox)findViewById(R.id.checkBoxRemPsw);
//        checkBoxAutoSignIn = (CheckBox)findViewById(R.id.checkBoxAutoSignIn);
        userTextInput = (TextInputLayout) findViewById(R.id.userTextInput);
        passwordTextInput = (TextInputLayout) findViewById(R.id.passwordTextInput);

//        checkBoxAutoSignIn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (checkBoxAutoSignIn.isChecked()){
//                    checkBoxRemPSW.setChecked(true);
//                }
//            }
//        });
//        checkBoxRemPSW.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (!checkBoxRemPSW.isChecked()){
//                    checkBoxAutoSignIn.setChecked(false);
//                    TextPassword.setText("");
//                }
//            }
//        });
        sp = this.getSharedPreferences(USER_INFO, Context.MODE_PRIVATE);
//        checkBoxRemPSW.setChecked(sp.getBoolean(REM_PSW,false));
        TextUsername.setText(sp.getString(USERNAME, ""));
        TextPassword.setText(sp.getString(PSW,""));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 判断是否支持ble
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
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

    private class SignUpClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(SignInActivity.this,SignUpActivity.class);
            SignInActivity.this.startActivity(intent);
        }
    }
    private class SignInClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            checkLogin();
        }
    }
    private class SignInTask extends AsyncTask<Void,Void,String>{
//        private boolean rem_Flag = checkBoxRemPSW.isChecked();
//        private boolean autoSignIn_Flag = checkBoxAutoSignIn.isChecked();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SignInActivity.this);
            pDialog.setMessage("登录中..");
            pDialog.setIndeterminate(false);//setIndeterminate(true)的意思就是不明确具体进度,进度条在最大值与最小值之间来回移动,形成一个动画效果
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String json = null;
            try {
                json = NetUtil.signIn(Sname,Spassword);

            } catch (SoapFault | NullPointerException soapFault) {
                soapFault.printStackTrace();
            }
            Message msg = myHandler.obtainMessage();
            if(json == null){
                msg.what = 0x38;
            }else{
                switch (json) {
                    case "n":
                    case "p": {
                        msg.what = 0x34;
                        break;
                    }
                    case "o": {
                        msg.what = 0x35;
                        break;
                    }
                    default: {
                        msg.what = 0x36;
                        CurrentUser user = CurrentUser.getOnlyUser();
                        user.setUsername(Sname);
                        user.setUserId(json);
                        SharedPreferences.Editor editor = sp.edit();

                        editor.putBoolean(REM_PSW, true);
                        editor.putString(USERNAME, Sname);
                        editor.putString(PSW, Spassword);
                        editor.putString(USERID, json);

                        editor.putBoolean(AUTO_SIGNIN, true);

                        editor.apply();
                        break;
                    }
                }
            }
            myHandler.sendMessage(msg);
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
    private static class MyHandler extends Handler{
        private WeakReference<Context> reference;
        MyHandler(Context context){
            reference = new WeakReference<>(context);
        }
        @Override
        public void handleMessage(Message msg) {
            SignInActivity activity = (SignInActivity) reference.get();
            if(activity != null){
                switch (msg.what){
                    case 0x34:
                        Toast.makeText(activity, "用户名密码错误", Toast.LENGTH_SHORT).show();
                        break;
                    case 0x35:
                        Toast.makeText(activity, "其他错误", Toast.LENGTH_SHORT).show();
                        break;
                    case 0x36:
                        Intent i;
                        if(Sname.equals("yh")){
                            i = new Intent(activity, DeviceScanActivity.class);
                        }else{
                            i = new Intent(activity, TasksActivity.class);
                        }
                        activity.startActivity(i);
                        activity.finish();
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

    private void checkLogin() {
        // Reset errors.
        userTextInput.setErrorEnabled(false);
        passwordTextInput.setErrorEnabled(false);

        Sname = TextUsername.getText().toString();
        Spassword = TextPassword.getText().toString();

        if (TextUtils.isEmpty(Sname)) {
            userTextInput.setError("请输入用户名");
            return;
        }
        if (TextUtils.isEmpty(Spassword)) {
            passwordTextInput.setError("请输入密码");
            return;
        }
        new SignInTask().execute();
    }

    private class ForgetPasswordListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SignInActivity.this);
            alertDialogBuilder.setTitle("忘记密码怎么办");
            alertDialogBuilder.setMessage("请编辑短信，格式为“支付宝账号_用户名，忘记密码”，发送到手机17603902065");
            alertDialogBuilder.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            alertDialogBuilder.create().show();
        }
    }
}
