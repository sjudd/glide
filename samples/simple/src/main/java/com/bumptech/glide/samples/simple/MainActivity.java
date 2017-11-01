package com.bumptech.glide.samples.simple;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

/**
 * Displays a centered image.
 */
public class MainActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ImageView imageView = findViewById(R.id.image_view);
    Glide.with(this)
        .load("https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png")
        .into(imageView);
  }
}
