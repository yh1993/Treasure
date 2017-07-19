package com.dell.treasure.support;

/**
 * Created by DELL on 2017/7/7.
 */

public class CurrentUser {
    private static CurrentUser sUser = new CurrentUser();

    private String username;
    private String userId;
    private String lastId;       //上线的id
    private String taskId ="";       //任务Id
    private String target_ble ="";   //目标蓝牙Mac
    private String startTime = "";    //任务开始时间
    private String beginTime = "";    //接受任务并开始的时间
    private String endTime = "";      //任务结束
    private String distance ;
    private String tasKind = "0";
    public boolean isNetConn = true;


    private CurrentUser(){}

    public static CurrentUser getOnlyUser(){
        return sUser;
    }

    public String getUsername() { return username; }

    public String getUserId() {
        return userId;
    }

    public String getLastId() {
        return lastId;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getTarget_ble() {
        return target_ble;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getDistance() {
        return distance;
    }

    public String getStartTime() {
        return startTime;
    }

    public boolean isNetConn() {
        return isNetConn;
    }

    public void setUsername(String name) { this.username = name; }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTarget_ble(String target_ble) {
        this.target_ble = target_ble;
    }

    public void setLastId(String lastId) { this.lastId = lastId; }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public void setNetConn(boolean isNetConn){
        this.isNetConn = isNetConn;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setTasKind(String tasKind) {
        this.tasKind = tasKind;
    }

    public String getTasKind() {
        return tasKind;
    }
}
