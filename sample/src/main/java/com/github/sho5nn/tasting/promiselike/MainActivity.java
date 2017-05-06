package com.github.sho5nn.tasting.promiselike;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    findViewById(R.id.show_simple_activity_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(MainActivity.this, SimpleActivity.class));
      }
    });
    findViewById(R.id.show_sequence_activity_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(MainActivity.this, CoffeeMakerActivity.class));
      }
    });
  }
}
