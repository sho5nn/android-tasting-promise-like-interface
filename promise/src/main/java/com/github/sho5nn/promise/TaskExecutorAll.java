/*
 * MIT License
 *
 * Copyright (c) 2017 sho5nn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.sho5nn.promise;

import android.support.annotation.NonNull;

import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

class TaskExecutorAll extends TaskExecutor<Object[], Throwable[], PromiseTask.All> {

  @NonNull
  private LinkedList<Deferred<Object, Throwable>> deferredList = new LinkedList<>();
  @NonNull
  private CountDownLatch counter = new CountDownLatch(0);

  TaskExecutorAll(@NonNull Promise<Object[], Throwable[]> promise,
                  @NonNull PromiseTask.All task) {
    super(promise, task);
  }

  @Override
  public void execute() {
    if (promise.isFulfilled()) {
      promise.resolve(promise.resolvedValue());
      return;
    }

    if (promise.isRejected()) {
      promise.reject(promise.rejectedValue());
      return;
    }

    if (promise.isExecuting()) return;

    if (task.taskList.isEmpty()) throw new IllegalArgumentException("task is null");

    for (PromiseTask.Single single : task.taskList) {
      deferredList.add(new DeferredTrigger<Object, Throwable>(promise.executor(), single));
    }

    counter = new CountDownLatch(deferredList.size());

    promise.execute();
    for (Deferred<Object, Throwable> task : deferredList) {
      task.promise().done(new FulfillCallbackDone<Object>() {
        @Override
        public void onFulfilled(@NonNull Object value) {
          done();
        }
      }, new RejectCallbackDone<Throwable>() {
        @Override
        public void onRejected(@NonNull Throwable reason) {
          done();
        }
      });
    }
  }

  private synchronized void done() {
    if (!promise.isExecuting()) return;

    counter.countDown();

    if (counter.getCount() > 0) return;

    boolean isAllFulfilled = true;
    for (Deferred<Object, Throwable> deferred : deferredList) {
      if (Promise.State.FULFILLED != deferred.promise().state()) {
        isAllFulfilled = false;
        break;
      }
    }

    if (isAllFulfilled) {
      LinkedList<Object> resolvedValueList = new LinkedList<>();
      for (Deferred<Object, Throwable> deferred : deferredList) {
        resolvedValueList.offer(deferred.promise().resolvedValue());
      }
      promise.resolve(resolvedValueList.toArray());

    } else {
      LinkedList<Throwable> rejectedValueList = new LinkedList<>();
      for (Deferred<Object, Throwable> deferred : deferredList) {
        rejectedValueList.offer(deferred.promise().rejectedValue());
      }
      promise.reject(rejectedValueList.toArray(new Throwable[rejectedValueList.size()]));
    }
  }
}
