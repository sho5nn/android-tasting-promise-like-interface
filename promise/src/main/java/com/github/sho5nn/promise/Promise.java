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
import android.support.annotation.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class Promise<F, R> {

  @NonNull
  public static <F> Promise<F, Throwable> when(@NonNull ExecutorService executor, @NonNull PromiseTask.Single<F> single) {
    return new DeferredTrigger<F, Throwable>(executor, single).promise();
  }

  @NonNull
  public static Promise<Object[], Throwable[]> when(@NonNull ExecutorService executor, @NonNull PromiseTask.All all) {
    return new DeferredTrigger<Object[], Throwable[]>(executor, all).promise();
  }

  @NonNull
  public static Promise<Object, Throwable> when(@NonNull ExecutorService executor, @NonNull PromiseTask.Race race) {
    return new DeferredTrigger<Object, Throwable>(executor, race).promise();
  }

  @NonNull
  public static PromiseTask.Single<Void> single(@NonNull Runnable task) {
    return new PromiseTask.Single<>(task);
  }

  @NonNull
  public static <F> PromiseTask.Single<F> single(@NonNull Callable<F> task) {
    return new PromiseTask.Single<>(task);
  }

  @NonNull
  public static PromiseTask.All all(@NonNull Object... taskArray) {
    return new PromiseTask.All(taskArray);
  }

  @NonNull
  public static PromiseTask.Race race(@NonNull Object... taskArray) {
    return new PromiseTask.Race(taskArray);
  }

  enum State {
    PENDING,
    EXECUTING,
    FULFILLED,
    REJECTED,
  }

  @NonNull
  public abstract Promise<F, R> atMain();

  @NonNull
  public abstract Promise<F, R> at(@NonNull Handler handler);

  @NonNull
  public abstract State state();

  public abstract boolean isPending();

  public abstract boolean isExecuting();

  public abstract boolean isFulfilled();

  public abstract boolean isRejected();

  public abstract F resolvedValue();

  public abstract R rejectedValue();

  @NonNull
  abstract ExecutorService executor();

  @NonNull
  abstract Promise<F, R> resolve(F value);

  @NonNull
  abstract Promise<F, R> reject(R reason);

  @NonNull
  abstract Promise<F, R> execute();

  @NonNull
  public abstract Promise<F, R> done(@NonNull FulfillCallbackDone<F> fulfillCallback);

  @NonNull
  public abstract Promise<F, R> done(@NonNull RejectCallbackDone<R> rejectCallback);

  @NonNull
  public abstract Promise<F, R> done(@NonNull FulfillCallbackDone<F> fulfillCallback,
                            @NonNull RejectCallbackDone<R> rejectCallback);

  @NonNull
  public abstract <NEXT_F> Promise<NEXT_F, Throwable> then(@NonNull FulfillCallbackThenSingle<F, NEXT_F> fulfillCallback);

  @NonNull
  public abstract <NEXT_F> Promise<NEXT_F, Throwable> then(@NonNull RejectCallbackThenSingle<R, NEXT_F> rejectCallback);

  @NonNull
  public abstract <NEXT_F> Promise<NEXT_F, Throwable> then(@NonNull FulfillCallbackThenSingle<F, NEXT_F> fulfillCallback,
                                                           @NonNull RejectCallbackThenSingle<R, NEXT_F> rejectCallback);

  @NonNull
  public abstract <NEXT_F> Promise<NEXT_F, Throwable> then(@NonNull FulfillCallbackThenSingle<F, NEXT_F> fulfillCallback,
                                                           @NonNull RejectCallbackDone<R> rejectCallback);

  @NonNull
  public abstract <NEXT_F> Promise<NEXT_F, Throwable> then(@NonNull FulfillCallbackDone<F> fulfillCallback,
                                                           @NonNull RejectCallbackThenSingle<R, NEXT_F> rejectCallback);

  @NonNull
  public abstract Promise<Object[], Throwable[]> then(@NonNull FulfillCallbackThenAll<F> fulfillCallback);

  @NonNull
  public abstract Promise<Object[], Throwable[]> then(@NonNull RejectCallbackThenAll<R> rejectCallback);

  @NonNull
  public abstract Promise<Object[], Throwable[]> then(@NonNull FulfillCallbackThenAll<F> fulfillCallback,
                                                      @NonNull RejectCallbackThenAll<R> rejectCallback);

  @NonNull
  public abstract Promise<Object[], Throwable[]> then(@NonNull FulfillCallbackThenAll<F> fulfillCallback,
                                                      @NonNull RejectCallbackDone<R> rejectCallback);

  @NonNull
  public abstract Promise<Object[], Throwable[]> then(@NonNull FulfillCallbackDone<F> fulfillCallback,
                                                      @NonNull RejectCallbackThenAll<R> rejectCallback);

  @NonNull
  public abstract Promise<Object, Throwable> then(@NonNull FulfillCallbackThenRace<F> fulfillCallback);

  @NonNull
  public abstract Promise<Object, Throwable> then(@NonNull RejectCallbackThenRace<R> rejectCallback);

  @NonNull
  public abstract Promise<Object, Throwable> then(@NonNull FulfillCallbackThenRace<F> fulfillCallback,
                                                  @NonNull RejectCallbackThenRace<R> rejectCallback);

  @NonNull
  public abstract Promise<Object, Throwable> then(@NonNull FulfillCallbackThenRace<F> fulfillCallback,
                                                  @NonNull RejectCallbackDone<R> rejectCallback);

  @NonNull
  public abstract Promise<Object, Throwable> then(@NonNull FulfillCallbackDone<F> fulfillCallback,
                                                  @NonNull RejectCallbackThenRace<R> rejectCallback);
}
