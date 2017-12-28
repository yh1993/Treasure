package com.dell.treasure.dao;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Unique;

/**
 * Created by DELL on 2017/1/4.
 */
@Entity
public class Task implements Parcelable{
    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            Task task = new Task();
            task.id = in.readLong();
            task.lastId = in.readString();
            task.taskId = in.readString();
            task.targetBle = in.readString();
//            task.beginTime = in.readString();
//            task.endTime = in.readString();
            task.startTime = in.readString();
            task.beginTime = in.readString();
            task.length = in.readDouble();
            task.distance = in.readString();
            task.needNum = in.readString();
            task.currentNum = in.readString();
            task.currentLevel = in.readString();
            task.money = in.readString();
            task.flag = in.readInt();

            return task;
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };
//    private String beginTime;
//    private String endTime;
    @Transient
    private static Task instance = new Task();
    // 虽然不是单例，但也可以用来标记当前任务
    @Id(autoincrement = true)
    private Long id;
    private String lastId;  // 上线用户id  0 服务器  id 其他用户
    @Unique
    private String taskId;  // 当前任务id
    private String targetBle;
    private String startTime; //任务开始时间（服务器开始），非是接收到任务的时间
    private String beginTime; //参与任务的开始时间
    private double length;    //已参与任务时长
    private String distance;  //已参与任务距离
    private String needNum;   //需要多少人参与
    private String currentNum;   //第几个参与
    private String currentLevel; //第几层参与
    private String money; //先保存总钱数，任务结束时，改为用户本次任务所得
    private int flag; //-3 参与人数已够，不能参与 -2 未参与 -1 参与中 0 参与中，开广播  1 结束 2 过期
    @Generated(hash = 251642779)
    public Task(Long id, String lastId, String taskId, String targetBle, String startTime, String beginTime, double length,
            String distance, String needNum, String currentNum, String currentLevel, String money, int flag) {
        this.id = id;
        this.lastId = lastId;
        this.taskId = taskId;
        this.targetBle = targetBle;
        this.startTime = startTime;
        this.beginTime = beginTime;
        this.length = length;
        this.distance = distance;
        this.needNum = needNum;
        this.currentNum = currentNum;
        this.currentLevel = currentLevel;
        this.money = money;
        this.flag = flag;
    }
    @Generated(hash = 733837707)
    public Task() {
    }

    protected Task(Parcel in) {
        id = in.readLong();
        lastId = in.readString();
        taskId = in.readString();
        targetBle = in.readString();
//        beginTime = in.readString();
//        endTime = in.readString();
        startTime = in.readString();
        beginTime = in.readString();
        length = in.readDouble();
        distance = in.readString();
        needNum = in.readString();
        currentNum = in.readString();
        currentLevel = in.readString();
        money = in.readString();
        flag = in.readInt();

    }

    public static Task getInstance(){
        return instance;
    }

    public void setTask(Task task){
        this.id = task.id;
        this.lastId = task.lastId;
        this.taskId = task.taskId;
        this.targetBle = task.targetBle;
//        this.beginTime = task.beginTime;
//        this.endTime = task.endTime;
        this.startTime = task.startTime;
        this.beginTime = task.beginTime;
        this.length = task.length;
        this.distance = task.distance;
        this.needNum = task.needNum;
        this.currentNum = task.currentNum;
        this.currentLevel = task.currentLevel;
        this.money = task.money;
        this.flag = task.flag;
    }

    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getLastId() {
        return this.lastId;
    }
    public void setLastId(String lastId) {
        this.lastId = lastId;
    }
    public String getTaskId() {
        return this.taskId;
    }
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
    public String getTargetBle() {
        return this.targetBle;
    }
    public void setTargetBle(String targetBle) {
        this.targetBle = targetBle;
    }
//    public String getBeginTime() {
//        return this.beginTime;
//    }
//    public void setBeginTime(String beginTime) {
//        this.beginTime = beginTime;
//    }
//    public String getEndTime() {
//        return this.endTime;
//    }
//    public void setEndTime(String endTime) {
//        this.endTime = endTime;
//    }
    public int getFlag(){
        return this.flag;
    }
    public void setFlag(int flag) {
        this.flag = flag;
    }
    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
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

    public String getMoney(){
        return money;
    }
    public void setMoney(String money) {
        this.money = money;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        // 序列化过程：必须按成员变量声明的顺序进行封装
        parcel.writeLong(id);
        parcel.writeString(lastId);
        parcel.writeString(taskId);
        parcel.writeString(targetBle);
//        parcel.writeString(beginTime);
//        parcel.writeString(endTime);
        parcel.writeString(startTime);
        parcel.writeString(beginTime);
        parcel.writeDouble(length);
        parcel.writeString(distance);
        parcel.writeString(needNum);
        parcel.writeString(currentNum);
        parcel.writeString(currentLevel);
        parcel.writeString(money);
        parcel.writeInt(flag);

    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    @Override
    public String toString() {
        return "id: "+id+" taskId: "+taskId+" lastId: "+lastId+" ble: "+targetBle+" start: "+startTime
                +" begin: "+beginTime+" length: "+length+" distance: "+distance+" needNum: "+needNum
                +" currentNum: "+currentNum+" currentLevel: "+currentLevel+" money: "+money+" flag: "+flag;
    }
}
