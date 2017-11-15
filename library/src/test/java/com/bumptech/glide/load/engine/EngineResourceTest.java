package com.bumptech.glide.load.engine;

import static com.bumptech.glide.tests.Util.mockResource;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bumptech.glide.load.Key;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 18)
public class EngineResourceTest {
  private EngineResource<Object> engineResource;
  private EngineResource.ResourceListener listener;
  private Key cacheKey = mock(Key.class);
  private Resource<Object> resource = mockResource();

  @Before
  public void setUp() {
    resource = mockResource();
    engineResource =
        new EngineResource<>(resource, /*isCacheable=*/ true, /*isRecyclable=*/ true);
    listener = mock(EngineResource.ResourceListener.class);
    engineResource.setResourceListener(cacheKey, listener);
  }

  @Test
  public void testCanAcquireAndRelease() {
    engineResource.acquire();
    engineResource.release();

    verify(listener).onResourceReleased(cacheKey, engineResource);
  }

  @Test
  public void testCanAcquireMultipleTimesAndRelease() {
    engineResource.acquire();
    engineResource.acquire();
    engineResource.release();
    engineResource.release();

    verify(listener).onResourceReleased(eq(cacheKey), eq(engineResource));
  }

  @Test
  public void testDelegatesGetToWrappedResource() {
    Object expected = new Object();
    when(resource.get()).thenReturn(expected);
    assertEquals(expected, engineResource.get());
  }

  @Test
  public void testDelegatesGetSizeToWrappedResource() {
    int expectedSize = 1234;
    when(resource.getSize()).thenReturn(expectedSize);
    engineResource = new EngineResource<>(resource, /*isCacheable=*/ true, /*isRecyclable=*/ true);
    assertEquals(expectedSize, engineResource.getSize());
  }

  @Test
  public void testRecyclesWrappedResourceWhenRecycled() {
    engineResource.acquire();
    engineResource.release();
    engineResource.recycle();
    verify(resource).recycle();
  }

  @Test(expected = IllegalStateException.class)
  public void testThrowsIfRecycledTwice() {
    engineResource.recycle();
    engineResource.recycle();
  }

  @Test(expected = IllegalStateException.class)
  public void testThrowsIfReleasedBeforeAcquired() {
    engineResource.release();
  }

  @Test(expected = IllegalStateException.class)
  public void testThrowsIfRecycledWhileAcquired() {
    engineResource.acquire();
    engineResource.recycle();
  }

  @Test(expected = IllegalStateException.class)
  public void testThrowsIfAcquiredAfterRecycled() {
    engineResource.recycle();
    engineResource.acquire();
  }

  @Test
  public void testThrowsIfAcquiredOnBackgroundThread() throws InterruptedException {
    Thread otherThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          engineResource.acquire();
        } catch (IllegalThreadStateException e) {
          return;
        }
        fail("Failed to receive expected IllegalThreadStateException");
      }
    });
    otherThread.start();
    otherThread.join();
  }

  @Test
  public void testThrowsIfReleasedOnBackgroundThread() throws InterruptedException {
    engineResource.acquire();
    Thread otherThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          engineResource.release();
        } catch (IllegalThreadStateException e) {
          return;
        }
        fail("Failed to receive expected IllegalThreadStateException");
      }
    });
    otherThread.start();
    otherThread.join();
  }

  @Test(expected = IllegalStateException.class)
  public void testThrowsIfReleasedMoreThanAcquired() {
    engineResource.acquire();
    engineResource.release();
    engineResource.release();
  }

  @Test(expected = NullPointerException.class)
  public void testThrowsIfWrappedResourceIsNull() {
    new EngineResource<>(/*toWrap=*/ null, /*isCacheable=*/ false, /*isRecyclable=*/ true);
  }

  @Test
  public void testCanSetAndGetIsCacheable() {
    engineResource =
        new EngineResource<>(mockResource(), /*isCacheable=*/ true, /*isRecyclable=*/ true);
    assertTrue(engineResource.isCacheable());
    engineResource =
        new EngineResource<>(mockResource(), /*isCacheable=*/ false, /*isRecyclable=*/ true);
    assertFalse(engineResource.isCacheable());
  }

  @Test
  public void release_whenNotRecycleable_doesNotRecycleResource() {
    resource = mockResource();
    engineResource = new EngineResource<>(resource, /*isCacheable=*/ true, /*isRecyclable=*/ false);
    engineResource.setResourceListener(cacheKey, listener);
    engineResource.recycle();

    verify(listener, never()).onResourceReleased(any(Key.class), any(EngineResource.class));
    verify(resource, never()).recycle();
  }

  @Test
  public void getSize_whenUnderlyingResourceSizeChanges_doesNotChange() {
    resource = mockResource();
    when(resource.getSize()).thenReturn(100);
    engineResource = new EngineResource<>(resource, /*isCacheable=*/ true, /*isRecyclable=*/ true);
    assertThat(engineResource.getSize()).isEqualTo(100);
    when(resource.getSize()).thenReturn(50);
    assertThat(engineResource.getSize()).isEqualTo(100);
  }
}
