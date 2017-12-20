package com.bumptech.glide;

import static com.bumptech.glide.test.BitmapSubject.assertThat;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.bumptech.glide.test.ConcurrencyHelper;
import com.bumptech.glide.test.TearDownGlide;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class Issue2733Test {
  @Rule public TearDownGlide tearDownGlide = new TearDownGlide();
  private final ConcurrencyHelper concurrency = new ConcurrencyHelper();
  private Context context;

  @Before
  public void setUp() {
    context = InstrumentationRegistry.getTargetContext();
  }

  @Test
  public void issue2733() {
    String url =
        "https://d18o6ewmssubp8.cloudfront.net/images/55018f67-e844-4ebf-bb0b-51552a11465c.jpg";

    Bitmap bitmap =
        concurrency.get(
            Glide.with(context)
                .asBitmap()
                .load(url)
                .submit());
    assertThat(bitmap).isNotNull();
  }
}
