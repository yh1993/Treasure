package com.dell.treasure.support;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by DELL on 2017/4/26.
 */

public class AppHelper {
    public static String mNewestAppVersion;

    public static String getAppVersion(Context context){
        try{
            PackageManager manager = context.getPackageManager();
            PackageInfo packageInfo = manager.getPackageInfo(context.getPackageName(),0);
            String version = packageInfo.versionName;
            return version;
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }
}
