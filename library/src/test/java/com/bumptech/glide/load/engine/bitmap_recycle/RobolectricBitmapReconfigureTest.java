package com.bumptech.glide.load.engine.bitmap_recycle;


import static org.junit.Assert.assertThrows;

import android.graphics.Bitmap;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 19)
public class RobolectricBitmapReconfigureTest {

  @Test
  public void reconfigure_crashes() {
    assertThrows(IllegalStateException.class, new ThrowingRunnable() {
      @Override
      public void run() throws Throwable {
        Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
            .reconfigure(9, 9, Bitmap.Config.ARGB_8888);
      }
    });
  }
}
