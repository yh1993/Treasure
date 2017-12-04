package com.dell.treasure.rank;

/**
 * Created by yh on 2017/11/20.
 */

public class TaskRankItem {
//    private int mRank;
    private String mUserName;
    private String mtime;
    private String mlength;
    private String mfind;
//    private String mMoney;
    private String mRegisNum;
    private String mReward;

    public TaskRankItem(String userName, String time, String length,String find,String regisNum,String reward){
        mUserName= userName;
        mtime = time;
        mlength = length;
        mfind = find;
//        mMoney = money;
        mRegisNum = regisNum;
        mReward = reward;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String mUserName) {
        this.mUserName = mUserName;
    }

    public String gettime() {
        return mtime;
    }

    public void settime(String mtime) {
        this.mtime = mtime;
    }

    public String getlength() {
        return mlength;
    }

    public void setlength(String mlength) {
        this.mlength = mlength;
    }

    public String getfind() {
        return mfind;
    }

    public void setfind(String mfind) {
        this.mfind = mfind;
    }

    public String getRegisNum() {
        return mRegisNum;
    }

    public void setRegisNum(String mRegisNum) {
        this.mRegisNum = mRegisNum;
    }

    public String getReward() {
        return mReward;
    }

    public void setReward(String mReward) {
        this.mReward = mReward;
    }

//    public String getmMoney() {
//        return mMoney;
//    }
//
//    public void setmMoney(String mMoney) {
//        this.mMoney = mMoney;
//    }

    @Override
    public String toString() {
        return "username "+ mUserName +" time "+ mtime +" length " + mlength +" num "+ mRegisNum +" reward "+ mReward;
    }
}
