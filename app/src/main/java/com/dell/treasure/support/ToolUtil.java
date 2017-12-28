package com.dell.treasure.support;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by DELL on 2016/8/29.
 */
public class ToolUtil {

//    将int型放入2个字节的byte[]数组中   0 低8位  1 高8位
    public static byte[] toByteArray(String s){
        byte[] localArr = new byte[2];
        int source = 0;
        if(!s.isEmpty()) {
            source = Integer.parseInt(s);
        }
        for (int i = 0; i < 2; i++) {
            localArr[i] = (byte) (source >> (8 * i) & 0xFF);
        }
        return localArr;
    }

    //    将long型放入8个字节的byte[]数组中   0 低8位
    public static byte[] toByteArray(long s){
        byte[] localArr = new byte[8];
        for (int i = 0; i < 8; i++) {
            localArr[i] = (byte) (s >> (8 * i) & 0xFF);
        }
        return localArr;
    }

    public static byte[] getLongByte(byte[] refArr){
        byte[] localArr = new byte[8];
        if(refArr.length >= 10) {
            for (int i = 0; i < 8; i++) {
                localArr[i] = refArr[i + 2];
            }
        }
        return localArr;
    }
    //    把byte[]数组，两个字节转换成一个int型,n 控制转换的起始位置
    public static int getInt(byte[] refArr,int n){
        int outcome = 0;
        byte loop;
        if(refArr.length >= 12) {
            for (int i = n; i < n+2; i++) {
                loop = refArr[i];
                outcome += (loop & 0xFF) << (8 * i);
            }
        }
        return outcome;
    }

    //    把byte[]数组，八个字节转换成一个long型
    public static long getLong(byte[] refArr){
        long outcome = 0;
        byte loop;
        if(refArr.length >= 10) {
            for (int i = 2; i < 10; i++) {
                // 注意此处和byte数组转换成int的区别在于，下面的转换中要将先将数组中的元素转换成long型再做移位操作，
                // 若直接做位移操作将得不到正确结果，因为Java默认操作数字时，若不加声明会将数字作为int型来对待，此处必须注意。
                loop = refArr[i];
                outcome += (long)(loop & 0xFF) << (8 * (i-2));
            }
        }
        return outcome;
    }

//    把byte[]数组，每两个字节转换成一个int型
    public static int[] toInt(byte[] refArr){
        int outcome[] = new int[9];
        byte loop;
        for (int i = 0; i < refArr.length; i+=2) {
            for(int j=0;j<2;j++) {
                loop = refArr[i+j];
                outcome[i/2] += (loop & 0xFF) << (8 * j);
            }
        }
        return outcome;
    }

//    转换成上传服务器所需的格式
    public static String upLink(int[] outcome,int size){
        String s = "";
        for (int i = 0; i < size; i++) {
            s += outcome[i];
            s += "#";
        }
        return s;
    }

//    合并两个byte[]数组
    public static byte[] byteMerger(byte [] byte_1,byte [] byte_2){
        byte [] byte_3 = new byte[byte_1.length+byte_2.length];
        System.arraycopy(byte_1,0,byte_3,0,byte_1.length);
        System.arraycopy(byte_2,0,byte_3,byte_1.length,byte_2.length);
        return byte_3;
    }

    public static String toMyString(byte [] bytes){
        String tmp = "";
        for (byte aByte : bytes) {
            char ch = (char) (aByte & 0xFF);
            tmp += ch;
        }
        return tmp;
    }

    public static String dateToString(Date date){
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        return sd.format(date);
    }

    public static Date stringToDate(String s){
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        Date date = null;
        try {
            date = sd.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date stringToDate1(String s){
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        Date date = null;
        try {
            date = sd.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static void setAlarm(Context context, long triggerAtMillis, int matchId, Intent intent){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getService(context,matchId,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP,triggerAtMillis,pendingIntent);
    }
}

