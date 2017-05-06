package com.github.sho5nn.promise;

import com.github.sho5nn.promise.fortestutils.CallableTask;
import com.github.sho5nn.promise.fortestutils.ForUnitTestException;

import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class PromiseTest extends BaseTest {

  @Test
  public void change_state_to_fulfilled() throws Exception {

    Promise<Integer, Throwable> promise = Promise.when(executor,
      Promise.single(CallableTask.increment(99))
    );

    assertNotNull(promise);
    assertEquals(true, promise.isPending());
    assertEquals(false, promise.isFulfilled());
    assertEquals(false, promise.isRejected());

    countSet(1);
    promise.done(new FulfillCallbackDone<Integer>() {
      @Override
      public void onFulfilled(Integer value) {
        assertNotNull(value);
        assertEquals(100, (int) value);
        countDown();
      }
    });
    awaitToCheckCount();

    assertEquals(false, promise.isPending());
    assertEquals(true, promise.isFulfilled());
    assertEquals(false, promise.isRejected());
  }

  @Test
  public void change_state_to_rejected() throws Exception {

    Promise<Void, Throwable> promise = Promise.when(executor,
      Promise.single(CallableTask.throwException())
    );

    assertNotNull(promise);
    assertEquals(true, promise.isPending());
    assertEquals(false, promise.isFulfilled());
    assertEquals(false, promise.isRejected());

    countSet(1);
    promise.done(new RejectCallbackDone<Throwable>() {
      @Override
      public void onRejected(Throwable reason) {
        assertNotNull(reason);
        assertEquals(ExecutionException.class, reason.getClass());
        assertEquals(ForUnitTestException.class, reason.getCause().getClass());
        countDown();
      }
    });
    awaitToCheckCount();

    assertEquals(false, promise.isPending());
    assertEquals(false, promise.isFulfilled());
    assertEquals(true, promise.isRejected());
  }

  @Test
  public void cannot_resolve_again() throws Exception {

    Promise<String, Throwable> promise = Promise.when(executor,
      Promise.single(CallableTask.randomString())
    );

    countSet(1);
    promise.done(new FulfillCallbackDone<String>() {
      @Override
      public void onFulfilled(String value) {
        assertNotNull(value);
        countDown();
      }
    });
    awaitToCheckCount();

    final String resolvedValue = promise.resolvedValue();
    assertNotNull(resolvedValue);

    countSet(1);
    promise.done(new FulfillCallbackDone<String>() {
      @Override
      public void onFulfilled(String value) {
        assertNotNull(value);
        assertTrue(resolvedValue == value); // verify same object
        assertTrue(resolvedValue.equals(value)); // verify same value
        countDown();
      }
    });
    awaitToCheckCount();
  }

  @Test
  public void cannot_reject_again() throws Exception {

    Promise<Void, Throwable> promise = Promise.when(executor,
      Promise.single(CallableTask.throwExceptionWithRandomMessage())
    );

    countSet(1);
    promise.done(new RejectCallbackDone<Throwable>() {
      @Override
      public void onRejected(Throwable reason) {
        assertNotNull(reason);
        assertEquals(ExecutionException.class, reason.getClass());
        assertEquals(ForUnitTestException.class, reason.getCause().getClass());
        countDown();
      }
    });
    awaitToCheckCount();

    final Throwable rejectedReason = promise.rejectedValue();
    assertNotNull(rejectedReason);

    countSet(1);
    promise.done(new RejectCallbackDone<Throwable>() {
      @Override
      public void onRejected(Throwable reason) {
        assertNotNull(reason);
        assertEquals(ExecutionException.class, reason.getClass());
        assertEquals(ForUnitTestException.class, reason.getCause().getClass());
        assertTrue(rejectedReason == reason);
        assertTrue(rejectedReason.getCause() == reason.getCause());
        assertTrue(rejectedReason.getCause().getMessage() == reason.getCause().getMessage());  // verify same object
        assertTrue(rejectedReason.getCause().getMessage().equals(reason.getCause().getMessage()));  // verify same value
        countDown();
      }
    });
    awaitToCheckCount();
  }
}
