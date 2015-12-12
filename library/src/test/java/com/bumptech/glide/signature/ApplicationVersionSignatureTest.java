package com.bumptech.glide.signature;

import static org.junit.Assert.assertNotNull;

import com.bumptech.glide.BuildConfig;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.tests.KeyAssertions;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

@RunWith(RobolectricGradleTestRunner.class)
@Config(manifest = Config.NONE, sdk = 18, constants = BuildConfig.class)
public class ApplicationVersionSignatureTest {

  @After
  public void tearDown() {
    ApplicationVersionSignature.reset();
  }

  @Test
  public void testCanGetKeyForSignature() {
    Key key = ApplicationVersionSignature.obtain(RuntimeEnvironment.application);
    assertNotNull(key);
  }

  @Test
  public void testKeyForSignatureIsTheSameAcrossCallsInTheSamePackage()
      throws NoSuchAlgorithmException, UnsupportedEncodingException {
    Key first = ApplicationVersionSignature.obtain(RuntimeEnvironment.application);
    Key second = ApplicationVersionSignature.obtain(RuntimeEnvironment.application);
    KeyAssertions.assertSame(first, second);
  }
}
