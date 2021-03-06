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

package com.dell.treasure.tasks;

import android.support.annotation.NonNull;

import com.dell.treasure.dao.Task;
import com.dell.treasure.source.TasksDataSource;
import com.dell.treasure.source.TasksRepository;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;


/**
 * Listens to user actions from the UI ({@link TasksFragment}), retrieves the data and updates the
 * UI as required.
 */
public class TasksPresenter implements TasksContract.Presenter {

    private final TasksRepository mTasksRepository;
    private final TasksContract.View mTasksView;
    public TasksFilterType mCurrentFiltering = TasksFilterType.ACTIVE_TASKS;
    private boolean mFirstLoad = true;


    public TasksPresenter(@NonNull TasksRepository tasksRepository, @NonNull TasksContract.View tasksView) {
        mTasksRepository = tasksRepository;
        mTasksView = tasksView;
        mTasksView.setPresenter(this);
    }

    @Override
    public void start() {
        loadTasks(false);
    }

//    @Override
//    public void result(int requestCode, int resultCode) {
//        // If a task was successfully added, show snackbar
//        if (AddEditTaskActivity.REQUEST_ADD_TASK == requestCode && Activity.RESULT_OK == resultCode) {
//            mTasksView.showSuccessfullySavedMessage();
//        }
//    }

    @Override
    public void loadTasks(boolean forceUpdate) {
        // Simplification for sample: a network reload will be forced on first load.
        loadTasks(forceUpdate || mFirstLoad, true);
        mFirstLoad = false;
    }


    private void loadTasks(boolean forceUpdate, final boolean showLoadingUI) {
        if (showLoadingUI) {
            mTasksView.setLoadingIndicator(true);
        }
        if (forceUpdate) {
            mTasksRepository.refreshTasks();
        }

        mTasksRepository.getTasks(new TasksDataSource.LoadTasksCallback() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                List<Task> tasksToShow = new ArrayList<Task>();
                // We filter the tasks based on the requestType
                for (Task task : tasks) {
                    Logger.d(task.toString());
                    switch (mCurrentFiltering) {
//                        case ALL_TASKS:
//                            if (task.getFlag() > -3) {
//                                Logger.d(task.getTaskId()+" "+task.getFlag());
//                                tasksToShow.add(task);
//                            }
//                            break;
                        case ACTIVE_TASKS:
                            if (task.getFlag() < 1 && task.getFlag() >= -2) {
                                tasksToShow.add(task);
                            }
                            break;
                        case COMPLETED_TASKS:
                            if (task.getFlag() == 2) {
                                tasksToShow.add(task);
                            }
                            break;
                        default:
                           // tasksToShow.add(task);
                            break;
                    }
                }
                // The view may not be able to handle UI updates anymore
                if (!mTasksView.isActive()) {
                    return;
                }
                if (showLoadingUI) {
                    mTasksView.setLoadingIndicator(false);
                }
                processTasks(tasksToShow);
            }

            @Override
            public void onDataNotAvailable() {
                // The view may not be able to handle UI updates anymore
                if (!mTasksView.isActive()) {
                    return;
                }
//                mTasksView.showLoadingTasksError();
                if (showLoadingUI) {
                    mTasksView.setLoadingIndicator(false);
                }
            }
        });
    }

    private void processTasks(List<Task> tasks) {
        if (tasks.isEmpty()) {
            // Show a message indicating there are no tasks for that filter type.
            processEmptyTasks();
        } else {
            // Show the list of tasks
            mTasksView.showTasks(tasks,mCurrentFiltering);
            // Set the filter label's text.
            showFilterLabel();
        }
    }

    private void showFilterLabel() {
        switch (mCurrentFiltering) {
            case ACTIVE_TASKS:
                mTasksView.showActiveFilterLabel();
                break;
            case COMPLETED_TASKS:
                mTasksView.showCompletedFilterLabel();
                break;
            default:
                mTasksView.showAllFilterLabel();
                break;
        }
    }

    private void processEmptyTasks() {
        switch (mCurrentFiltering) {
            case ACTIVE_TASKS:
                mTasksView.showNoActiveTasks();
                break;
            case COMPLETED_TASKS:
                mTasksView.showNoCompletedTasks();
                break;
            default:
                mTasksView.showNoTasks();
                break;
        }
    }


//    @Override
//    public void addNewTask() {
////        mTasksView.showAddTask();
//    }

    @Override
    public void openTaskDetails(@NonNull Task taskFlag) {
        mTasksView.showTaskDetailsUi(taskFlag);
    }

    @Override
    public void completeTask(@NonNull Task completedTask) {
        mTasksRepository.completeTask(completedTask);
        mTasksView.showTaskMarkedComplete();
        loadTasks(false, false);
    }

//    @Override
//    public void activateTask(@NonNull Task activeTask) {
//        checkNotNull(activeTask, "activeTask cannot be null!");
//        mTasksRepository.activateTask(activeTask);
//        mTasksView.showTaskMarkedActive();
//        loadTasks(false, false);
//    }

    @Override
    public void clearCompletedTasks() {
        mTasksRepository.clearCompletedTasks();
        mTasksView.showCompletedTasksCleared();
        loadTasks(false, false);
    }

    @Override
    public TasksFilterType getFiltering() {
        return mCurrentFiltering;
    }

    /**
     * Sets the current task filtering type.
     *
     * @param requestType Can be {@link TasksFilterType#ALL_TASKS},
     *                    {@link TasksFilterType#COMPLETED_TASKS}, or
     *                    {@link TasksFilterType#ACTIVE_TASKS}
     */
    @Override
    public void setFiltering(TasksFilterType requestType) {
        mCurrentFiltering = requestType;
    }

}
