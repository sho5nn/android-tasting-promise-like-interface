package com.github.sho5nn.promise;

import com.github.sho5nn.promise.fortestutils.CallableTask;
import com.github.sho5nn.promise.fortestutils.ForUnitTestException;
import com.github.sho5nn.promise.fortestutils.RunnableTask;

import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class PromiseTaskTest extends BaseTest {

  @Test
  public void single_fulfilled_callable() throws Exception {
    countSet(1);
    Promise.when(executor, Promise.single(CallableTask.increment(99)))
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
  public void single_fulfilled_runnable() throws Exception {
    countSet(1);
    Promise.when(executor, Promise.single(RunnableTask.empty()))
      .done(new FulfillCallbackDone<Void>() {
        @Override
        public void onFulfilled(Void value) {
          assertNull(value);
          countDown();
        }
      });
    awaitToCheckCount();
  }

  @Test
  public void single_rejected_callable() throws Exception {
    countSet(1);
    Promise.when(executor, Promise.single(CallableTask.throwException()))
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
  public void single_rejected_runnable() throws Exception {
    countSet(1);
    Promise.when(executor, Promise.single(RunnableTask.throwException()))
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
  public void all_fulfilled_callable() throws Exception {
    countSet(1);
    Promise.when(executor,
      Promise.all(
        CallableTask.increment(9),
        CallableTask.increment(99),
        CallableTask.increment(999)
      ))
      .done(new FulfillCallbackDone<Object[]>() {
        @Override
        public void onFulfilled(Object[] value) {
          assertNotNull(value);
          assertEquals(3, value.length);

          assertEquals(10, (int) value[0]);
          assertEquals(100, (int) value[1]);
          assertEquals(1000, (int) value[2]);
          countDown();
        }
      });
    awaitToCheckCount();
  }

  @Test
  public void all_fulfilled_runnable() throws Exception {
    countSet(1);
    Promise.when(executor,
      Promise.all(
        RunnableTask.empty(),
        RunnableTask.empty(),
        RunnableTask.empty()
      ))
      .done(new FulfillCallbackDone<Object[]>() {
        @Override
        public void onFulfilled(Object[] value) {
          assertNotNull(value);
          assertEquals(3, value.length);

          assertNull(value[0]);
          assertNull(value[1]);
          assertNull(value[2]);
          countDown();

        }
      });
    awaitToCheckCount();
  }

  @Test
  public void all_fulfilled_mixture() throws Exception {
    countSet(1);
    Promise.when(executor,
      Promise.all(
        CallableTask.increment(9),
        RunnableTask.empty(),
        CallableTask.increment(999)
      ))
      .done(new FulfillCallbackDone<Object[]>() {
        @Override
        public void onFulfilled(Object[] value) {
          assertNotNull(value);
          assertEquals(3, value.length);

          assertEquals(10, (int) value[0]);
          assertNull(value[1]);
          assertEquals(1000, (int) value[2]);
          countDown();

        }
      });
    awaitToCheckCount();
  }

  @Test
  public void all_rejected_callable() throws Exception {
    countSet(1);
    Promise.when(executor,
      Promise.all(
        CallableTask.increment(9),
        CallableTask.throwException(),
        CallableTask.increment(999)
      ))
      .done(new RejectCallbackDone<Throwable[]>() {
        @Override
        public void onRejected(Throwable[] reason) {
          assertNotNull(reason);
          assertEquals(3, reason.length);

          assertNull(reason[0]);
          assertEquals(ExecutionException.class, reason[1].getClass());
          assertEquals(ForUnitTestException.class, reason[1].getCause().getClass());
          assertNull(reason[2]);
          countDown();
        }
      });
    awaitToCheckCount();
  }

  @Test
  public void all_rejected_runnable() throws Exception {
    countSet(1);
    Promise.when(executor,
      Promise.all(
        RunnableTask.empty(),
        RunnableTask.empty(),
        RunnableTask.throwException()
      ))
      .done(new RejectCallbackDone<Throwable[]>() {
        @Override
        public void onRejected(Throwable[] reason) {
          assertNotNull(reason);
          assertEquals(3, reason.length);

          assertNull(reason[0]);
          assertNull(reason[1]);
          assertEquals(ExecutionException.class, reason[2].getClass());
          assertEquals(ForUnitTestException.class, reason[2].getCause().getClass());
          countDown();
        }
      });
    awaitToCheckCount();
  }

  @Test
  public void all_rejected_mixture() throws Exception {
    countSet(1);
    Promise.when(executor,
      Promise.all(
        CallableTask.throwException(),
        CallableTask.increment(99),
        RunnableTask.throwException()
      ))
      .done(new RejectCallbackDone<Throwable[]>() {
        @Override
        public void onRejected(Throwable[] reason) {
          assertNotNull(reason);
          assertEquals(3, reason.length);

          assertEquals(ExecutionException.class, reason[0].getClass());
          assertEquals(ForUnitTestException.class, reason[0].getCause().getClass());
          assertNull(reason[1]);
          assertEquals(ExecutionException.class, reason[2].getClass());
          assertEquals(ForUnitTestException.class, reason[2].getCause().getClass());
          countDown();
        }
      });
    awaitToCheckCount();
  }

  @Test
  public void race_fulfilled_callable() throws Exception {
    countSet(1);
    Promise.when(executor,
      Promise.race(
        CallableTask.sleep(100, "foo"),
        CallableTask.sleep(10, 3.14),
        CallableTask.sleep(1000, true)
      ))
      .done(new FulfillCallbackDone<Object>() {
        @Override
        public void onFulfilled(Object value) {
          assertNotNull(value);
          assertEquals(Double.class, value.getClass());
          assertEquals(3.14, value);
          countDown();
        }
      });
    awaitToCheckCount();
  }

  @Test
  public void race_fulfilled_runnable() throws Exception {
    countSet(1);
    Promise.when(executor,
      Promise.race(
        RunnableTask.sleep(1000),
        RunnableTask.sleep(100),
        RunnableTask.sleep(10)
      ))
      .done(new FulfillCallbackDone<Object>() {
        @Override
        public void onFulfilled(Object value) {
          assertNull(value);
          countDown();
        }
      });
    awaitToCheckCount();
  }

  @Test
  public void race_fulfilled_mixture() throws Exception {
    countSet(1);
    Promise.when(executor,
      Promise.race(
        CallableTask.sleep(1000, 12345),
        CallableTask.sleep(10, 3.14),
        RunnableTask.sleep(100)
      ))
      .done(new FulfillCallbackDone<Object>() {
        @Override
        public void onFulfilled(Object value) {
          assertNotNull(value);
          assertEquals(Double.class, value.getClass());
          assertEquals(3.14, value);
          countDown();
        }
      });
    awaitToCheckCount();
  }

  @Test
  public void race_rejected_callable() throws Exception {
    countSet(1);
    Promise.when(executor,
      Promise.race(
        CallableTask.sleep(100, 3.14f),
        CallableTask.sleepThrowException(10),
        CallableTask.sleep(1000, "bar")
      ))
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
  public void race_rejected_runnable() throws Exception {
    countSet(1);
    Promise.when(executor,
      Promise.race(
        RunnableTask.sleepThrowException(100, "foo"),
        RunnableTask.sleepThrowException(10, "bar"),
        RunnableTask.sleep(1000)
      ))
      .done(new RejectCallbackDone<Throwable>() {
        @Override
        public void onRejected(Throwable reason) {
          assertNotNull(reason);
          assertEquals(ExecutionException.class, reason.getClass());
          assertEquals(ForUnitTestException.class, reason.getCause().getClass());
          assertEquals(true, "bar".equals(reason.getCause().getMessage()));
          countDown();
        }
      });
    awaitToCheckCount();
  }

  @Test
  public void race_rejected_mixture() throws Exception {
    countSet(1);
    Promise.when(executor,
      Promise.race(
        CallableTask.sleepThrowException(10, "foo"),
        RunnableTask.sleepThrowException(100, "bar"),
        RunnableTask.sleep(1000)
      ))
      .done(new RejectCallbackDone<Throwable>() {
        @Override
        public void onRejected(Throwable reason) {
          assertNotNull(reason);
          assertEquals(ExecutionException.class, reason.getClass());
          assertEquals(ForUnitTestException.class, reason.getCause().getClass());
          assertEquals(true, "foo".equals(reason.getCause().getMessage()));
          countDown();
        }
      });
    awaitToCheckCount();
  }
}