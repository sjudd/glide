package com.bumptech.glide.load.model;


import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import com.bumptech.glide.load.Key;
import com.bumptech.glide.tests.KeyTester;
import com.bumptech.glide.tests.Util;
import java.security.MessageDigest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public class ByteArrayModelTest {
  @Rule public final KeyTester keyTester = new KeyTester();

  private final byte[] data = new byte[0];
  @Mock private Key key;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void constructor_withNullKey_throws() {
    assertThrows(NullPointerException.class, new ThrowingRunnable() {
      @SuppressWarnings("ConstantConditions")
      @Override
      public void run() throws Throwable {
        new ByteArrayModel(/*key=*/ null, new byte[0]);
      }
    });
  }

  @Test
  public void constructor_withNullData_throws() {
    assertThrows(NullPointerException.class, new ThrowingRunnable() {
      @SuppressWarnings("ConstantConditions")
      @Override
      public void run() throws Throwable {
        new ByteArrayModel(key, /*data=*/ null);
      }
    });
  }

  @Test
  public void constructor_withNullDataAndKey_throws() {
    assertThrows(NullPointerException.class, new ThrowingRunnable() {
      @SuppressWarnings("ConstantConditions")
      @Override
      public void run() throws Throwable {
        new ByteArrayModel(/*key=*/ null, /*data=*/ null);
      }
    });
  }

  @Test
  public void testEquals() {
    Key otherKey = mock(Key.class);

    doAnswer(new Util.WriteDigest("key"))
        .when(key).updateDiskCacheKey(any(MessageDigest.class));
    doAnswer(new Util.WriteDigest("otherKey"))
        .when(otherKey).updateDiskCacheKey(any(MessageDigest.class));

    keyTester
        .addEquivalenceGroup(
            new ByteArrayModel(key, data),
            new ByteArrayModel(key, data),
            new ByteArrayModel(key, new byte[10]),
            new ByteArrayModel(key, new byte[1]))
        .addEquivalenceGroup(
            new ByteArrayModel(otherKey, new byte[0]),
            new ByteArrayModel(otherKey, new byte[1]),
            new ByteArrayModel(otherKey, data))
        .addRegressionTest(
            new ByteArrayModel(key, new byte[0]),
            "2c70e12b7a0646f92279f427c7b38e7334d8e5389cff167a1dc30e73f826b683")
        .addRegressionTest(
            new ByteArrayModel(otherKey, new byte[1]),
            "4299d48c5b870b6000903911f3c446436346a405787077cd30200b97ac447ea5")
        .test();
  }
}
