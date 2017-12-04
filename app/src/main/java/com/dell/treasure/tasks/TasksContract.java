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

import com.dell.treasure.BasePresenter;
import com.dell.treasure.BaseView;
import com.dell.treasure.dao.Task;

import java.util.List;

/**
 * This specifies the contract between the view and the presenter.
 */
public interface TasksContract {

    interface View extends BaseView<Presenter> {

        void setLoadingIndicator(boolean active);

        void showTasks(List<Task> tasks);

//        void showAddTask();

        void showTaskDetailsUi(Task taskFlag);

        void showTaskMarkedComplete();

//        void showTaskMarkedActive();

        void showCompletedTasksCleared();

        void showLoadingTasksError();

        void showActiveFilterLabel();

        void showCompletedFilterLabel();

        void showAllFilterLabel();

        void showNoTasks();

        void showNoActiveTasks();

        void showNoCompletedTasks();

//        void showSuccessfullySavedMessage();

        boolean isActive();

        void showFilteringPopUpMenu();
    }

    interface Presenter extends BasePresenter {

//        void result(int requestCode, int resultCode);

        void loadTasks(boolean forceUpdate);


//        void addNewTask();

        void openTaskDetails(@NonNull Task taskFlag);

        void completeTask(@NonNull Task completedTask);

//        void activateTask(@NonNull Task activeTask);

        void clearCompletedTasks();

        TasksFilterType getFiltering();

        void setFiltering(TasksFilterType requestType);
    }
}
