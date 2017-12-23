package com.dell.treasure.publisher;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dell.treasure.support.CurrentUser;
import com.dell.treasure.support.MyApp;
import com.dell.treasure.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DELL on 2016/5/14.
 * 扫描activity，扫描ble设备，跳过进入主界面
 * 对扫描到的ble进行点击，进入设备绑定界面Ble_record
 */
public class DeviceScanActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 5 seconds.
    private static final long SCAN_PERIOD = 5000;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanCallback mScanCallback;
    private ScanResultAdapter mAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private ListView listView;
//    private MyApp myApp;
    private CurrentUser user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        myApp = MyApp.getInstance();
        user = CurrentUser.getOnlyUser();

        setContentView(R.layout.device_scan);
        listView = (ListView)findViewById(R.id.device_list);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        // 标题的文字需在setSupportActionBar之前，不然会无效
        mToolbar.setTitle("设备发现");
        mToolbar.inflateMenu(R.menu.main);//设置右上角的填充菜单
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_scan:
                        scanLeDevice(true);
                        break;
                    case R.id.menu_stop:
                        scanLeDevice(false);
                        break;
                    case R.id.menu_skip:
                        scanLeDevice(false);
                        Intent intent = new Intent(DeviceScanActivity.this,MainActivity.class);
                        startActivity(intent);
                }
                return true;
            }
        });

        mHandler = new Handler();

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.bt_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        // 初始化 listview adapter.
        mScanning = false;
        mAdapter = new ScanResultAdapter();
        listView.setAdapter(mAdapter);

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            scanLeDevice(true);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        scanLeDevice(true);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothAdapter = null;
    }
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            startScanning();
        } else {
            stopScanning();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        scanLeDevice(false);
        mHandler.removeCallbacksAndMessages(null);
        mScanCallback = null;
        mAdapter.clear();
        mAdapter = null;
    }

    private void startScanning() {
        if (mScanCallback == null) {
            mScanCallback = new SampleScanCallback();
        }
        if(!mScanning) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScanning();
                }
            }, SCAN_PERIOD);
            mScanning = true;
            mAdapter.clear();
            mBluetoothLeScanner.startScan(buildScanFilters(), buildScanSettings(), mScanCallback);
        }else {
            Toast.makeText(DeviceScanActivity.this, R.string.already_scanning, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Stop scanning for BLE Advertisements.
     */
    private void stopScanning() {
        mScanning = false;
        mBluetoothLeScanner.stopScan(mScanCallback);

        //通过一个外部的方法控制如果适配器的内容改变时需要强制调用getView来刷新每个Item的内容。
        mAdapter.notifyDataSetChanged();//重绘当前可见区域
    }

    /**
     * Return a List of {@link ScanFilter} objects to filter by Service UUID.
     */
    private List<ScanFilter> buildScanFilters() {
        List<ScanFilter> scanFilters = new ArrayList<>();

        ScanFilter.Builder builder = new ScanFilter.Builder();
        // Comment out the below line to see all BLE devices around you
//        builder.setServiceUuid(Constants.Service_UUID);
        scanFilters.add(builder.build());

        return scanFilters;
    }
    /**
     * Return a {@link ScanSettings} object set to use low power (to preserve battery life).
     */
    private ScanSettings buildScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        return builder.build();
    }
    /**
     * Custom ScanCallback object - adds to adapter on success, displays error on failure.
     */
    private class SampleScanCallback extends ScanCallback {
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult result : results) {
                mAdapter.add(result);
            }
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            mAdapter.add(result);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    }

    public class ScanResultAdapter extends BaseAdapter {

        private ArrayList<ScanResult> mArrayList;
        private LayoutInflater mInflater;

        ScanResultAdapter() {
            super();
            mArrayList = new ArrayList<>();
            mInflater = DeviceScanActivity.this.getLayoutInflater();
        }
        /**
         * Clear out the adapter.
         */
        void clear() {
            mArrayList.clear();
        }
        @Override
        public int getCount() {
            return mArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return mArrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mArrayList.get(position).getDevice().getAddress().hashCode();
        }
        @Override
        public View getView(int position, View view, ViewGroup parent) {
            ViewHolder holder;
            // Reuse an old view if we can, otherwise create a new one.
            if (view == null) {
                holder=new ViewHolder();
                view = mInflater.inflate(R.layout.ble_list, parent,false);
                holder.devicePic = (ImageView) view.findViewById(R.id.dataImg);
                holder.deviceAddressView = (TextView)view.findViewById(R.id.address);
                holder.deviceNameView = (TextView)view.findViewById(R.id.name);
                holder.deviceRssi = (TextView)view.findViewById(R.id.type);
                holder.deviceLayout = (RelativeLayout)view.findViewById(R.id.dataRelativeLayout);
                view.setTag(holder);
            }else{
                holder = (ViewHolder)view.getTag();
            }

            final ScanResult scanResult = mArrayList.get(position);
            if(scanResult != null){
                String name = scanResult.getDevice().getName();
                if (name == null) {
                    name = "no name";
                }
                holder.deviceNameView.setText(name);
                holder.deviceAddressView.setText(scanResult.getDevice().getAddress());
                holder.deviceRssi.setText(String.valueOf(scanResult.getRssi()));
                holder.deviceLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(DeviceScanActivity.this, Ble_record.class);
                        user.setTarget_ble(scanResult.getDevice().getAddress());
                        if (mScanning) {
                            stopScanning();
                        }
                        startActivity(intent);
                    }
                });
            }
            return view;
        }

        /**
         * Search the adapter for an existing device address and return it, otherwise return -1.
         */
        private int getPosition(String address) {
            int position = -1;
            for (int i = 0; i < mArrayList.size(); i++) {
                if (mArrayList.get(i).getDevice().getAddress().equals(address)) {
                    position = i;
                    break;
                }
            }
            return position;
        }

        /**
         * Add a ScanResult item to the adapter if a result from that device isn't already present.
         * Otherwise updates the existing position with the new ScanResult.
         */
        void add(ScanResult scanResult) {
            int existingPosition = getPosition(scanResult.getDevice().getAddress());

            if (existingPosition >= 0) {
                // Device is already in list, update its tasks_act.
                mArrayList.set(existingPosition, scanResult);
            } else {
                // Add new Device's ScanResult to list.
                mArrayList.add(scanResult);
            }
        }
        final class ViewHolder{
            ImageView devicePic;
            TextView deviceNameView;
            TextView deviceAddressView;
            TextView deviceRssi;
            RelativeLayout deviceLayout;
        }
    }
}
