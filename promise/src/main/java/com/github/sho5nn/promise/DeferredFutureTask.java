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

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

class DeferredFutureTask<F> extends FutureTask<F> {

  @NonNull
  private Promise<F, Throwable> promise;

  DeferredFutureTask(@NonNull Promise<F, Throwable> promise, @NonNull Callable<F> callable) {
    super(callable);
    this.promise = promise;
  }

  DeferredFutureTask(@NonNull Promise<F, Throwable> promise, @NonNull Runnable runnable) {
    super(runnable, null);
    this.promise = promise;
  }

  @Override
  protected void done() {
    F result;
    try {
      result = get();
    } catch (InterruptedException | ExecutionException e) {
      promise.reject(e);
      return;
    }

    try {

      promise.resolve(result);
    } catch (final RuntimeException e) {
      new Handler(Looper.getMainLooper()).post(new Runnable() {
        @Override
        public void run() {
          throw new RuntimeException(e);
        }
      });
    }
  }
}
