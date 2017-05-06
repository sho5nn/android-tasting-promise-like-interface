package com.github.sho5nn.promise.fortestutils;

import java.util.UUID;
import java.util.concurrent.Callable;

public class CallableTask {

  private CallableTask() {
  }

  public static Callable<Integer> increment(final int i) {
    return new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        return i + 1;
      }
    };
  }

  public static Callable<String> randomString() {
    return new Callable<String>() {
      @Override
      public String call() throws Exception {
        return UUID.randomUUID().toString();
      }
    };
  }

  public static Callable<Void> throwException() {
    return new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        throw new ForUnitTestException();
      }
    };
  }

  public static Callable<Void> throwException(final String message) {
    return new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        throw new ForUnitTestException(message);
      }
    };
  }

  public static Callable<Void> throwExceptionWithRandomMessage() {
    return new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        throw new ForUnitTestException(UUID.randomUUID().toString());
      }
    };
  }

  public static <T> Callable<T> sleep(final long millis, final T t) {
    return new Callable<T>() {
      @Override
      public T call() throws Exception {
        Thread.sleep(millis);
        return t;
      }
    };
  }

  public static Callable<Void> sleepThrowException(final long millis) {
    return new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        Thread.sleep(millis);
        throw new ForUnitTestException();
      }
    };
  }

  public static Callable<Void> sleepThrowException(final long millis, final String message) {
    return new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        Thread.sleep(millis);
        throw new ForUnitTestException(message);
      }
    };
  }
}
