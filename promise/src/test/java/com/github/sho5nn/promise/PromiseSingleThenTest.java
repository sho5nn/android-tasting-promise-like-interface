package com.github.sho5nn.promise;

import android.support.annotation.NonNull;

import com.github.sho5nn.promise.fortestutils.CallableTask;
import com.github.sho5nn.promise.fortestutils.ForUnitTestException;

import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class PromiseSingleThenTest extends BaseTest {

  @Test
  public void fulfilled_then_single_fulfilled() throws Exception {
    countSet(2);
    Promise
      .when(executor, Promise.single(CallableTask.increment(98)))
      .then(
        new FulfillCallbackThenSingle<Integer, Integer>() {
          @NonNull
          @Override
          public PromiseTask.Single<Integer> onFulfilled(Integer value) {
            assertNotNull(value);
            assertEquals(99, (int) value);
            countDown();
            return Promise.single(CallableTask.increment(value));
          }
        })
      .done(
        new FulfillCallbackDone<Integer>() {
          @Override
          public void onFulfilled(Integer value) {
            assertNotNull(value);
            assertEquals(100, (int) value);
            countDown();
          }
        });
    awaitToCheckCount();
  }

  @Test
  public void fulfilled_then_single_rejected() throws Exception {
    countSet(2);
    Promise.when(executor, Promise.single(CallableTask.increment(98)))
      .then(new FulfillCallbackThenSingle<Integer, Void>() {
        @NonNull
        @Override
        public PromiseTask.Single<Void> onFulfilled(Integer value) {
          assertNotNull(value);
          assertEquals(99, (int) value);
          countDown();
          return Promise.single(CallableTask.throwException());
        }
      })
      .done(new RejectCallbackDone<Throwable>() {
        @Override
        public void onRejected(Throwable reason) {
          assertNotNull(reason);
          assertEquals(ExecutionException.class, reason.getClass());
          assertEquals(ForUnitTestException.class, reason.getCause().getClass());
          countDown();
        }
      });
    awaitToCheckCount();
  }

  @Test
  public void fulfilled_then_all_fulfilled() throws Exception {
    countSet(2);
    Promise
      .when(executor, Promise.single(CallableTask.increment(99)))
      .then(new FulfillCallbackThenAll<Integer>() {
        @NonNull
        @Override
        public PromiseTask.All onFulfilled(Integer value) {
          assertNotNull(value);
          assertEquals(100, (int) value);
          countDown();
          return Promise.all(
            CallableTask.increment(9),
            CallableTask.increment(99),
            CallableTask.increment(999)
          );
        }
      })
      .done(new FulfillCallbackDone<Object[]>() {
        @Override
        public void onFulfilled(Object[] value) {
          assertNotNull(value);
          assertEquals(3, value.length);
          assertEquals(10, value[0]);
          assertEquals(100, value[1]);
          assertEquals(1000, value[2]);
          countDown();
        }
      });
    awaitToCheckCount();
  }

  @Test
  public void fulfilled_then_all_rejected() throws Exception {
    countSet(2);
    Promise.when(executor, Promise.single(CallableTask.increment(99)))
      .then(new FulfillCallbackThenAll<Integer>() {
        @NonNull
        @Override
        public PromiseTask.All onFulfilled(Integer value) {
          assertNotNull(value);
          assertEquals(100, (int) value);
          countDown();
          return Promise.all(
            CallableTask.throwException(),
            CallableTask.increment(99),
            CallableTask.increment(999)
          );
        }
      })
      .done(new RejectCallbackDone<Throwable[]>() {
        @Override
        public void onRejected(Throwable[] reason) {
          assertNotNull(reason);
          assertEquals(3, reason.length);

          assertEquals(ExecutionException.class, reason[0].getClass());
          assertEquals(ForUnitTestException.class, reason[0].getCause().getClass());
          assertNull(reason[1]);
          assertNull(reason[2]);
          countDown();
        }
      });
    awaitToCheckCount();
  }

  @Test
  public void fulfilled_then_race_fulfilled() throws Exception {
    countSet(2);
    Promise.when(executor, Promise.single(CallableTask.increment(99)))
      .then(new FulfillCallbackThenRace<Integer>() {
        @NonNull
        @Override
        public PromiseTask.Race onFulfilled(Integer value) {
          assertNotNull(value);
          assertEquals(100, (int) value);
          countDown();
          return Promise.race(
            CallableTask.sleep(100, "foo"),
            CallableTask.sleep(10, 3.14f),
            CallableTask.sleep(1000, true)
          );
        }
      })
      .done(new FulfillCallbackDone<Object>() {
        @Override
        public void onFulfilled(Object value) {
          assertNotNull(value);
          assertEquals(Float.class, value.getClass());
          assertEquals(3.14f, value);
          countDown();
        }
      });
    awaitToCheckCount();
  }

  @Test
  public void fulfilled_then_race_rejected() throws Exception {
    countSet(2);
    Promise.when(executor, Promise.single(CallableTask.increment(99)))
      .then(new FulfillCallbackThenRace<Integer>() {
        @NonNull
        @Override
        public PromiseTask.Race onFulfilled(Integer value) {
          assertNotNull(value);
          assertEquals(100, (int) value);
          countDown();
          return Promise.race(
            CallableTask.sleep(100, "foo"),
            CallableTask.sleepThrowException(10),
            CallableTask.sleep(1000, true)
          );
        }
      })
      .done(new RejectCallbackDone<Throwable>() {
        @Override
        public void onRejected(Throwable reason) {
          assertNotNull(reason);
          assertEquals(ExecutionException.class, reason.getClass());
          assertEquals(ForUnitTestException.class, reason.getCause().getClass());
          countDown();
        }
      });
    awaitToCheckCount();
  }

  @Test
  public void rejected_then_single_fulfilled() throws Exception {
    countSet(2);
    Promise.when(executor, Promise.single(CallableTask.throwException()))
      .then(new RejectCallbackThenSingle<Throwable, Integer>() {
        @NonNull
        @Override
        public PromiseTask.Single<Integer> onRejected(Throwable reason) {
          assertNotNull(reason);
          assertEquals(ExecutionException.class, reason.getClass());
          assertEquals(ForUnitTestException.class, reason.getCause().getClass());
          countDown();
          return Promise.single(CallableTask.increment(99));
        }
      })
      .done(new FulfillCallbackDone<Integer>() {
        @Override
        public void onFulfilled(Integer value) {
          assertNotNull(value);
          assertEquals(100, (int) value);
          countDown();
        }
      });
    awaitToCheckCount();
  }

  @Test
  public void rejected_then_single_rejected() throws Exception {
    countSet(2);
    Promise.when(executor, Promise.single(CallableTask.throwException()))
      .then(new RejectCallbackThenSingle<Throwable, Void>() {
        @NonNull
        @Override
        public PromiseTask.Single<Void> onRejected(Throwable reason) {
          assertNotNull(reason);
          assertEquals(ExecutionException.class, reason.getClass());
          assertEquals(ForUnitTestException.class, reason.getCause().getClass());
          countDown();
          return Promise.single(CallableTask.throwException());
        }
      })
      .done(new RejectCallbackDone<Throwable>() {
        @Override
        public void onRejected(Throwable reason) {
          assertNotNull(reason);
          assertEquals(ExecutionException.class, reason.getClass());
          assertEquals(ForUnitTestException.class, reason.getCause().getClass());
          countDown();
        }
      });
    awaitToCheckCount();
  }

  @Test
  public void rejected_then_all_fulfilled() throws Exception {
    countSet(2);
    Promise.when(executor, Promise.single(CallableTask.throwException()))
      .then(new RejectCallbackThenAll<Throwable>() {
        @NonNull
        @Override
        public PromiseTask.All onRejected(Throwable reason) {
          assertNotNull(reason);
          assertEquals(ExecutionException.class, reason.getClass());
          assertEquals(ForUnitTestException.class, reason.getCause().getClass());
          countDown();
          return Promise.all(
            CallableTask.increment(9),
            CallableTask.increment(99),
            CallableTask.increment(999)
          );
        }
      })
      .done(new FulfillCallbackDone<Object[]>() {
        @Override
        public void onFulfilled(Object[] value) {
          assertNotNull(value);
          assertEquals(3, value.length);
          assertEquals(10, value[0]);
          assertEquals(100, value[1]);
          assertEquals(1000, value[2]);
          countDown();
        }
      });
    awaitToCheckCount();
  }

  @Test
  public void rejected_then_all_rejected() throws Exception {
    countSet(2);
    Promise.when(executor, Promise.single(CallableTask.throwException()))
      .then(new RejectCallbackThenAll<Throwable>() {
        @NonNull
        @Override
        public PromiseTask.All onRejected(Throwable reason) {
          assertNotNull(reason);
          assertEquals(ExecutionException.class, reason.getClass());
          assertEquals(ForUnitTestException.class, reason.getCause().getClass());
          countDown();
          return Promise.all(
            CallableTask.throwException(),
            CallableTask.increment(99),
            CallableTask.increment(999)
          );
        }
      })
      .done(new RejectCallbackDone<Throwable[]>() {
        @Override
        public void onRejected(Throwable[] reason) {
          assertNotNull(reason);
          assertEquals(3, reason.length);

          assertEquals(ExecutionException.class, reason[0].getClass());
          assertEquals(ForUnitTestException.class, reason[0].getCause().getClass());
          assertNull(reason[1]);
          assertNull(reason[2]);
          countDown();
        }
      });
    awaitToCheckCount();
  }

  @Test
  public void rejected_then_race_fulfilled() throws Exception {
    countSet(2);
    Promise.when(executor, Promise.single(CallableTask.throwException()))
      .then(new RejectCallbackThenRace<Throwable>() {
        @NonNull
        @Override
        public PromiseTask.Race onRejected(Throwable reason) {
          assertNotNull(reason);
          assertEquals(ExecutionException.class, reason.getClass());
          assertEquals(ForUnitTestException.class, reason.getCause().getClass());
          countDown();
          return Promise.race(
            CallableTask.sleep(100, "foo"),
            CallableTask.sleep(10, 3.14f),
            CallableTask.sleep(1000, true)
          );
        }
      })
      .done(new FulfillCallbackDone<Object>() {
        @Override
        public void onFulfilled(Object value) {
          assertNotNull(value);
          assertEquals(Float.class, value.getClass());
          assertEquals(3.14f, value);
          countDown();
        }
      });
    awaitToCheckCount();
  }

  @Test
  public void rejected_then_race_rejected() throws Exception {
    countSet(2);
    Promise.when(executor, Promise.single(CallableTask.throwException()))
      .then(new RejectCallbackThenRace<Throwable>() {
        @NonNull
        @Override
        public PromiseTask.Race onRejected(Throwable reason) {
          assertNotNull(reason);
          assertEquals(ExecutionException.class, reason.getClass());
          assertEquals(ForUnitTestException.class, reason.getCause().getClass());
          countDown();
          return Promise.race(
            CallableTask.sleep(100, "foo"),
            CallableTask.sleepThrowException(10),
            CallableTask.sleep(1000, true)
          );
        }
      })
      .done(new RejectCallbackDone<Throwable>() {
        @Override
        public void onRejected(Throwable reason) {
          assertNotNull(reason);
          assertEquals(ExecutionException.class, reason.getClass());
          assertEquals(ForUnitTestException.class, reason.getCause().getClass());
          countDown();
        }
      });
    awaitToCheckCount();
  }
}
