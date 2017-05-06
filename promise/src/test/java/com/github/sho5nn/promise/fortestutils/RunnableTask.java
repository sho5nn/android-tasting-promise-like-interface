package com.github.sho5nn.promise.fortestutils;

public class RunnableTask {

  private RunnableTask() {
  }

  public static Runnable empty() {
    return new Runnable() {
      @Override
      public void run() {
      }
    };
  }

  public static Runnable throwException() {
    return new Runnable() {
      @Override
      public void run() {
        throw new ForUnitTestException();
      }
    };
  }

  public static Runnable throwException(final String message) {
    return new Runnable() {
      @Override
      public void run() {
        throw new ForUnitTestException(message);
      }
    };
  }

  public static Runnable sleep(final long millis) {
    return new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(millis);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  public static Runnable sleepThrowException(final long millis) {
    return new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(millis);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        throw new ForUnitTestException();
      }
    };
  }

  public static Runnable sleepThrowException(final long millis, final String message) {
    return new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(millis);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        throw new ForUnitTestException(message);
      }
    };
  }
}
