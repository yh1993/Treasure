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

import android.content.Context;
import android.support.annotation.NonNull;

import com.dell.treasure.dao.DaoSession;
import com.dell.treasure.dao.Task;
import com.dell.treasure.dao.TaskDao;
import com.dell.treasure.source.TasksDataSource;
import com.dell.treasure.support.MyApp;

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
    public void getTask(@NonNull Long taskId, @NonNull GetTaskCallback callback) {
        Task task = null;

        task = taskDao.load(taskId);

        if (task != null) {
            callback.onTaskLoaded(task);
        } else {
            callback.onDataNotAvailable();
        }
    }

    @Override
    public void saveTask(@NonNull Task task) {
        taskDao.insert(task);
    }

    @Override
    public void completeTask(@NonNull Task task) {
        task.setFlag(2);
        taskDao.update(task);
    }

    @Override
    public void completeTask(@NonNull Long taskId) {

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
    public void deleteTask(@NonNull Long taskId) {
        taskDao.deleteByKey(taskId);
    }
}
