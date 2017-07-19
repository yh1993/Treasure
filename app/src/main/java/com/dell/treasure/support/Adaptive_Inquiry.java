package com.dell.treasure.support;

import android.bluetooth.le.ScanResult;

import java.util.ArrayList;

/**
 * Created by DELL on 2016/10/8.
 */

public class Adaptive_Inquiry {
    private static int N = 5;        //发现邻居数量的阈值
    private static int I = 1;        //查询间隔的增量
    private static int base_W = 8;   //基础查询窗口
    private static int base_I = 10;  //基础查询间隔
    private static int MAX_RSP = 255;//最大的邻居发现数量
    private static int small_W = 5;  //最小的查询窗口
    private static int inc_NP = 10;  //没有邻居时的查询间隔增量
    private static int r = 0;        //随机变量
    private static double p = 0.8;   //随机变量r=0 的概率
    private static int peers = -1;
    private static int last_peers;
    private static int inquiry_window = base_W;
    private static int inquiry_interval = base_I;
    private static int totalSize = 0;

    private static ArrayList<ScanResult> mArrayList = new ArrayList<>(); //邻居集合
    private static ArrayList<ScanResult> total = new ArrayList<>();

    public static void getPeers(){
        peers = mArrayList.size();
        mArrayList.clear();
    }

    public static int getSize(){
        return total.size();

    }
    public static void getR(){
        r = Math.random() > 0.2d ? 0 : (Math.random() > 0.5d ? 1 : -1);
    }

    public static int getInquiry_window(){
        if(peers < 0){
            inquiry_window = base_W;
        }else if(peers > N){
            inquiry_window = base_W;
        }else{
            inquiry_window = small_W + r;
        }
        return inquiry_window;
    }

    public static int getInquiry_interval(){
        if(peers == 0 && last_peers == 0){
            inquiry_interval += inc_NP + r;
        }else if(peers != 0 && last_peers == 0){
            inquiry_interval = base_I + r;
        }else if(peers > last_peers){
            inquiry_interval -= I;
        }else if(peers < last_peers){
            inquiry_interval += I;
        }
        last_peers = peers;
        return inquiry_interval;
    }

    private static int getPosition(String address,ArrayList<ScanResult> list) {
        int position = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getDevice().getAddress().equals(address)) {
                position = i;
                break;
            }
        }
        return position;
    }

    public static void add(ScanResult scanResult) {

        int existingPosition = getPosition(scanResult.getDevice().getAddress(),mArrayList);

        if (existingPosition >= 0) {
            // Device is already in list, update its tasks_act.
            mArrayList.set(existingPosition, scanResult);
        } else {
            // Add new Device's ScanResult to list.
            mArrayList.add(scanResult);
        }

        int position = getPosition(scanResult.getDevice().getAddress(),total);
        if (position < 0) {
            total.add(scanResult);
        }
    }
}
