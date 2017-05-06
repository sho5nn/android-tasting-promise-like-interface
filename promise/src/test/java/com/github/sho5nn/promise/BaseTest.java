package com.github.sho5nn.promise;

import org.junit.Before;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public abstract class BaseTest {

  ExecutorService executor;
  CountDownLatch counter;

  @Before
  public void setup() throws Exception {
    executor = Executors.newCachedThreadPool();
  }

  void countSet(int max) throws Exception {
    counter = new CountDownLatch(max);
  }

  void countDown() {
    counter.countDown();
  }

  void awaitToCheckCount() throws Exception {
    counter.await(2, TimeUnit.SECONDS);
    assertEquals(0, counter.getCount());
  }
}
