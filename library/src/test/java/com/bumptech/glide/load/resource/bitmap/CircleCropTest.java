package com.bumptech.glide.load.resource.bitmap;

import static com.bumptech.glide.tests.Util.isABitmapConfig;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.annotation.TargetApi;
import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPoolAdapter;
import com.bumptech.glide.tests.Util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class CircleCropTest {

  private BitmapPool bitmapPool;
  private CircleCrop circleCrop;
  private Application context;

  @Before
  public void setup() {
    bitmapPool = new BitmapPoolAdapter();
    circleCrop = CircleCrop.get();
    context = RuntimeEnvironment.application;
  }

  @Test
  public void testTransform_withSquare() {
    Bitmap redSquare = createSolidRedBitmap(50, 50);
    Bitmap result = circleCrop.transform(context, bitmapPool, redSquare, 50, 50);
    Bitmap expected = createBitmapWithRedCircle(50, 50);

    assertSamePixels(expected, result);
  }

  @Test
  public void testTransform_reusesBitmap() {
    BitmapPool bitmapPool = mock(BitmapPool.class);
    when(bitmapPool.get(anyInt(), anyInt(), isABitmapConfig()))
        .thenAnswer(new Util.CreateBitmap());
    Bitmap toReuse = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
    when(bitmapPool.get(50, 50, Bitmap.Config.ARGB_8888)).thenReturn(toReuse);

    Bitmap redSquare = createSolidRedBitmap(50, 50);
    Bitmap result = circleCrop.transform(context, bitmapPool, redSquare, 50, 50);

    assertEquals(toReuse, result);
  }

  @Test
  public void testTransform_withWideRectangle() {
    Bitmap redWideRectangle = createSolidRedBitmap(100, 50);
    Bitmap result = circleCrop.transform(context, bitmapPool, redWideRectangle, 80, 50);
    Bitmap expected = createBitmapWithRedCircle(80, 50);

    assertSamePixels(expected, result);
  }

  @Test
  public void testTransform_withNarrowRectangle() {
    Bitmap redNarrowRectangle = createSolidRedBitmap(20, 50);
    Bitmap result = circleCrop.transform(context, bitmapPool, redNarrowRectangle, 40, 80);
    Bitmap expected = createBitmapWithRedCircle(40, 80);

    assertSamePixels(expected, result);
  }

  private void assertSamePixels(Bitmap expected, Bitmap actual) {
    assertEquals(expected.getWidth(), actual.getWidth());
    assertEquals(expected.getHeight(), actual.getHeight());
    assertEquals(expected.getConfig(), actual.getConfig());
    for (int y = 0; y < expected.getHeight(); y++) {
      for (int x = 0; x < expected.getWidth(); x++) {
        assertEquals(expected.getPixel(x, y), actual.getPixel(x, y));
      }
    }
  }

  @TargetApi(12)
  private Bitmap createBitmapWithRedCircle(int width, int height) {
    Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    result.setHasAlpha(true);
    Canvas canvas = new Canvas(result);
    Paint paint = new Paint();
    paint.setColor(Color.RED);

    int minEdge = Math.min(width, height);
    float radius = minEdge / 2f;
    int left = (width - minEdge) / 2;
    int top = (height - minEdge) / 2;

    canvas.drawCircle(left + radius, top + radius, radius, paint);
    return result;
  }

  private Bitmap createSolidRedBitmap(int width, int height) {
    Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(result);
    Paint paint = new Paint();
    paint.setColor(Color.RED);
    Rect rect = new Rect(0, 0, width, height);
    canvas.drawRect(rect, paint);
    return result;
  }
}
