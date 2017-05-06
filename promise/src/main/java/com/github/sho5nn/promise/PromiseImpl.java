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
import android.support.annotation.Nullable;

import java.util.concurrent.ExecutorService;

class PromiseImpl<F, R> extends Promise<F, R> {

  @NonNull
  private ExecutorService executor;
  @NonNull
  private State state;
  @NonNull
  private Deferred.Trigger<?, ?> trigger;
  @Nullable
  private DispatchObject<F> resolvedDispatch;
  @Nullable
  private DispatchObject<R> rejectedDispatch;

  private F resolvedValue;
  private R rejectedValue;

  PromiseImpl(@NonNull ExecutorService executor, @NonNull Deferred.Trigger<?, ?> trigger) {
    this.state = State.PENDING;
    this.executor = executor;
    this.trigger = trigger;
  }

  @NonNull
  @Override
  public Promise<F, R> atMain() {
    Handler handler = new Handler(Looper.getMainLooper());
    if (resolvedDispatch != null) resolvedDispatch.setHandler(handler);
    if (rejectedDispatch != null) rejectedDispatch.setHandler(handler);
    return this;
  }

  @NonNull
  @Override
  public Promise<F, R> at(@NonNull Handler handler) {
    if (resolvedDispatch != null) resolvedDispatch.setHandler(handler);
    if (rejectedDispatch != null) rejectedDispatch.setHandler(handler);
    return this;
  }

  @NonNull
  @Override
  public State state() {
    return state;
  }

  @Override
  public boolean isPending() {
    return state == State.PENDING;
  }

  @Override
  public boolean isExecuting() {
    return state == State.EXECUTING;
  }

  @Override
  public boolean isFulfilled() {
    return state == State.FULFILLED;
  }

  @Override
  public boolean isRejected() {
    return state == State.REJECTED;
  }

  @Override
  public F resolvedValue() {
    return resolvedValue;
  }

  @Override
  public R rejectedValue() {
    return rejectedValue;
  }

  @NonNull
  @Override
  ExecutorService executor() {
    return executor;
  }

  @NonNull
  @Override
  Promise<F, R> execute() {
    if (isPending()) {
      state = State.EXECUTING;
    }
    return this;
  }

  @NonNull
  @Override
  Promise<F, R> resolve(F value) {
    synchronized (this) {
      if (isRejected()) {
        throw new IllegalStateException("Cannot resolve, because Promise is rejected.");
      }

      if (isPending() || isExecuting()) {
        resolvedValue = value;
        state = State.FULFILLED;
      }

      if (resolvedDispatch != null) {
        resolvedDispatch.dispatch(resolvedValue);
      }
      return this;
    }
  }

  @NonNull
  @Override
  Promise<F, R> reject(R reason) {
    synchronized (this) {
      if (isFulfilled()) {
        throw new IllegalStateException("Cannot reject, because Promise is resolved.");
      }

      if (isPending() || isExecuting()) {
        rejectedValue = reason;
        state = State.REJECTED;
      }

      if (rejectedDispatch != null) {
        rejectedDispatch.dispatch(reason);
      }
      return this;
    }
  }

  private void listenResolved(@NonNull DispatchObject.Dispatcher<F> dispatcher) {
    resolvedDispatch = new DispatchObject<>(dispatcher);
  }

  private void listenRejected(@NonNull DispatchObject.Dispatcher<R> dispatcher) {
    rejectedDispatch = new DispatchObject<>(dispatcher);
  }

  @NonNull
  @Override
  public Promise<F, R> done(@NonNull final FulfillCallbackDone<F> fulfillCallback) {
    listenResolved(new DispatchObject.Dispatcher<F>() {
      @Override
      public void dispatch(F value) {
        fulfillCallback.onFulfilled(value);
      }
    });
    trigger.execute();
    return this;
  }

  @NonNull
  @Override
  public Promise<F, R> done(@NonNull final RejectCallbackDone<R> rejectCallback) {
    listenRejected(new DispatchObject.Dispatcher<R>() {
      @Override
      public void dispatch(R value) {
        rejectCallback.onRejected(value);
      }
    });
    trigger.execute();
    return this;
  }

  @NonNull
  @Override
  public Promise<F, R> done(@NonNull final FulfillCallbackDone<F> fulfillCallback,
                            @NonNull final RejectCallbackDone<R> rejectCallback) {
    listenResolved(new DispatchObject.Dispatcher<F>() {
      @Override
      public void dispatch(F value) {
        fulfillCallback.onFulfilled(value);
      }
    });
    listenRejected(new DispatchObject.Dispatcher<R>() {
      @Override
      public void dispatch(R value) {
        rejectCallback.onRejected(value);
      }
    });
    trigger.execute();
    return this;
  }

  @NonNull
  @Override
  public <NEXT_F> Promise<NEXT_F, Throwable> then(@NonNull final FulfillCallbackThenSingle<F, NEXT_F> fulfillCallback) {
    final Deferred.Later<NEXT_F, Throwable> later = new DeferredLater<>(executor, trigger);
    listenResolved(new DispatchObject.Dispatcher<F>() {
      @Override
      public void dispatch(F value) {
        later.execute(fulfillCallback.onFulfilled(value));
      }
    });
    listenRejected(new DispatchObject.Dispatcher<R>() {
      @Override
      public void dispatch(R value) {
        later.promise().reject((Throwable) value);
      }
    });
    return later.promise();
  }

  @NonNull
  @Override
  public <NEXT_F> Promise<NEXT_F, Throwable> then(@NonNull final RejectCallbackThenSingle<R, NEXT_F> rejectCallback) {
    final Deferred.Later<NEXT_F, Throwable> later = new DeferredLater<>(executor, trigger);
    listenResolved(new DispatchObject.Dispatcher<F>() {
      @SuppressWarnings("unchecked")
      @Override
      public void dispatch(F value) {
        later.promise().resolve((NEXT_F) value);
      }
    });
    listenRejected(new DispatchObject.Dispatcher<R>() {
      @Override
      public void dispatch(R value) {
        later.execute(rejectCallback.onRejected(value));
      }
    });
    return later.promise();
  }

  @NonNull
  @Override
  public <NEXT_F> Promise<NEXT_F, Throwable> then(@NonNull final FulfillCallbackThenSingle<F, NEXT_F> fulfillCallback,
                                                  @NonNull final RejectCallbackThenSingle<R, NEXT_F> rejectCallback) {
    final Deferred.Later<NEXT_F, Throwable> later = new DeferredLater<>(executor, trigger);
    listenResolved(new DispatchObject.Dispatcher<F>() {
      @Override
      public void dispatch(F value) {
        later.execute(fulfillCallback.onFulfilled(value));
      }
    });
    listenRejected(new DispatchObject.Dispatcher<R>() {
      @Override
      public void dispatch(R value) {
        later.execute(rejectCallback.onRejected(value));
      }
    });
    return later.promise();
  }

  @NonNull
  @Override
  public <NEXT_F> Promise<NEXT_F, Throwable> then(@NonNull final FulfillCallbackThenSingle<F, NEXT_F> fulfillCallback,
                                                  @NonNull final RejectCallbackDone<R> rejectCallback) {
    final Deferred.Later<NEXT_F, Throwable> later = new DeferredLater<>(executor, trigger);
    listenResolved(new DispatchObject.Dispatcher<F>() {
      @Override
      public void dispatch(F value) {
        later.execute(fulfillCallback.onFulfilled(value));
      }
    });
    listenRejected(new DispatchObject.Dispatcher<R>() {
      @Override
      public void dispatch(R value) {
        rejectCallback.onRejected(value);
      }
    });
    return later.promise();
  }

  @NonNull
  @Override
  public <NEXT_F> Promise<NEXT_F, Throwable> then(@NonNull final FulfillCallbackDone<F> fulfillCallback,
                                                  @NonNull final RejectCallbackThenSingle<R, NEXT_F> rejectCallback) {
    final Deferred.Later<NEXT_F, Throwable> later = new DeferredLater<>(executor, trigger);
    listenResolved(new DispatchObject.Dispatcher<F>() {
      @Override
      public void dispatch(F value) {
        fulfillCallback.onFulfilled(value);
      }
    });
    listenRejected(new DispatchObject.Dispatcher<R>() {
      @Override
      public void dispatch(R value) {
        later.execute(rejectCallback.onRejected(value));
      }
    });
    return later.promise();
  }

  @NonNull
  @Override
  public Promise<Object[], Throwable[]> then(@NonNull final FulfillCallbackThenAll<F> fulfillCallback) {
    final Deferred.Later<Object[], Throwable[]> later = new DeferredLater<>(executor, trigger);
    listenResolved(new DispatchObject.Dispatcher<F>() {
      @Override
      public void dispatch(F value) {
        later.execute(fulfillCallback.onFulfilled(value));
      }
    });
    listenRejected(new DispatchObject.Dispatcher<R>() {
      @Override
      public void dispatch(R value) {
        later.promise().reject((Throwable[]) value);
      }
    });
    return later.promise();
  }

  @NonNull
  @Override
  public Promise<Object[], Throwable[]> then(@NonNull final RejectCallbackThenAll<R> rejectCallback) {
    final Deferred.Later<Object[], Throwable[]> later = new DeferredLater<>(executor, trigger);
    listenResolved(new DispatchObject.Dispatcher<F>() {
      @Override
      public void dispatch(F value) {
        later.promise().resolve((Object[]) value);
      }
    });
    listenRejected(new DispatchObject.Dispatcher<R>() {
      @Override
      public void dispatch(R value) {
        later.execute(rejectCallback.onRejected(value));
      }
    });
    return later.promise();
  }

  @NonNull
  @Override
  public Promise<Object[], Throwable[]> then(@NonNull final FulfillCallbackThenAll<F> fulfillCallback,
                                             @NonNull final RejectCallbackThenAll<R> rejectCallback) {
    final Deferred.Later<Object[], Throwable[]> later = new DeferredLater<>(executor, trigger);
    listenResolved(new DispatchObject.Dispatcher<F>() {
      @Override
      public void dispatch(F value) {
        later.execute(fulfillCallback.onFulfilled(value));
      }
    });
    listenRejected(new DispatchObject.Dispatcher<R>() {
      @Override
      public void dispatch(R value) {
        later.execute(rejectCallback.onRejected(value));
      }
    });
    return later.promise();
  }

  @NonNull
  @Override
  public Promise<Object[], Throwable[]> then(@NonNull final FulfillCallbackThenAll<F> fulfillCallback,
                                             @NonNull final RejectCallbackDone<R> rejectCallback) {
    final Deferred.Later<Object[], Throwable[]> later = new DeferredLater<>(executor, trigger);
    listenResolved(new DispatchObject.Dispatcher<F>() {
      @Override
      public void dispatch(F value) {
        later.execute(fulfillCallback.onFulfilled(value));
      }
    });
    listenRejected(new DispatchObject.Dispatcher<R>() {
      @Override
      public void dispatch(R value) {
        rejectCallback.onRejected(value);
      }
    });
    return later.promise();
  }

  @NonNull
  @Override
  public Promise<Object[], Throwable[]> then(@NonNull final FulfillCallbackDone<F> fulfillCallback,
                                             @NonNull final RejectCallbackThenAll<R> rejectCallback) {
    final Deferred.Later<Object[], Throwable[]> later = new DeferredLater<>(executor, trigger);
    listenResolved(new DispatchObject.Dispatcher<F>() {
      @Override
      public void dispatch(F value) {
        fulfillCallback.onFulfilled(value);
      }
    });
    listenRejected(new DispatchObject.Dispatcher<R>() {
      @Override
      public void dispatch(R value) {
        later.execute(rejectCallback.onRejected(value));
      }
    });
    return later.promise();
  }

  @NonNull
  @Override
  public Promise<Object, Throwable> then(@NonNull final FulfillCallbackThenRace<F> fulfillCallback) {
    final Deferred.Later<Object, Throwable> later = new DeferredLater<>(executor, trigger);
    listenResolved(new DispatchObject.Dispatcher<F>() {
      @Override
      public void dispatch(F value) {
        later.execute(fulfillCallback.onFulfilled(value));
      }
    });
    listenRejected(new DispatchObject.Dispatcher<R>() {
      @Override
      public void dispatch(R value) {
        later.promise().reject((Throwable) value);
      }
    });
    return later.promise();
  }

  @NonNull
  @Override
  public Promise<Object, Throwable> then(@NonNull final RejectCallbackThenRace<R> rejectCallback) {
    final Deferred.Later<Object, Throwable> later = new DeferredLater<>(executor, trigger);
    listenResolved(new DispatchObject.Dispatcher<F>() {
      @Override
      public void dispatch(F value) {
        later.promise().resolve(value);
      }
    });
    listenRejected(new DispatchObject.Dispatcher<R>() {
      @Override
      public void dispatch(R value) {
        later.execute(rejectCallback.onRejected(value));
      }
    });
    return later.promise();
  }

  @NonNull
  @Override
  public Promise<Object, Throwable> then(@NonNull final FulfillCallbackThenRace<F> fulfillCallback,
                                         @NonNull final RejectCallbackThenRace<R> rejectCallback) {
    final Deferred.Later<Object, Throwable> later = new DeferredLater<>(executor, trigger);
    listenResolved(new DispatchObject.Dispatcher<F>() {
      @Override
      public void dispatch(F value) {
        later.execute(fulfillCallback.onFulfilled(value));
      }
    });
    listenRejected(new DispatchObject.Dispatcher<R>() {
      @Override
      public void dispatch(R value) {
        later.execute(rejectCallback.onRejected(value));
      }
    });
    return later.promise();
  }

  @NonNull
  @Override
  public Promise<Object, Throwable> then(@NonNull final FulfillCallbackThenRace<F> fulfillCallback,
                                         @NonNull final RejectCallbackDone<R> rejectCallback) {
    final Deferred.Later<Object, Throwable> later = new DeferredLater<>(executor, trigger);
    listenResolved(new DispatchObject.Dispatcher<F>() {
      @Override
      public void dispatch(F value) {
        later.execute(fulfillCallback.onFulfilled(value));
      }
    });
    listenRejected(new DispatchObject.Dispatcher<R>() {
      @Override
      public void dispatch(R value) {
        rejectCallback.onRejected(value);
      }
    });
    return later.promise();
  }

  @NonNull
  @Override
  public Promise<Object, Throwable> then(@NonNull final FulfillCallbackDone<F> fulfillCallback,
                                         @NonNull final RejectCallbackThenRace<R> rejectCallback) {
    final Deferred.Later<Object, Throwable> later = new DeferredLater<>(executor, trigger);
    listenResolved(new DispatchObject.Dispatcher<F>() {
      @Override
      public void dispatch(F value) {
        fulfillCallback.onFulfilled(value);
      }
    });
    listenRejected(new DispatchObject.Dispatcher<R>() {
      @Override
      public void dispatch(R value) {
        later.execute(rejectCallback.onRejected(value));
      }
    });
    return later.promise();
  }
}
