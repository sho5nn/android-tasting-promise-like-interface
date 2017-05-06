package com.github.sho5nn.promise;

import android.support.annotation.NonNull;

import com.github.sho5nn.promise.fortestutils.CallableTask;
import com.github.sho5nn.promise.fortestutils.ForUnitTestException;

import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PromiseRaceThenTest extends BaseTest {

  @Test
  public void fulfilled_then_single_fulfilled() throws Exception {
    countSet(2);
    Promise.when(executor,
      Promise.race(
        CallableTask.sleep(10, "foo"),
        CallableTask.sleep(100, "bar"),
        CallableTask.sleep(1000, "baz")
      ))
      .then(new FulfillCallbackThenSingle<Object, Integer>() {
        @Override
        public PromiseTask.Single<Integer> onFulfilled(@NonNull Object value) {
          assertNotNull(value);
          assertEquals("foo", value);
          countDown();
          return Promise.single(CallableTask.increment(9));
        }
      })
      .done(new FulfillCallbackDone<Integer>() {
        @Override
        public void onFulfilled(@NonNull Integer value) {
          assertNotNull(value);
          assertEquals(10, (int) value);
          countDown();
        }
      });
    awaitToCheckCount();
  }

  @Test
  public void fulfilled_then_single_rejected() throws Exception {
    countSet(2);
    Promise.when(executor,
      Promise.race(
        CallableTask.sleep(10, "foo"),
        CallableTask.sleep(100, "bar"),
        CallableTask.sleep(1000, "baz")
      ))
      .then(new FulfillCallbackThenSingle<Object, Void>() {
        @Override
        public PromiseTask.Single<Void> onFulfilled(@NonNull Object value) {
          assertNotNull(value);
          assertEquals("foo", value);
          countDown();
          return Promise.single(CallableTask.throwException());
        }
      })
      .done(new RejectCallbackDone<Throwable>() {
        @Override
        public void onRejected(@NonNull Throwable value) {
          assertNotNull(value);
          assertEquals(ExecutionException.class, value.getClass());
          assertEquals(ForUnitTestException.class, value.getCause().getClass());
          countDown();
        }
      });
    awaitToCheckCount();
  }

  @Test
  public void fulfilled_then_all_fulfilled() throws Exception {
    countSet(2);
    Promise.when(executor,
      Promise.race(
        CallableTask.sleep(10, "foo"),
        CallableTask.sleep(100, "bar"),
        CallableTask.sleep(1000, "baz")
      ))
      .then(new FulfillCallbackThenAll<Object>() {
        @Override
        public PromiseTask.All onFulfilled(@NonNull Object value) {
          assertNotNull(value);
          assertEquals("foo", value);
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
        public void onFulfilled(@NonNull Object[] value) {
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
    Promise.when(executor,
      Promise.race(
        CallableTask.sleep(10, "foo"),
        CallableTask.sleep(100, "bar"),
        CallableTask.sleep(1000, "baz")
      ))
      .then(new FulfillCallbackThenAll<Object>() {
        @Override
        public PromiseTask.All onFulfilled(@NonNull Object value) {
          assertNotNull(value);
          assertEquals("foo", value);
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
        public void onRejected(@NonNull Throwable[] value) {
          assertNotNull(value);
          assertEquals(3, value.length);
          assertEquals(ExecutionException.class, value[0].getClass());
          assertEquals(ForUnitTestException.class, value[0].getCause().getClass());
          assertNull(value[1]);
          assertNull(value[2]);
          countDown();
        }
      });
    awaitToCheckCount();
  }

  @Test
  public void fulfilled_then_race_fulfilled() throws Exception {
    countSet(2);
    Promise.when(executor,
      Promise.race(
        CallableTask.sleep(10, "foo"),
        CallableTask.sleep(100, "bar"),
        CallableTask.sleep(1000, "baz")
      ))
      .then(new FulfillCallbackThenRace<Object>() {
        @Override
        public PromiseTask.Race onFulfilled(@NonNull Object value) {
          assertNotNull(value);
          assertEquals("foo", value);
          countDown();
          return Promise.race(
            CallableTask.sleep(10, 3.14f),
            CallableTask.sleep(100, true),
            CallableTask.sleep(1000, "foo")
          );
        }
      })
      .done(new FulfillCallbackDone<Object>() {
        @Override
        public void onFulfilled(@NonNull Object value) {
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
    Promise.when(executor,
      Promise.race(
        CallableTask.sleep(10, "foo"),
        CallableTask.sleep(100, "bar"),
        CallableTask.sleep(1000, "baz")
      ))
      .then(new FulfillCallbackThenRace<Object>() {
        @Override
        public PromiseTask.Race onFulfilled(@NonNull Object value) {
          assertNotNull(value);
          assertEquals("foo", value);
          countDown();
          return Promise.race(
            CallableTask.sleepThrowException(10),
            CallableTask.sleep(100, true),
            CallableTask.sleep(1000, "foo")
          );
        }
      })
      .done(new RejectCallbackDone<Throwable>() {
        @Override
        public void onRejected(@NonNull Throwable value) {
          assertNotNull(value);
          assertEquals(ExecutionException.class, value.getClass());
          assertEquals(ForUnitTestException.class, value.getCause().getClass());
          countDown();
        }
      });
    awaitToCheckCount();
  }

  @Test
  public void rejected_then_single_fulfilled() throws Exception {
    countSet(2);
    Promise.when(executor,
      Promise.race(
        CallableTask.sleep(100, "foo"),
        CallableTask.sleepThrowException(10),
        CallableTask.sleep(1000, "baz")
      ))
      .then(new RejectCallbackThenSingle<Throwable, Integer>() {
        @Override
        public PromiseTask.Single<Integer> onRejected(@NonNull Throwable value) {
          assertNotNull(value);
          assertEquals(ExecutionException.class, value.getClass());
          assertEquals(ForUnitTestException.class, value.getCause().getClass());
          countDown();
          return Promise.single(CallableTask.increment(9999));
        }
      })
      .done(new FulfillCallbackDone<Integer>() {
        @Override
        public void onFulfilled(@NonNull Integer value) {
          assertNotNull(value);
          assertEquals(10000, (int) value);
          countDown();
        }
      });
    awaitToCheckCount();
  }

  @Test
  public void rejected_then_single_rejected() throws Exception {
    countSet(2);
    Promise.when(executor,
      Promise.race(
        CallableTask.sleep(100, "foo"),
        CallableTask.sleepThrowException(10),
        CallableTask.sleep(1000, "baz")
      ))
      .then(new RejectCallbackThenSingle<Throwable, Void>() {
        @Override
        public PromiseTask.Single<Void> onRejected(@NonNull Throwable value) {
          assertNotNull(value);
          assertEquals(ExecutionException.class, value.getClass());
          assertEquals(ForUnitTestException.class, value.getCause().getClass());
          countDown();
          return Promise.single(CallableTask.throwException());
        }
      })
      .done(new RejectCallbackDone<Throwable>() {
        @Override
        public void onRejected(@NonNull Throwable value) {
          assertNotNull(value);
          assertEquals(ExecutionException.class, value.getClass());
          assertEquals(ForUnitTestException.class, value.getCause().getClass());
          countDown();
        }
      });
    awaitToCheckCount();
  }

  @Test
  public void rejected_then_all_fulfilled() throws Exception {
    countSet(2);
    Promise.when(executor,
      Promise.race(
        CallableTask.sleep(100, "foo"),
        CallableTask.sleepThrowException(10),
        CallableTask.sleep(1000, "baz")
      ))
      .then(new RejectCallbackThenAll<Throwable>() {
        @Override
        public PromiseTask.All onRejected(@NonNull Throwable value) {
          assertNotNull(value);
          assertEquals(ExecutionException.class, value.getClass());
          assertEquals(ForUnitTestException.class, value.getCause().getClass());
          countDown();
          return Promise.all(
            CallableTask.increment(9999),
            CallableTask.increment(99999),
            CallableTask.increment(999999)
          );
        }
      })
      .done(new FulfillCallbackDone<Object[]>() {
        @Override
        public void onFulfilled(@NonNull Object[] value) {
          assertNotNull(value);
          assertEquals(3, value.length);
          assertEquals(10000, value[0]);
          assertEquals(100000, value[1]);
          assertEquals(1000000, value[2]);
          countDown();
        }
      });
    awaitToCheckCount();
  }

  @Test
  public void rejected_then_all_rejected() throws Exception {
    countSet(2);
    Promise.when(executor,
      Promise.race(
        CallableTask.sleep(100, "foo"),
        CallableTask.sleepThrowException(10),
        CallableTask.sleep(1000, "baz")
      ))
      .then(new RejectCallbackThenAll<Throwable>() {
        @Override
        public PromiseTask.All onRejected(@NonNull Throwable value) {
          assertNotNull(value);
          assertEquals(ExecutionException.class, value.getClass());
          assertEquals(ForUnitTestException.class, value.getCause().getClass());
          countDown();
          return Promise.all(
            CallableTask.throwException(),
            CallableTask.increment(99999),
            CallableTask.increment(999999)
          );
        }
      })
      .done(new RejectCallbackDone<Throwable[]>() {
        @Override
        public void onRejected(@NonNull Throwable[] value) {
          assertNotNull(value);
          assertEquals(3, value.length);
          assertEquals(ExecutionException.class, value[0].getClass());
          assertEquals(ForUnitTestException.class, value[0].getCause().getClass());
          assertNull(value[1]);
          assertNull(value[2]);
          countDown();
        }
      });
    awaitToCheckCount();
  }

  @Test
  public void rejected_then_race_fulfilled() throws Exception {
    countSet(2);
    Promise.when(executor,
      Promise.race(
        CallableTask.sleep(100, "foo"),
        CallableTask.sleepThrowException(10),
        CallableTask.sleep(1000, "baz")
      ))
      .then(new RejectCallbackThenRace<Throwable>() {
        @Override
        public PromiseTask.Race onRejected(@NonNull Throwable value) {
          assertNotNull(value);
          assertEquals(ExecutionException.class, value.getClass());
          assertEquals(ForUnitTestException.class, value.getCause().getClass());
          countDown();
          return Promise.race(
            CallableTask.sleep(10, 3.14f),
            CallableTask.sleep(100, true),
            CallableTask.sleep(1000, "foo")
          );
        }
      })
      .done(new FulfillCallbackDone<Object>() {
        @Override
        public void onFulfilled(@NonNull Object value) {
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
    Promise.when(executor,
      Promise.race(
        CallableTask.sleep(100, "foo"),
        CallableTask.sleepThrowException(10),
        CallableTask.sleep(1000, "baz")
      ))
      .then(new RejectCallbackThenRace<Throwable>() {
        @Override
        public PromiseTask.Race onRejected(@NonNull Throwable value) {
          assertNotNull(value);
          assertEquals(ExecutionException.class, value.getClass());
          assertEquals(ForUnitTestException.class, value.getCause().getClass());
          countDown();
          return Promise.race(
            CallableTask.sleepThrowException(10),
            CallableTask.sleep(100, true),
            CallableTask.sleep(1000, "foo")
          );
        }
      })
      .done(new RejectCallbackDone<Throwable>() {
        @Override
        public void onRejected(@NonNull Throwable value) {
          assertNotNull(value);
          assertEquals(ExecutionException.class, value.getClass());
          assertEquals(ForUnitTestException.class, value.getCause().getClass());
          countDown();
        }
      });
    awaitToCheckCount();
  }
}
