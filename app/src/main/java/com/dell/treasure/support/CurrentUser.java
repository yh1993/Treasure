package com.dell.treasure.support;

/**
 * Created by DELL on 2017/7/7.
 */

public class CurrentUser {
    private static CurrentUser sUser = new CurrentUser();

    private String username = null;
    private String userId;
    private String lastId;       //上线的id
    private String taskId ="";       //任务Id
    private String target_ble ="";   //目标蓝牙Mac
    private String startTime = "";    //任务开始时间
    private String beginTime = "";    //接受任务并开始的时间
    private String endTime = "";      //任务结束
    private String distance ;
    private String tasKind = "0";
    private String currentState = "005";  // 000 招募状态 001 招募结束  002 等待任务 003 任务进行中 004 任务结束
    private boolean isNetConn = false;
    private String needNum = "";
    private String currentNum = "";
    private String currentLevel = "";
    private boolean isJoin = false;
    private String taskIdTmp;
    private String fromUserId;

    private CurrentUser(){}

    public static CurrentUser getOnlyUser(){
        return sUser;
    }

    public void clear(){
        this.lastId = "";
        this.taskId = "";
        this.target_ble = "";
        this.startTime = "";
        this.beginTime = "";
        this.endTime = "";
        this.distance = "";
        this.tasKind = "0";
        this.currentState = "002";
        this.isNetConn = false;
        this.needNum = "";
        this.currentNum = "";
        this.currentLevel = "";
        this.isJoin = false;

    }
    public String getUsername() { return username; }

    public void setUsername(String name) { this.username = name; }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLastId() {
        return lastId;
    }

    public void setLastId(String lastId) { this.lastId = lastId; }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTarget_ble() {
        return target_ble;
    }

    public void setTarget_ble(String target_ble) {
        this.target_ble = target_ble;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public boolean isNetConn() {
        return isNetConn;
    }

    public void setNetConn(boolean isNetConn){
        this.isNetConn = isNetConn;
    }

    public String getTasKind() {
        return tasKind;
    }

    public void setTasKind(String tasKind) {
        this.tasKind = tasKind;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    public String getNeedNum() {
        return needNum;
    }

    public void setNeedNum(String needNum) {
        this.needNum = needNum;
    }

    public String getCurrentNum() {
        return currentNum;
    }

    public void setCurrentNum(String currentNum) {
        this.currentNum = currentNum;
    }

    public String getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(String currentLevel) {
        this.currentLevel = currentLevel;
    }

    public boolean isJoin() {
        return isJoin;
    }

    public void setJoin(boolean join) {
        isJoin = join;
    }

    public String getTaskIdTmp() {
        return taskIdTmp;
    }

    public void setTaskIdTmp(String taskIdTmp) {
        this.taskIdTmp = taskIdTmp;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }
}
