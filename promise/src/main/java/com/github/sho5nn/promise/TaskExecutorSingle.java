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

import java.util.concurrent.Callable;

class TaskExecutorSingle<F> extends TaskExecutor<F, Throwable, PromiseTask.Single<F>> {

  TaskExecutorSingle(@NonNull Promise<F, Throwable> promise,
                     @NonNull PromiseTask.Single<F> task) {
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

    if (task.task instanceof Callable) {
      promise.execute();
      //noinspection unchecked
      promise.executor().submit(new DeferredFutureTask<>(promise, (Callable) task.task));
    } else if (task.task instanceof Runnable) {
      promise.execute();
      promise.executor().submit(new DeferredFutureTask<>(promise, (Runnable) task.task));
    } else {
      throw new IllegalStateException("Promise only allows Runnable or Callable. actual:" + task.task);
    }
  }
}
