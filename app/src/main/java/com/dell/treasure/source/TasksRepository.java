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

package com.dell.treasure.source;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dell.treasure.dao.Task;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;



/**
 * Concrete implementation to load tasks from the data sources into a cache.
 * <p>
 * For simplicity, this implements a dumb synchronisation between locally persisted data and data
 * obtained from the server, by using the remote data source only if the local database doesn't
 * exist or is empty.
 */
public class TasksRepository implements TasksDataSource {

    private static TasksRepository INSTANCE = null;

    private final TasksDataSource mTasksLocalDataSource;

    /**
     * This variable has package local visibility so it can be accessed from tests.
     */
    Map<Long, Task> mCachedTasks = new LinkedHashMap<>();

    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This variable
     * has package local visibility so it can be accessed from tests.
     */
    boolean mCacheIsDirty = false;

    // Prevent direct instantiation.
    private TasksRepository(@NonNull TasksDataSource tasksLocalDataSource) {
        mTasksLocalDataSource = tasksLocalDataSource;
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @param tasksLocalDataSource  the device storage data source
     * @return the {@link TasksRepository} instance
     */
    public static TasksRepository getInstance(TasksDataSource tasksLocalDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new TasksRepository(tasksLocalDataSource);
            INSTANCE.refreshCache(tasksLocalDataSource.init());

        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    @Override
    public List<Task> init() {
        refreshCache(mTasksLocalDataSource.init());
        return null;
    }

    /**
     * Gets tasks from cache, local data source (SQLite) or remote data source, whichever is
     * available first.
     * <p>
     * Note: {@link LoadTasksCallback#onDataNotAvailable()} is fired if all data sources fail to
     * get the data.
     */
    @Override
    public void getTasks(@NonNull final LoadTasksCallback callback) {

        // Respond immediately with cache if available and not dirty
        if (mCachedTasks != null && !mCacheIsDirty) {
            callback.onTasksLoaded(new ArrayList<>(mCachedTasks.values()));
            return;
        }


        // Query the local storage if available. If not, query the network.
        mTasksLocalDataSource.getTasks(new LoadTasksCallback() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                refreshCache(tasks);
                callback.onTasksLoaded(new ArrayList<>(mCachedTasks.values()));
            }

            @Override
            public void onDataNotAvailable() {
                callback.onDataNotAvailable();
            }
        });
    }

    @Override
    public void saveTask(@NonNull Task task) {
        mTasksLocalDataSource.saveTask(task);

        // Do in memory cache update to keep the app UI up to date
        if (mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
        }
        Task task1 = new Task();
        task1.setTask(task);
        mCachedTasks.put(task.getId(), task1);

    }

    @Override
    public void completeTask(@NonNull Task task) {

        mTasksLocalDataSource.completeTask(task);

        // Do in memory cache update to keep the app UI up to date
        if (mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
        }
        Task task1 = new Task();
        task1.setTask(task);
        mCachedTasks.put(task.getId(), task1);
        for(Task task2: mCachedTasks.values()){
            Logger.d(" task: "+task2.toString());
        }
    }

    @Override
    public void completeTask(@NonNull Long Id) {
        completeTask(getTaskWithId(Id));
    }

    @Override
    public void updateTask(@NonNull Task task) {
        mTasksLocalDataSource.updateTask(task);
        if (mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
        }
        Task task1 = new Task();
        task1.setTask(task);
        mCachedTasks.put(task.getId(), task1);
    }

    @Override
    public boolean isTaskExist(@NonNull String taskId) {
        if (mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
            return mTasksLocalDataSource.isTaskExist(taskId);
        }else {
            ArrayList<Task> arrayList = new ArrayList<>(mCachedTasks.values());
            for(Task task1:arrayList){
                if(task1.getTaskId().equals(taskId)){
                    return true;
                }
            }
            return false;
        }
    }

//    @Override
//    public void activateTask(@NonNull Task task) {
//        checkNotNull(task);
//        mTasksRemoteDataSource.activateTask(task);
//        mTasksLocalDataSource.activateTask(task);
//
//        Task activeTask = new Task(task.getTitle(), task.getDescription(), task.getId());
//
//        // Do in memory cache update to keep the app UI up to date
//        if (mCachedTasks == null) {
//            mCachedTasks = new LinkedHashMap<>();
//        }
//        mCachedTasks.put(task.getId(), activeTask);
//    }
//
//    @Override
//    public void activateTask(@NonNull String taskId) {
//        checkNotNull(taskId);
//        activateTask(getTaskWithId(taskId));
//    }

    @Override
    public void clearCompletedTasks() {
        mTasksLocalDataSource.clearCompletedTasks();

        // Do in memory cache update to keep the app UI up to date
        if (mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
        }
        Iterator<Map.Entry<Long, Task>> it = mCachedTasks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, Task> entry = it.next();
            int key = entry.getValue().getFlag();
            if (key == 2 || key == 3) {
                it.remove();
            }
        }
    }

    /**
     * Gets tasks from local data source (sqlite) unless the table is new or empty. In that case it
     * uses the network data source. This is done to simplify the sample.
     * <p>
     * Note: {@link GetTaskCallback#onDataNotAvailable()} is fired if both data sources fail to
     * get the data.
     */
    @Override
    public void getTask(@NonNull final Long Id, @NonNull final GetTaskCallback callback) {

        Task cachedTask = getTaskWithId(Id);

        // Respond immediately with cache if available
        if (cachedTask != null) {
            callback.onTaskLoaded(cachedTask);
            return;
        }

        // Load from server/persisted if needed.

        // Is the task in the local data source? If not, query the network.
        mTasksLocalDataSource.getTask(Id, new GetTaskCallback() {
            @Override
            public void onTaskLoaded(Task task) {
                // Do in memory cache update to keep the app UI up to date
                if (mCachedTasks == null) {
                    mCachedTasks = new LinkedHashMap<>();
                }
                mCachedTasks.put(task.getId(), task);
                callback.onTaskLoaded(task);
            }

            @Override
            public void onDataNotAvailable() {
            }
        });
    }

    @Override
    public Task getTask(String taskId) {
        return mTasksLocalDataSource.getTask(taskId);
    }

    @Override
    public void refreshTasks() {
        mCacheIsDirty = true;
    }

    @Override
    public void deleteAllTasks() {
        mTasksLocalDataSource.deleteAllTasks();

        if (mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
        }
        mCachedTasks.clear();
    }

    @Override
    public void deleteTask(@NonNull Long taskId) {
        mTasksLocalDataSource.deleteTask(taskId);

        mCachedTasks.remove(taskId);
    }


    public void refreshCache(List<Task> tasks) {
        if (mCachedTasks == null) {
            mCachedTasks = new LinkedHashMap<>();
        }
        mCachedTasks.clear();
        for (Task task : tasks) {
            mCachedTasks.put(task.getId(), task);
        }
        mCacheIsDirty = false;
    }

//    private void refreshLocalDataSource(List<Task> tasks) {
//        mTasksLocalDataSource.deleteAllTasks();
//        for (Task task : tasks) {
//            mTasksLocalDataSource.saveTask(task);
//        }
//    }

    @Nullable
    private Task getTaskWithId(@NonNull Long id) {
        if (mCachedTasks == null || mCachedTasks.isEmpty()) {
            return null;
        } else {
            return mCachedTasks.get(id);
        }
    }
}
