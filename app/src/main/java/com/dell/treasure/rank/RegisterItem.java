package com.dell.treasure.rank;

/**
 * Created by yh on 2017/11/20.
 * 招募Rank
 */

public class RegisterItem {
//    private int mRank;
    private String key;
    private int value;
//    private double mTotal;

    public RegisterItem(String userName, int number) {
        key = userName;
        value = number;
    }

//    public RegisterItem(int rank, String userName, int cnt, double money) {
//        mRank = rank;
//        mUserName = userName;
//        mCnt = cnt;
//        mTotal = money;
//    }

//    public int getRank() {
//        return mRank;
//    }
//
//    public void setRank(int rank) {
//        mRank = rank;
//    }

//    public String getUserName() {
//        return mUserName;
//    }
//
//    public void setUserName(String userName) {
//        mUserName = userName;
//    }
//
//    public int getNumber() {
//        return mNumber;
//    }
//
//    public void setNumber(int number) {
//        this.mNumber = number;
//    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

//    public double getTotal() {
//        return mTotal;
//    }
//
//    public void setTotal(double total) {
//        mTotal = total;
//    }
}
