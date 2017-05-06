package com.github.sho5nn.tasting.promiselike;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.sho5nn.promise.FulfillCallbackDone;
import com.github.sho5nn.promise.Promise;
import com.github.sho5nn.promise.RejectCallbackDone;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleActivity extends AppCompatActivity {

  ExecutorService executor = Executors.newCachedThreadPool();
  TextView text;
  Button button;
  ProgressBar progress;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_simple);
    text = (TextView) findViewById(R.id.text);
    button = (Button) findViewById(R.id.button);
    progress = (ProgressBar) findViewById(R.id.progress);

    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        requestRandomId();
      }
    });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    executor.shutdown();
  }

  private void requestRandomId() {
    button.setEnabled(false);
    progress.setVisibility(View.VISIBLE);

    Promise
      .when(executor, Promise.single(new SimpleActivity.RequestRandomIdCallable()))
      .done(new FulfillCallbackDone<String>() {
        @Override
        public void onFulfilled(String value) {
          button.setEnabled(true);
          progress.setVisibility(View.INVISIBLE);
          text.setText(value);
        }
      }, new RejectCallbackDone<Throwable>() {
        @Override
        public void onRejected(Throwable value) {
          button.setEnabled(true);
          progress.setVisibility(View.INVISIBLE);
          Toast.makeText(SimpleActivity.this, value.getMessage(), Toast.LENGTH_SHORT).show();
        }
      }).atMain();
  }

  private static class RequestRandomIdCallable implements Callable<String> {
    @Override
    public String call() throws Exception {
      Thread.sleep(500);
      return UUID.randomUUID().toString();
    }
  }
}
