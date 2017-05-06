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

abstract class TaskExecutor<F, R, T extends PromiseTask> {

  @SuppressWarnings("unchecked")
  @NonNull
  static <F, R, T extends PromiseTask> TaskExecutor create(@NonNull Promise<F, R> promise, @NonNull T task) {
    if (task instanceof PromiseTask.Single) {
      return new TaskExecutorSingle<>((Promise<F, Throwable>) promise, (PromiseTask.Single) task);
    } else if (task instanceof PromiseTask.All) {
      return new TaskExecutorAll((Promise<Object[], Throwable[]>) promise, (PromiseTask.All) task);
    } else if (task instanceof PromiseTask.Race) {
      return new TaskExecutorRace((Promise<Object, Throwable>) promise, (PromiseTask.Race) task);
    } else {
      throw new IllegalArgumentException("Not supported PromiseTask. actual:" + task);
    }
  }
  @NonNull
  Promise<F, R> promise;
  @NonNull
  T task;

  TaskExecutor(@NonNull Promise<F, R> promise, @NonNull T task) {
    this.promise = promise;
    this.task = task;
  }

  abstract void execute();
}
