package com.bumptech.glide.load.resource.bitmap;

import static com.bumptech.glide.tests.Util.isABitmapConfig;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Application;
import android.graphics.Bitmap;

import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPoolAdapter;
import com.bumptech.glide.tests.KeyAssertions;
import com.bumptech.glide.tests.Util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 18)
public class CenterCropTest {
  @Mock Resource<Bitmap> resource;
  @Mock Transformation<Bitmap> transformation;

  private CenterCrop centerCrop;
  private int bitmapWidth;
  private int bitmapHeight;
  private Bitmap bitmap;
  private BitmapPool pool;
  private Application context;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    bitmapWidth = 100;
    bitmapHeight = 100;
    bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
    when(resource.get()).thenReturn(bitmap);

    context = RuntimeEnvironment.application;
    pool = new BitmapPoolAdapter();

    centerCrop = CenterCrop.get();
  }

  @Test
  public void testDoesNotPutNullBitmapAcquiredFromPool() {
    BitmapPool bitmapPool = mock(BitmapPool.class);
    when(bitmapPool.get(anyInt(), anyInt(), isABitmapConfig()))
        .thenAnswer(new Util.CreateBitmap());
    when(bitmapPool.get(anyInt(), anyInt(), any(Bitmap.Config.class))).thenReturn(null);

    centerCrop.transform(context, bitmapPool, resource, 100, 100);

    verify(bitmapPool, never()).put(any(Bitmap.class));
  }

  @Test
  public void testReturnsGivenResourceIfMatchesSizeExactly() {
    Resource<Bitmap> result =
        centerCrop.transform(context, pool, resource, bitmapWidth, bitmapHeight);

    assertEquals(resource, result);
  }

  @Test
  public void testDoesNotRecycleGivenResourceIfMatchesSizeExactly() {
    centerCrop.transform(context, pool, resource, bitmapWidth, bitmapHeight);

    verify(resource, never()).recycle();
  }

  @Test
  public void testDoesNotRecycleGivenResource() {
    centerCrop.transform(context, pool, resource, 50, 50);

    verify(resource, never()).recycle();
  }

  @Test
  public void testAsksBitmapPoolForArgb8888IfInConfigIsNull() {
    Shadows.shadowOf(bitmap).setConfig(null);
    BitmapPool bitmapPool = mock(BitmapPool.class);
    when(bitmapPool.get(anyInt(), anyInt(), isABitmapConfig()))
        .thenAnswer(new Util.CreateBitmap());

    centerCrop.transform(context, bitmapPool, resource, 10, 10);

    verify(bitmapPool).get(anyInt(), anyInt(), eq(Bitmap.Config.ARGB_8888));
    verify(bitmapPool, never()).get(anyInt(), anyInt(), (Bitmap.Config) isNull());
  }

  @Test
  public void testReturnsBitmapWithExactlyGivenDimensionsIfBitmapIsLargerThanTarget() {
    int expectedWidth = 75;
    int expectedHeight = 74;

    for (int[] dimens : new int[][] { new int[] { 800, 200 }, new int[] { 450, 100 },
        new int[] { 78, 78 } }) {
      Bitmap toTransform = Bitmap.createBitmap(dimens[0], dimens[1], Bitmap.Config.ARGB_4444);
      when(resource.get()).thenReturn(toTransform);

      Resource<Bitmap> result =
          centerCrop.transform(context, pool, resource, expectedWidth, expectedHeight);
      Bitmap transformed = result.get();
      assertEquals(expectedWidth, transformed.getWidth());
      assertEquals(expectedHeight, transformed.getHeight());
    }
  }

  @Test
  public void testReturnsBitmapWithExactlyGivenDimensionsIfBitmapIsSmallerThanTarget() {
    int expectedWidth = 100;
    int expectedHeight = 100;

    for (int[] dimens : new int[][] { new int[] { 50, 90 }, new int[] { 150, 2 },
        new int[] { 78, 78 } }) {
      Bitmap toTransform = Bitmap.createBitmap(dimens[0], dimens[1], Bitmap.Config.ARGB_4444);
      when(resource.get()).thenReturn(toTransform);

      Resource<Bitmap> result =
          centerCrop.transform(context, pool, resource, expectedWidth, expectedHeight);
      Bitmap transformed = result.get();
      assertEquals(expectedWidth, transformed.getWidth());
      assertEquals(expectedHeight, transformed.getHeight());
    }
  }

  @Test
  public void testEquals() throws NoSuchAlgorithmException {
    KeyAssertions.assertSame(centerCrop, CenterCrop.get());

    doAnswer(new Util.WriteDigest("other")).when(transformation)
        .updateDiskCacheKey(any(MessageDigest.class));
    KeyAssertions.assertDifferent(centerCrop, transformation);
  }
}
