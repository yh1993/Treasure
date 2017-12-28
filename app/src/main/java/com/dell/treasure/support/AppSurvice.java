package com.dell.treasure.support;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.dell.treasure.R;
import com.dell.treasure.tasks.TaskDetails;

import java.util.List;

import static com.dell.treasure.support.NotificationHelper.sendDefaultNotice;

/**
 * Created by DELL on 2017/7/19.
 */

public class AppSurvice {

    /**
     * 判断应用是否已启动
     * @param context
     * @param packageName  要判断的应用包名
     * @return boolean
     */
    public static boolean isAppAlive(Context context, String packageName){
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();

        for (int i = 0; i < processInfos.size(); i++) {
            if(processInfos.get(i).processName.equals(packageName))
                return true;
        }
        return false;
    }

    //app 是否存活
    public static void isSurvive(Context context) {
        boolean appState = isAppAlive(context,"com.dell.treasure");
        PendingIntent pi = null;
        if(appState){
            Intent taskIntent = new Intent(context,TaskDetails.class);
//            CurrentUser.getOnlyUser().setTasKind("1");
            pi = PendingIntent.getActivity(context,0,taskIntent,PendingIntent.FLAG_CANCEL_CURRENT);
        }else{
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.dell.treasure");
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            launchIntent.putExtra("tasKind","1");
            pi = PendingIntent.getActivity(context,0,launchIntent,PendingIntent.FLAG_CANCEL_CURRENT);
        }
        sendDefaultNotice(context,"任务","收到任务，点击进入程序查看详情。", R.mipmap.ic_launcher,pi);
    }
}
