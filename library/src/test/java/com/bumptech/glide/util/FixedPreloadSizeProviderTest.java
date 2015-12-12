package com.bumptech.glide.util;

import static com.google.common.truth.Truth.assertThat;

import com.bumptech.glide.BuildConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricGradleTestRunner.class)
@Config(manifest = Config.NONE, sdk = 18, constants = BuildConfig.class)
public class FixedPreloadSizeProviderTest {

  @Test
  public void testReturnsGivenSize() {
    int width = 500;
    int height = 1234;
    FixedPreloadSizeProvider<Object> provider = new FixedPreloadSizeProvider<>(width, height);

    int[] size = provider.getPreloadSize(new Object(), 0, 0);

    assertThat(size).asList().containsExactly(width, height);
  }
}
