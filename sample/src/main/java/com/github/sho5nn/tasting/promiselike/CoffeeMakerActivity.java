package com.github.sho5nn.tasting.promiselike;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.sho5nn.promise.FulfillCallbackDone;
import com.github.sho5nn.promise.FulfillCallbackThenSingle;
import com.github.sho5nn.promise.Promise;
import com.github.sho5nn.promise.PromiseTask;
import com.github.sho5nn.promise.RejectCallbackDone;

import java.security.SecureRandom;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CoffeeMakerActivity extends AppCompatActivity {

  private static final String TAG = CoffeeMakerActivity.class.getSimpleName();

  ExecutorService executor;
  TextView coffee;
  ProgressBar progress;
  Button startButton;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_coffee_maker);

    executor = Executors.newCachedThreadPool();

    coffee = (TextView) findViewById(R.id.text);
    progress = (ProgressBar) findViewById(R.id.progress);
    startButton = (Button) findViewById(R.id.button);
    startButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        makeCoffee();
      }
    });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    executor.shutdown();
  }

  void makeCoffee() {
    startButton.setEnabled(false);
    progress.setVisibility(View.VISIBLE);
    coffee.setText("making...");

    Promise
      .when(executor, Promise.single(new HeatingCallable()))
      .then(new FulfillCallbackThenSingle<String, String>() {
        @Override
        public PromiseTask.Single<String> onFulfilled(String value) {
          // In the worker thread.
          Log.d(TAG, value);
          return Promise.single(new PumpingCallable());
        }
      })
      .then(new FulfillCallbackThenSingle<String, String>() {
        @Override
        public PromiseTask.Single<String> onFulfilled(String value) {
          // In the worker thread.
          Log.d(TAG, value);
          return Promise.single(new CoffeeCallable());
        }
      })
      .done(new FulfillCallbackDone<String>() {
        @Override
        public void onFulfilled(String value) {
          // In the main thread, because, it was calling Promise#atMain().
          Log.d(TAG, value);
          coffee.setText(value);
          startButton.setEnabled(true);
          progress.setVisibility(View.GONE);
        }
      }, new RejectCallbackDone<Throwable>() {
        @Override
        public void onRejected(Throwable value) {
          // In the main thread, because, it was calling Promise#atMain().
          Log.w(TAG, value);
          coffee.setText(value.getMessage());
          startButton.setEnabled(true);
          progress.setVisibility(View.GONE);
        }
      }).atMain();
  }

  private static class HeatingCallable implements Callable<String> {
    @Override
    public String call() throws Exception {
      Thread.sleep(1000);
      return "~ ~ ~ heating ~ ~ ~";
    }
  }

  private static class PumpingCallable implements Callable<String> {
    @Override
    public String call() throws Exception {
      Thread.sleep(1000);
      int random = new SecureRandom().nextInt(10);
      if (random % 3 == 0) {
        throw new IllegalStateException("Oops! CoffeeMaker trouble...");
      }
      return "=> => pumping => =>";
    }
  }

  private static class CoffeeCallable implements Callable<String> {
    @Override
    public String call() throws Exception {
      Thread.sleep(500);
      return "[_]P  coffee!  [_]P";
    }
  }
}
