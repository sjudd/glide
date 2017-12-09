package com.bumptech.glide;

import static com.google.common.truth.Truth.assertThat;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.support.test.runner.AndroidJUnit4;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class BitmapReconfigureTest {

  @Test
  public void reconfigure_doesNotCrash() {
    Bitmap bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
    bitmap.reconfigure(9, 9, Bitmap.Config.ARGB_8888);
    assertThat(bitmap.getWidth()).isEqualTo(9);
    assertThat(bitmap.getHeight()).isEqualTo(9);
    assertThat(bitmap.getConfig()).isEqualTo(Config.ARGB_8888);
  }

  @Test
  public void reconfigure_withLruBitmapPoolBitmap_doesNotCrash() {
    Bitmap bitmap = new LruBitmapPool(0).getDirty(10, 10, Config.ARGB_8888);
    bitmap.reconfigure(9, 9, Bitmap.Config.ARGB_8888);
    assertThat(bitmap.getWidth()).isEqualTo(9);
    assertThat(bitmap.getHeight()).isEqualTo(9);
    assertThat(bitmap.getConfig()).isEqualTo(Config.ARGB_8888);
  }
}
