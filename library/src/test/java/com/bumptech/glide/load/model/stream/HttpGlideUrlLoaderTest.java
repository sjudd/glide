package com.bumptech.glide.load.model.stream;

import static com.google.common.truth.Truth.assertThat;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.data.HttpUrlFetcher;
import com.bumptech.glide.load.model.GlideUrl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.InputStream;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 18)
public class HttpGlideUrlLoaderTest {
  private HttpGlideUrlLoader loader;
  private GlideUrl model;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    loader = new HttpGlideUrlLoader();
    model = GlideUrl.obtain("www.google.com");
  }

  @Test
  public void testReturnsValidFetcher() {
    DataFetcher<InputStream> result = loader.buildLoadData(model, 100, 100, new Options()).fetcher;
    assertThat(result).isInstanceOf(HttpUrlFetcher.class);
  }
}
