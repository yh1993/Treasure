/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dell.treasure.source.local;

import android.support.annotation.NonNull;

import com.dell.treasure.dao.DaoSession;
import com.dell.treasure.dao.Task;
import com.dell.treasure.dao.TaskDao;
import com.dell.treasure.source.TasksDataSource;
import com.dell.treasure.support.MyApp;
import com.orhanobut.logger.Logger;

import org.greenrobot.greendao.query.Query;

import java.util.ArrayList;
import java.util.List;


/**
 * Concrete implementation of a data source as a db.
 */
public class TasksLocalDataSource implements TasksDataSource {

    private static TasksLocalDataSource INSTANCE;

    private DaoSession daoSession;
    private TaskDao taskDao;

    // Prevent direct instantiation.
    private TasksLocalDataSource() {
        daoSession = MyApp.getInstance().getDaoSession();
        taskDao = daoSession.getTaskDao();
    }

    public static TasksLocalDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TasksLocalDataSource();
        }
        return INSTANCE;
    }

    /**
     * Note: {@link LoadTasksCallback#onDataNotAvailable()} is fired if the database doesn't exist
     * or the table is empty.
     */
    @Override
    public List<Task> init(){
        List<Task> tasks = new ArrayList<>();
        return tasks = taskDao.queryBuilder().orderAsc(TaskDao.Properties.Id).list();
    }
    @Override
    public void getTasks(@NonNull LoadTasksCallback callback) {
        List<Task> tasks = new ArrayList<>();

        tasks = taskDao.queryBuilder().orderAsc(TaskDao.Properties.Id).list();

        if (tasks.isEmpty()) {
            // This will be called if the table is new or just empty.
            callback.onDataNotAvailable();
        } else {
            callback.onTasksLoaded(tasks);
        }

    }

    /**
     * Note: {@link GetTaskCallback#onDataNotAvailable()} is fired if the {@link Task} isn't
     * found.
     */
    @Override
    public void getTask(@NonNull Long Id, @NonNull GetTaskCallback callback) {
        Task task = null;

        task = taskDao.load(Id);

        if (task != null) {
            callback.onTaskLoaded(task);
        } else {
            callback.onDataNotAvailable();
        }
    }

    @Override
    public Task getTask(String taskId) {
        Query<Task> taskQueryCur = taskDao.queryBuilder().where(TaskDao.Properties.TaskId.eq(taskId)).build();
        List<Task> tasksCur = taskQueryCur.list();
        if(tasksCur.size() > 0){
            return tasksCur.get(0);
        }
        return null;
    }

    @Override
    public void saveTask(@NonNull Task task) {
        taskDao.insert(task);
    }

    @Override
    public void completeTask(@NonNull Task task) {
        Logger.d(task.toString());
        if(isTaskExist(task.getTaskId())) {
            taskDao.update(task);
        }
    }

    @Override
    public void completeTask(@NonNull Long Id) {

    }

    @Override
    public void updateTask(@NonNull Task task) {
        taskDao.update(task);
    }

    @Override
    public boolean isTaskExist(@NonNull String taskId) {
        Query<Task> taskQueryCur = taskDao.queryBuilder().where(TaskDao.Properties.TaskId.eq(taskId)).build();

        List<Task> tasksCur = taskQueryCur.list();
        for (Task task2:tasksCur) {
            Logger.d(task2.toString());
        }

        if(tasksCur != null && tasksCur.size() > 0){
            return true;
        }
        return false;
    }

//    @Override
//    public void activateTask(@NonNull Task task) {
//        SQLiteDatabase db = mDbHelper.getWritableDatabase();
//
//        ContentValues values = new ContentValues();
//        values.put(TaskEntry.COLUMN_NAME_COMPLETED, false);
//
//        String selection = TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
//        String[] selectionArgs = { task.getId() };
//
//        db.update(TaskEntry.TABLE_NAME, values, selection, selectionArgs);
//
//        db.close();
//    }
//
//    @Override
//    public void activateTask(@NonNull String taskId) {
//        // Not required for the local data source because the {@link TasksRepository} handles
//        // converting from a {@code taskId} to a {@link task} using its cached data.
//    }

    @Override
    public void clearCompletedTasks() {
        List<Task> tasks = new ArrayList<>();
        tasks = taskDao.queryBuilder().where(TaskDao.Properties.Flag.eq(2)).list();
        if (!tasks.isEmpty()) {
            for(Task task:tasks){
                taskDao.delete(task);
            }
        }
    }

    @Override
    public void refreshTasks() {

    }

    @Override
    public void deleteAllTasks() {
        taskDao.deleteAll();
    }

    @Override
    public void deleteTask(@NonNull Long Id) {
        taskDao.deleteByKey(Id);
    }
}
