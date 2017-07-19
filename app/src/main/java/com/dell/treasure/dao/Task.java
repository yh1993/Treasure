package com.dell.treasure.dao;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;

/**
 * Created by DELL on 2017/1/4.
 */
@Entity
public class Task implements Parcelable{
    @Id(autoincrement = true)
    private Long id;

    private String lastId;
    @Unique
    private String taskId;
    private String targetBle;
    private String beginTime;
    private String endTime;
//    private boolean isFinish;
//    private boolean isSubmit;
    private int flag; //-1 未参与 0 参与中，未结束  1 结束，未提交  2 提交  3 过期作废
    @Generated(hash = 960743891)
    public Task(Long id, String lastId, String taskId, String targetBle,
            String beginTime, String endTime, int flag) {
        this.id = id;
        this.lastId = lastId;
        this.taskId = taskId;
        this.targetBle = targetBle;
        this.beginTime = beginTime;
        this.endTime = endTime;
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
        beginTime = in.readString();
        endTime = in.readString();
        flag = in.readInt();
//        isFinish = in.readByte() != 0;
//        isSubmit = in.readByte() != 0;
    }

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            Task task = new Task();
            task.id = in.readLong();
            task.lastId = in.readString();
            task.taskId = in.readString();
            task.targetBle = in.readString();
            task.beginTime = in.readString();
            task.endTime = in.readString();
            task.flag = in.readInt();
//            task.isFinish = (in.readByte() != 0);
//            task.isSubmit = (in.readByte() != 0);
            return task;
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

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
    public String getBeginTime() {
        return this.beginTime;
    }
    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }
    public String getEndTime() {
        return this.endTime;
    }
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    public int getFlag(){
        return this.flag;
    }
    public void setFlag(int flag) {
        this.flag = flag;
    }
    //    public boolean getIsFinish() {
//        return this.isFinish;
//    }
//    public void setIsFinish(boolean isFinish) {
//        this.isFinish = isFinish;
//    }
//    public boolean getIsSubmit() {
//        return this.isSubmit;
//    }
//    public void setIsSubmit(boolean isSubmit) {
//        this.isSubmit = isSubmit;
//    }

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
        parcel.writeString(beginTime);
        parcel.writeString(endTime);
        parcel.writeInt(flag);
//        parcel.writeByte((byte) (isFinish ? 1 : 0));
//        parcel.writeByte((byte) (isSubmit ? 1 : 0));
    }
}
