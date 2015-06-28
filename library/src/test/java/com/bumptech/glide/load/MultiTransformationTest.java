package com.bumptech.glide.load;

import static com.bumptech.glide.tests.Util.isABitmapPool;
import static com.bumptech.glide.tests.Util.isAContext;
import static com.bumptech.glide.tests.Util.isAResource;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Application;

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
import org.robolectric.annotation.Config;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 18)
public class MultiTransformationTest {

  @Mock Transformation<Object> first;
  @Mock Transformation<Object> second;
  @Mock Resource<Object> initial;
  @Mock Resource<Object> firstTransformed;
  @Mock Resource<Object> secondTransformed;
  private Application context;
  private BitmapPool bitmapPool;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    context = RuntimeEnvironment.application;
    bitmapPool = new BitmapPoolAdapter();
  }

  @Test
  public void testAppliesTransformationsInOrder() {
    final int width = 584;
    final int height = 768;

    MultiTransformation<Object> transformation = new MultiTransformation<>(first, second);
    when(first.transform(isAContext(), isABitmapPool(), eq(initial), eq(width), eq(height)))
        .thenReturn(firstTransformed);

    when(second.transform(isAContext(), isABitmapPool(), eq(firstTransformed), eq(width),
        eq(height)))
        .thenReturn(secondTransformed);

    assertEquals(secondTransformed, transformation.transform(context, bitmapPool, initial, width,
        height));
  }

  @Test
  public void testInitialResourceIsNotRecycled() {
    when(first.transform(isAContext(), isABitmapPool(), isAResource(), anyInt(), anyInt()))
        .thenReturn(firstTransformed);

    MultiTransformation<Object> transformation = new MultiTransformation<>(first);

    transformation.transform(context, bitmapPool, initial, 123, 456);

    verify(initial, never()).recycle();
  }

  @Test
  public void testInitialResourceIsNotRecycledEvenIfReturnedByMultipleTransformations() {
    when(first.transform(isAContext(), isABitmapPool(), isAResource(), anyInt(), anyInt()))
        .thenReturn(initial);
    when(second.transform(isAContext(), isABitmapPool(), isAResource(), anyInt(), anyInt()))
        .thenReturn(initial);

    MultiTransformation<Object> transformation = new MultiTransformation<>(first, second);
    transformation.transform(context, bitmapPool, initial, 1111, 2222);

    verify(initial, never()).recycle();
  }

  @Test
  public void
  testInitialResourceIsNotRecycledIfReturnedByOneTransformationButNotByALaterTransformation() {
    when(first.transform(isAContext(), isABitmapPool(), isAResource(), anyInt(), anyInt()))
        .thenReturn(initial);
    when(second.transform(isAContext(), isABitmapPool(), isAResource(), anyInt(), anyInt()))
        .thenReturn(secondTransformed);

    MultiTransformation<Object> transformation = new MultiTransformation<>(first, second);
    transformation.transform(context, bitmapPool, initial, 1, 2);

    verify(initial, never()).recycle();
  }

  @Test
  public void testFinalResourceIsNotRecycled() {
    when(first.transform(isAContext(), isABitmapPool(), isAResource(), anyInt(), anyInt()))
        .thenReturn(firstTransformed);

    MultiTransformation<Object> transformation = new MultiTransformation<>(first);

    transformation.transform(context, bitmapPool, initial, 111, 222);

    verify(firstTransformed, never()).recycle();
  }

  @Test
  public void testIntermediateResourcesAreRecycled() {
    when(first.transform(isAContext(), isABitmapPool(), isAResource(), anyInt(), anyInt()))
        .thenReturn(firstTransformed);
    when(second.transform(isAContext(), isABitmapPool(), isAResource(), anyInt(), anyInt()))
        .thenReturn(secondTransformed);

    MultiTransformation<Object> transformation = new MultiTransformation<>(first, second);

    transformation.transform(context, bitmapPool, initial, 233, 454);

    verify(firstTransformed).recycle();
  }

  @Test
  public void testEquals() throws NoSuchAlgorithmException {
    doAnswer(new Util.WriteDigest("first")).when(first)
        .updateDiskCacheKey(any(MessageDigest.class));
    KeyAssertions.assertSame(new MultiTransformation<>(first), new MultiTransformation<>(first));

    doAnswer(new Util.WriteDigest("second")).when(second)
        .updateDiskCacheKey(any(MessageDigest.class));
    KeyAssertions.assertDifferent(new MultiTransformation<>(first),
        new MultiTransformation<>(second));
  }
}
