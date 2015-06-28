package com.bumptech.glide.load.resource;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

import android.app.Application;

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
import org.robolectric.annotation.Config;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 18)
public class UnitTransformationTest {

  @Mock Resource<Object> resource;
  @Mock Transformation<Object> other;
  private BitmapPool bitmapPool;
  private Application context;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    context = RuntimeEnvironment.application;
    bitmapPool = new BitmapPoolAdapter();
  }

  @Test
  public void testReturnsGivenResource() {
    UnitTransformation<Object> transformation = UnitTransformation.get();
    assertEquals(resource, transformation.transform(context, bitmapPool, resource, 10, 10));
  }

  @Test
  public void testEquals() throws NoSuchAlgorithmException {
    KeyAssertions.assertSame(UnitTransformation.get(), UnitTransformation.get());

    doAnswer(new Util.WriteDigest("other")).when(other)
        .updateDiskCacheKey(any(MessageDigest.class));
    KeyAssertions.assertDifferent(UnitTransformation.get(), other);
  }
}
