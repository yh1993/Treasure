package com.dell.treasure.publisher;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.dell.treasure.support.CurrentUser;
import com.dell.treasure.support.MyApp;
import com.dell.treasure.R;
import com.dell.treasure.SignInActivity;
import com.dell.treasure.service.LocationService;
import com.dell.treasure.support.NetUtil;

import org.ksoap2.SoapFault;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private SharedPreferences sp;
    private TextView bindName;
    private TextView tagBle;
    private TextView title;
    private ImageView back;    //返回

    private TableLayout tableLayout; //求助内容布局
    private EditText payment;
    private EditText time_lost;
    private EditText lost_place;

    private Button save;
    private Button help;
    private int helpFlag = 1;//求助、提交切换

    private ProgressDialog pDialog;

    //上报信息
    private String lost_time;
    private String address;
    private double lat;
    private double lon;
    private String pay;
    private String ble_id;

    public CurrentUser user;
    private String username;

    private LocationService locationService;
    private BDLocationListener myListener = new MyLocationListener();
    private MyHandler myHandler = new MyHandler(this);

    private Button game_over;
    private Button get_price;
    private Button exit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        title = (TextView)findViewById(R.id.title);
        back = (ImageView)findViewById(R.id.back);
        back.setOnClickListener(this);


        tableLayout = (TableLayout)findViewById(R.id.TableLayout);
        payment = (EditText)findViewById(R.id.pay_money);
        ImageView time = (ImageView) findViewById(R.id.time);
        time_lost = (EditText)findViewById(R.id.lost_time);
        time.setOnClickListener(this);
        ImageView loc_lost = (ImageView) findViewById(R.id.choose_lost_Place);
        loc_lost.setOnClickListener(this);
        lost_place = (EditText)findViewById(R.id.lostPlace);

        save = (Button)findViewById(R.id.save_button);
        save.setOnClickListener(this);
        help = (Button)findViewById(R.id.help_button);
        help.setOnClickListener(this);

        user = CurrentUser.getOnlyUser();
        username = user.getUsername();
        ble_id = user.getTarget_ble();

        game_over = (Button) findViewById(R.id.over_game);
        game_over.setOnClickListener(this);
        get_price = (Button) findViewById(R.id.get_price);
        get_price.setOnClickListener(this);
        exit = (Button) findViewById(R.id.exit);
        exit.setOnClickListener(this);
        sp = getSharedPreferences(SignInActivity.USER_INFO, Context.MODE_PRIVATE);
        bindName = (TextView) findViewById(R.id.name);
        tagBle = (TextView) findViewById(R.id.address);
        bindName.setText(sp.getString(Ble_record.BIND_NAME, null));
        tagBle.setText(sp.getString(Ble_record.TAG_BLE,null));
    }

    @Override
    protected void onStart() {
        super.onStart();
        //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
        locationService = MyApp.getInstance().locationService;
        locationService.setLocationOption(locationService.getDefaultLocationClientOption());
        //注册监听
        locationService.registerListener(myListener);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationService.unregisterListener(myListener); //注销掉监听
        locationService.stop();
        if(pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
            pDialog = null;
        }
        myHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.over_game:
                break;
            case R.id.get_price:
                break;
            case R.id.back:
                finish();
                break;
            case R.id.save_button:
                new OffBoundEqu().execute();
                break;
            case R.id.choose_lost_Place:
                locationService.start();// 定位SDK
                break;
            case R.id.exit:
                exit();
                break;
            case R.id.time:
                getTime();
                break;
            case R.id.help_button:
                sendHelp();
                break;

        }
    }

    private void exit() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("提示")
                .setMessage("确定退出？")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putBoolean(SignInActivity.AUTO_SIGNIN, false);
                        editor.apply();
                        Intent intent = new Intent(MainActivity.this,SignInActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    public class MyLocationListener implements BDLocationListener{
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if(bdLocation == null){
                return;
            }else{
                lat = bdLocation.getLatitude();
                lon = bdLocation.getLongitude();
                Log.d("result",lat+" "+lon );
                String address2 = bdLocation.getAddrStr();
                String address1 = bdLocation.getAddress().toString();

                address = bdLocation.getLocationDescribe();//位置语义化结果
                Log.d("result",address2+" "+address1 +" "+address);
                lost_place.setText(address);
            }
            locationService.stop();
        }
    }
    private void getTime(){
        View view = View.inflate(getApplicationContext(), R.layout.time_picker, null);
        final TimePicker timePicker = (TimePicker)view.findViewById(R.id.time_picker);

        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        timePicker.setIs24HourView(true);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            timePicker.setCurrentHour(hour);
            timePicker.setCurrentMinute(minute);
        }else{
            timePicker.setHour(hour);
            timePicker.setMinute(minute);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(view);
        builder.setTitle("选择时间");
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String timeStr ="";
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    if (timePicker.getCurrentHour() < 10) {
                        timeStr = "0";
                    }
                    timeStr += timePicker.getCurrentHour() + ":";
                    if (timePicker.getCurrentMinute() < 10) {
                        timeStr += "0";
                    }
                    timeStr += timePicker.getCurrentMinute();
                }else{
                    if(timePicker.getHour() < 10){
                        timeStr = "0";
                    }
                    timeStr += timePicker.getHour() + ":";
                    if (timePicker.getMinute() < 10) {
                        timeStr += "0";
                    }
                    timeStr += timePicker.getMinute();
                }
                time_lost.setText(timeStr);
            }
        });
        builder.show();
    }

    private void sendHelp(){
        tableLayout.setVisibility(View.VISIBLE);
        title.setText("我的求助");
//            back.setVisibility(View.INVISIBLE);
        if(helpFlag==1){
            helpFlag = 0;
            save.setVisibility(View.GONE);
            help.setText("提 交");
        }else {
            helpFlag = 1;
            pay = payment.getText().toString();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            String s = formatter.format(new Date());
            lost_time = s + " " + time_lost.getText().toString();

            if (!TextUtils.isEmpty(time_lost.getText()) && !TextUtils.isEmpty(lost_place.getText())) {
                SubmitTask asyncTask = new SubmitTask();
                asyncTask.execute();
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("请添加");
                sb.append(TextUtils.isEmpty(payment.getText()) ? " 给予酬劳" : "");
                sb.append(TextUtils.isEmpty(time_lost.getText()) ? " 丢失时间" : "");
                sb.append(TextUtils.isEmpty(lost_place.getText()) ? " 丢失地点" : "");
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(sb.toString())
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialoginterface, int i) {
                                        //按钮事件
                                    }
                                })
                        .show();
            }
        }
    }

    private class OffBoundEqu extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("解除绑定中..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        protected String doInBackground(String... args) {
            String json = null;
            try {
                json = NetUtil.OffBoundEqu(username,ble_id);
            } catch (SoapFault | NullPointerException soapFault) {
                soapFault.printStackTrace();
            }
            Message msg = myHandler.obtainMessage();
            if (json == null){
                msg.what = 0x38;
            }else {
                if(json.equals("t")){
                    msg.what = 0x39;
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString(Ble_record.BIND_NAME, null);
                    editor.putString(Ble_record.TAG_BLE, null);
                    editor.apply();
                }else if(json.equals("f")||json.equals("n")){
                    msg.what = 0x3A;
                }else {
                    msg.what = 0x3B;
                    msg.obj = json;
                }
            }
            myHandler.sendMessage(msg);
            return null;
        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            if(pDialog != null){
                pDialog.dismiss();
                pDialog = null;
            }
        }

    }

    private class SubmitTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("求助上报中..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        protected String doInBackground(String... args) {
            String json = null;
            try {
                json = NetUtil.ReleaseTask(username,ble_id,address,String.valueOf(lon),String.valueOf(lat),lost_time,pay);
            } catch (SoapFault | NullPointerException soapFault) {
                soapFault.printStackTrace();
            }
            Message msg = myHandler.obtainMessage();
            if (json == null){
                msg.what = 0x38;
            }else {
                switch (json) {
                    case "t": {
                        msg.what = 0x34;
                        break;
                    }
                    case "e": {
                        msg.what = 0x36;
                        break;
                    }
                    case "f": {
                        msg.what = 0x37;
                        break;
                    }
                    case "s": {
                        msg.what = 0x33;
                        break;
                    }
                    default: {
                        msg.what = 0x3B;
                        msg.obj = json;
                        break;
                    }
                }
            }
            myHandler.sendMessage(msg);
            return null;
        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            if(pDialog != null) {
                pDialog.dismiss();
                pDialog = null;
            }
        }

    }
    private static class MyHandler extends Handler {
        private WeakReference<Context> reference;
        MyHandler(Context context){
            reference = new WeakReference<>(context);
        }
        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = (MainActivity) reference.get();
            if(activity != null){
                switch (msg.what) {
                    case 0x33:
                        Toast.makeText(activity, "积分不足", Toast.LENGTH_SHORT).show();
                        break;
                    case 0x34:
                        Toast.makeText(activity, "求助成功", Toast.LENGTH_SHORT).show();
                        break;
                    case 0x36:
                        Toast.makeText(activity, "该任务已经存在", Toast.LENGTH_SHORT).show();
                        break;
                    case 0x37:
                        Toast.makeText(activity, "任务发布失败", Toast.LENGTH_SHORT).show();
                        break;
                    case 0x38:
                        Toast.makeText(activity, "无法连接服务器", Toast.LENGTH_SHORT).show();
                        break;
                    case 0x39:
                        Toast.makeText(activity, "解绑成功", Toast.LENGTH_SHORT).show();
                        activity.finish();
                        break;
                    case 0x3A:
                        Toast.makeText(activity, "解绑失败", Toast.LENGTH_SHORT).show();
                        break;
                    case 0x3B:
                        Toast.makeText(activity, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        }
    }

}
