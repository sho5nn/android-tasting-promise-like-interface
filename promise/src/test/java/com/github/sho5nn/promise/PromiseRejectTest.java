package com.github.sho5nn.promise;

import com.github.sho5nn.promise.fortestutils.CallableTask;
import com.github.sho5nn.promise.fortestutils.ForUnitTestException;
import com.github.sho5nn.promise.fortestutils.RunnableTask;

import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class PromiseRejectTest extends BaseTest {

  @Test
  public void catch_at_on_the_way() throws Exception {
    countSet(2);
    Promise.when(executor, Promise.single(CallableTask.increment(9)))
      .then(new FulfillCallbackThenSingle<Integer, Void>() {
        @Override
        public PromiseTask.Single<Void> onFulfilled(Integer value) {
          assertNotNull(value);
          assertEquals(10, (int) value);
          countDown();
          return Promise.single(CallableTask.throwException("catch_at_on_the_way"));
        }
      })
      .then(
        new FulfillCallbackThenSingle<Void, Void>() {
          @Override
          public PromiseTask.Single<Void> onFulfilled(Void value) {
            fail();
            return Promise.single(RunnableTask.empty());
          }
        },
        new RejectCallbackDone<Throwable>() {
          @Override
          public void onRejected(Throwable value) {
            assertNotNull(value);
            assertEquals(ExecutionException.class, value.getClass());
            assertEquals(ForUnitTestException.class, value.getCause().getClass());
            assertEquals("catch_at_on_the_way", value.getCause().getMessage());
            countDown();
          }
        })
      .done(
        new FulfillCallbackDone<Void>() {
          @Override
          public void onFulfilled(Void value) {
            fail();
          }
        },
        new RejectCallbackDone<Throwable>() {
          @Override
          public void onRejected(Throwable value) {
            fail();
          }
        });
    awaitToCheckCount();
  }

  @Test
  public void catch_at_last() throws Exception {
    countSet(2);
    Promise.when(executor, Promise.single(CallableTask.increment(9)))
      .then(new FulfillCallbackThenSingle<Integer, Void>() {
        @Override
        public PromiseTask.Single<Void> onFulfilled(Integer value) {
          assertNotNull(value);
          assertEquals(10, (int) value);
          countDown();
          return Promise.single(RunnableTask.throwException("catch_at_last"));
        }
      })
      .then(new FulfillCallbackThenSingle<Void, Void>() {
        @Override
        public PromiseTask.Single<Void> onFulfilled(Void value) {
          fail();
          return Promise.single(RunnableTask.empty());
        }
      })
      .done(
        new FulfillCallbackDone<Void>() {
          @Override
          public void onFulfilled(Void value) {
            fail();
          }
        },
        new RejectCallbackDone<Throwable>() {
          @Override
          public void onRejected(Throwable value) {
            assertNotNull(value);
            assertEquals(ExecutionException.class, value.getClass());
            assertEquals(ForUnitTestException.class, value.getCause().getClass());
            assertEquals("catch_at_last", value.getCause().getMessage());
            countDown();
          }
        });
    awaitToCheckCount();
  }
}
