package com.bumptech.glide.load.model;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import com.google.common.testing.EqualsTester;

import android.net.Uri;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.net.MalformedURLException;
import java.net.URL;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 18)
public class GlideUrlTest {

  @Test
  public void testReturnsNullIfGivenURLIsNull() {
    assertThat(GlideUrl.obtain((URL) null)).isNull();
  }

  @Test
  public void testReturnsNullIfGivenStringUrlIsNull() {
    assertThat(GlideUrl.obtain((String) null)).isNull();
  }

  @Test
  public void testReturnsNullIfGivenUriIsNull() {
    assertThat(GlideUrl.obtain((Uri) null)).isNull();
  }

  @Test
  public void testReturnsNullIfGivenStringURLIsEmpty() {
    assertThat(GlideUrl.obtain("")).isNull();
  }

  @Test
  public void testCanCompareGlideUrlsCreatedWithDifferentTypes() throws MalformedURLException {
    String stringUrl = "http://www.google.com";
    URL url = new URL(stringUrl);

    assertEquals(GlideUrl.obtain(stringUrl), GlideUrl.obtain(url));
  }

  @Test
  public void testCanCompareHashcodeOfGlideUrlsCreatedWithDifferentTypes()
      throws MalformedURLException {
    String stringUrl = "http://nytimes.com";
    URL url = new URL(stringUrl);

    assertEquals(GlideUrl.obtain(stringUrl).hashCode(), GlideUrl.obtain(url).hashCode());
  }

  @Test
  public void testProducesEquivalentUrlFromString() throws MalformedURLException {
    String stringUrl = "http://www.google.com";
    GlideUrl glideUrl = GlideUrl.obtain(stringUrl);

    URL url = glideUrl.toURL();
    assertEquals(stringUrl, url.toString());
  }

  @Test
  public void testProducesEquivalentStringFromURL() throws MalformedURLException {
    String expected = "http://www.washingtonpost.com";
    URL url = new URL(expected);
    GlideUrl glideUrl = GlideUrl.obtain(url);

    assertEquals(expected, glideUrl.toStringUrl());
  }

  @Test
  public void testIssue133() throws MalformedURLException {
    // u00e0=à
    final String original = "http://www.commitstrip.com/wp-content/uploads/2014/07/"
        + "Excel-\u00E0-toutes-les-sauces-650-finalenglish.jpg";

    final String escaped = "http://www.commitstrip.com/wp-content/uploads/2014/07/"
        + "Excel-%C3%A0-toutes-les-sauces-650-finalenglish.jpg";

    GlideUrl glideUrlFromString = GlideUrl.obtain(original);
    assertEquals(escaped, glideUrlFromString.toURL().toString());

    GlideUrl glideUrlFromEscapedString = GlideUrl.obtain(escaped);
    assertEquals(escaped, glideUrlFromEscapedString.toURL().toString());

    GlideUrl glideUrlFromUrl = GlideUrl.obtain(new URL(original));
    assertEquals(escaped, glideUrlFromUrl.toURL().toString());

    GlideUrl glideUrlFromEscapedUrl = GlideUrl.obtain(new URL(escaped));
    assertEquals(escaped, glideUrlFromEscapedUrl.toURL().toString());
  }

  @Test
  public void testEquals() throws MalformedURLException {
    Headers headers = mock(Headers.class);
    Headers otherHeaders = mock(Headers.class);
    String url = "http://www.google.com";
    String otherUrl = "http://mail.google.com";
    new EqualsTester()
        .addEqualityGroup(
            GlideUrl.obtain(url),
            GlideUrl.obtain(url),
            GlideUrl.obtain(new URL(url)),
            GlideUrl.obtain(new URL(url))
        )
        .addEqualityGroup(
            GlideUrl.obtain(otherUrl),
            GlideUrl.obtain(new URL(otherUrl))
        )
        .addEqualityGroup(
            GlideUrl.obtain(url, headers),
            GlideUrl.obtain(new URL(url), headers)
        )
        .addEqualityGroup(
            GlideUrl.obtain(url, otherHeaders),
            GlideUrl.obtain(new URL(url), otherHeaders)
        ).testEquals();
  }
}
