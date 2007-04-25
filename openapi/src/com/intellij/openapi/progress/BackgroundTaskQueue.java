/*
 * Copyright 2000-2007 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.openapi.progress;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author yole
 */
public class BackgroundTaskQueue {
  private final Queue<Task> myQueue = new LinkedList<Task>();
  private boolean myHasActiveTask = false;
  private Task.Backgroundable myRunnerTask;

  public BackgroundTaskQueue(Project project, String title) {
    myRunnerTask = new Task.Backgroundable(project, title) {
      public void run(final ProgressIndicator indicator) {
        myHasActiveTask = true;
        while(true) {
          final Task task;
          synchronized(myQueue) {
            task = myQueue.poll();
            if (task == null) {
              break;
            }
          }
          indicator.setText(task.getTitle());
          task.run(indicator);
          ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
              task.onSuccess();
            }
          }, ModalityState.NON_MODAL);
        }
        myHasActiveTask = false;
      }
    };
  }

  public void run(Task.Backgroundable task) {
    if (task.isConditionalModal() && !task.shouldStartInBackground()) {
      ProgressManager.getInstance().run(task);
    }
    else {
      synchronized(myQueue) {
        myQueue.offer(task);
        if (!myHasActiveTask) {
          ProgressManager.getInstance().run(myRunnerTask);
        }
      }
    }
  }
}
