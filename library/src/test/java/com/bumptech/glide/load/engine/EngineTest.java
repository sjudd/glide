package com.bumptech.glide.load.engine;

import static com.bumptech.glide.tests.Util.anyResource;
import static com.bumptech.glide.tests.Util.isADataSource;
import static com.bumptech.glide.tests.Util.mockResource;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.os.MessageQueue.IdleHandler;
import com.bumptech.glide.GlideContext;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemoryCache;
import com.bumptech.glide.load.engine.executor.GlideExecutor;
import com.bumptech.glide.load.engine.executor.MockGlideExecutor;
import com.bumptech.glide.request.ResourceCallback;
import com.bumptech.glide.tests.BackgroundUtil;
import com.bumptech.glide.tests.GlideShadowLooper;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 18, shadows = { GlideShadowLooper.class })
@SuppressWarnings("unchecked")
public class EngineTest {
  private EngineTestHarness harness;

  @Before
  public void setUp() {
    harness = new EngineTestHarness();
  }

  @Test
  public void testNewRunnerIsCreatedAndPostedWithNoExistingLoad() {
    harness.doLoad();

    verify(harness.job).start(any());
  }

  @Test
  public void testCallbackIsAddedToNewEngineJobWithNoExistingLoad() {
    harness.doLoad();

    verify(harness.job).addCallback(eq(harness.cb));
  }

  @Test
  public void testLoadStatusIsReturnedForNewLoad() {
    assertNotNull(harness.doLoad());
  }

  @Test
  public void testEngineJobReceivesRemoveCallbackFromLoadStatus() {
    Engine.LoadStatus loadStatus = harness.doLoad();
    loadStatus.cancel();

    verify(harness.job).removeCallback(eq(harness.cb));
  }

  @Test
  public void testNewRunnerIsAddedToRunnersMap() {
    harness.doLoad();

    assertThat(harness.jobs).containsKey(harness.cacheKey);
  }

  @Test
  public void testNewRunnerIsNotCreatedAndPostedWithExistingLoad() {
    harness.doLoad();
    harness.doLoad();

    verify(harness.job, times(1)).start(any());
  }

  @Test
  public void testCallbackIsAddedToExistingRunnerWithExistingLoad() {
    harness.doLoad();

    ResourceCallback newCallback = mock(ResourceCallback.class);
    harness.cb = newCallback;
    harness.doLoad();

    verify(harness.job).addCallback(eq(newCallback));
  }

  @Test
  public void testLoadStatusIsReturnedForExistingJob() {
    harness.doLoad();
    Engine.LoadStatus loadStatus = harness.doLoad();

    assertNotNull(loadStatus);
  }

  @Test
  public void testResourceIsReturnedFromActiveResourcesIfPresent() {
    harness.activeResources.activate(harness.cacheKey, harness.resource);

    harness.doLoad();

    verify(harness.cb).onResourceReady(eq(harness.resource), eq(DataSource.MEMORY_CACHE));
  }

  @Test
  public void testResourceIsAcquiredIfReturnedFromActiveResources() {
    harness.activeResources.activate(harness.cacheKey, harness.resource);

    harness.doLoad();

    verify(harness.resource).acquire();
  }

  @Test
  public void testNewLoadIsNotStartedIfResourceIsActive() {
    harness.activeResources.activate(harness.cacheKey, harness.resource);

    harness.doLoad();

    verify(harness.job, never()).start(any(DecodeJob.class));
  }

  @Test
  public void testNullLoadStatusIsReturnedIfResourceIsActive() {
    harness.activeResources.activate(harness.cacheKey, harness.resource);

    assertNull(harness.doLoad());
  }

  @Test
  public void load_withResourceInActiveResources_doesNotCheckMemoryCache() {
    harness.activeResources.activate(harness.cacheKey, harness.resource);

    harness.doLoad();

    verify(harness.cb).onResourceReady(eq(harness.resource), eq(DataSource.MEMORY_CACHE));
    verify(harness.cache, never()).remove(any(Key.class));
  }

  @Test
  public void testActiveResourcesIsNotCheckedIfNotMemoryCacheable() {
    harness.activeResources.activate(harness.cacheKey, harness.resource);

    harness.isMemoryCacheable = false;
    harness.doLoad();

    verify(harness.resource, never()).acquire();
    verify(harness.job).start(any());
  }

  @Test
  public void testCacheIsCheckedIfMemoryCacheable() {
    when(harness.cache.remove(eq(harness.cacheKey))).thenReturn(harness.resource);

    harness.doLoad();

    verify(harness.cb).onResourceReady(eq(harness.resource), eq(DataSource.MEMORY_CACHE));
  }

  @Test
  public void testCacheIsNotCheckedIfNotMemoryCacheable() {
    when(harness.cache.remove(eq(harness.cacheKey))).thenReturn(harness.resource);

    harness.isMemoryCacheable = false;
    harness.doLoad();

    verify(harness.job).start(any());
  }

  @Test
  public void testResourceIsReturnedFromCacheIfPresent() {
    when(harness.cache.remove(eq(harness.cacheKey))).thenReturn(harness.resource);

    harness.doLoad();

    verify(harness.cb).onResourceReady(eq(harness.resource), eq(DataSource.MEMORY_CACHE));
  }

  @Test
  public void testHandlesNonEngineResourcesFromCacheIfPresent() {
    final Object expected = new Object();
    @SuppressWarnings("rawtypes") Resource fromCache = mockResource();
    when(fromCache.get()).thenReturn(expected);
    when(harness.cache.remove(eq(harness.cacheKey))).thenReturn(fromCache);

    doAnswer(invocationOnMock -> {
      Resource<?> resource = (Resource<?>) invocationOnMock.getArguments()[0];
      assertEquals(expected, resource.get());
      return null;
    }).when(harness.cb).onResourceReady(anyResource(), isADataSource());

    harness.doLoad();

    verify(harness.cb).onResourceReady(anyResource(), isADataSource());
  }

  @Test
  public void testResourceIsAddedToActiveResourceIfReturnedFromCache() {
    when(harness.cache.remove(eq(harness.cacheKey))).thenReturn(harness.resource);

    harness.doLoad();
    EngineResource<?> activeResource = harness.activeResources.get(harness.cacheKey);
    assertThat(activeResource).isEqualTo(harness.resource);
  }

  @Test
  public void testResourceIsAcquiredIfReturnedFromCache() {
    when(harness.cache.remove(eq(harness.cacheKey))).thenReturn(harness.resource);

    harness.doLoad();

    verify(harness.resource).acquire();
  }

  @Test
  public void testNewLoadIsNotStartedIfResourceIsCached() {
    when(harness.cache.remove(eq(harness.cacheKey))).thenReturn(harness.resource);

    harness.doLoad();

    verify(harness.job, never()).start(any(DecodeJob.class));
  }

  @Test
  public void testNullLoadStatusIsReturnedForCachedResource() {
    when(harness.cache.remove(eq(harness.cacheKey))).thenReturn(harness.resource);

    Engine.LoadStatus loadStatus = harness.doLoad();
    assertNull(loadStatus);
  }

  @Test
  public void testRunnerIsRemovedFromRunnersOnEngineNotifiedJobComplete() {
    harness.doLoad();

    harness.getEngine().onEngineJobComplete(harness.cacheKey, harness.resource);

    assertThat(harness.jobs).doesNotContainKey(harness.cacheKey);
  }

  @Test
  public void testEngineIsSetAsResourceListenerOnJobComplete() {
    harness.doLoad();

    harness.getEngine().onEngineJobComplete(harness.cacheKey, harness.resource);

    verify(harness.resource).setResourceListener(eq(harness.cacheKey), eq(harness.getEngine()));
  }

  @Test
  public void testEngineIsNotSetAsResourceListenerIfResourceIsNullOnJobComplete() {
    harness.doLoad();

    harness.getEngine().onEngineJobComplete(harness.cacheKey, null);
  }

  @Test
  public void testResourceIsAddedToActiveResourcesOnEngineComplete() {
    when(harness.resource.isCacheable()).thenReturn(true);
    harness.getEngine().onEngineJobComplete(harness.cacheKey, harness.resource);

    EngineResource<?> resource = harness.activeResources.get(harness.cacheKey);
    assertThat(harness.resource).isEqualTo(resource);
  }

  @Test
  public void testDoesNotPutNullResourceInActiveResourcesOnEngineComplete() {
    harness.getEngine().onEngineJobComplete(harness.cacheKey, null);
    assertThat(harness.activeResources.get(harness.cacheKey)).isNull();
  }

  @Test
  public void testDoesNotPutResourceThatIsNotCacheableInActiveResourcesOnEngineComplete() {
    when(harness.resource.isCacheable()).thenReturn(false);
    harness.getEngine().onEngineJobComplete(harness.cacheKey, harness.resource);
    assertThat(harness.activeResources.get(harness.cacheKey)).isNull();
  }

  @Test
  public void testRunnerIsRemovedFromRunnersOnEngineNotifiedJobCancel() {
    harness.doLoad();

    harness.getEngine().onEngineJobCancelled(harness.job, harness.cacheKey);

    assertThat(harness.jobs).doesNotContainKey(harness.cacheKey);
  }

  @Test
  public void testJobIsNotRemovedFromJobsIfOldJobIsCancelled() {
    harness.doLoad();

    harness.getEngine().onEngineJobCancelled(mock(EngineJob.class), harness.cacheKey);

    assertEquals(harness.job, harness.jobs.get(harness.cacheKey));
  }

  @Test
  public void testResourceIsAddedToCacheOnReleased() {
    final Object expected = new Object();
    when(harness.resource.isCacheable()).thenReturn(true);
    when(harness.resource.get()).thenReturn(expected);
    doAnswer(invocationOnMock -> {
      Resource<?> resource = (Resource<?>) invocationOnMock.getArguments()[1];
      assertEquals(expected, resource.get());
      return null;
    }).when(harness.cache).put(eq(harness.cacheKey), anyResource());

    harness.getEngine().onResourceReleased(harness.cacheKey, harness.resource);

    verify(harness.cache).put(eq(harness.cacheKey), anyResource());
  }

  @Test
  public void testResourceIsNotAddedToCacheOnReleasedIfNotCacheable() {
    when(harness.resource.isCacheable()).thenReturn(false);
    harness.getEngine().onResourceReleased(harness.cacheKey, harness.resource);

    verify(harness.cache, never()).put(eq(harness.cacheKey), eq(harness.resource));
  }

  @Test
  public void testResourceIsRecycledIfNotCacheableWhenReleased() {
    when(harness.resource.isCacheable()).thenReturn(false);
    harness.getEngine().onResourceReleased(harness.cacheKey, harness.resource);
    verify(harness.resourceRecycler).recycle(eq(harness.resource));
  }

  @Test
  public void testResourceIsRemovedFromActiveResourcesWhenReleased() {
    harness.activeResources.activate(harness.cacheKey, harness.resource);

    harness.getEngine().onResourceReleased(harness.cacheKey, harness.resource);

    assertThat(harness.activeResources.get(harness.cacheKey)).isNull();
  }

  @Test
  public void testEngineAddedAsListenerToMemoryCache() {
    harness.getEngine();
    verify(harness.cache).setResourceRemovedListener(eq(harness.getEngine()));
  }

  @Test
  public void testResourceIsRecycledWhenRemovedFromCache() {
    harness.getEngine().onResourceRemoved(harness.resource);
    verify(harness.resourceRecycler).recycle(eq(harness.resource));
  }

  @Test
  public void testJobIsPutInJobWithCacheKeyWithRelevantIds() {
    harness.doLoad();

    assertThat(harness.jobs).containsEntry(harness.cacheKey, harness.job);
  }

  @Test
  public void testKeyFactoryIsGivenNecessaryArguments() {
    harness.doLoad();

    verify(harness.keyFactory)
        .buildKey(eq(harness.model), eq(harness.signature), eq(harness.width), eq(harness.height),
            eq(harness.transformations), eq(Object.class), eq(Object.class), eq(harness.options));
  }

  @Test
  public void testFactoryIsGivenNecessaryArguments() {
    harness.doLoad();

    verify(harness.engineJobFactory).build(
        eq(harness.cacheKey),
        eq(true) /*isMemoryCacheable*/,
        eq(false) /*useUnlimitedSourceGeneratorPool*/,
        /*useAnimationPool=*/ eq(false));
  }

  @Test
  public void testFactoryIsGivenNecessaryArgumentsWithUnlimitedPool() {
    harness.useUnlimitedSourceGeneratorPool = true;
    harness.doLoad();

    verify(harness.engineJobFactory).build(
        eq(harness.cacheKey),
        eq(true) /*isMemoryCacheable*/,
        eq(true) /*useUnlimitedSourceGeneratorPool*/,
        /*useAnimationPool=*/ eq(false));
  }

  @Test
  public void testReleaseReleasesEngineResource() {
    EngineResource<Object> engineResource = mock(EngineResource.class);
    harness.getEngine().release(engineResource);
    verify(engineResource).release();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testThrowsIfAskedToReleaseNonEngineResource() {
    harness.getEngine().release(mockResource());
  }

  @Test(expected = RuntimeException.class)
  public void testThrowsIfLoadCalledOnBackgroundThread() throws InterruptedException {
    BackgroundUtil.testInBackground(() -> harness.doLoad());
  }

  @Test
  public void load_afterResourceIsLoadedInActiveResources_returnsFromMemoryCache() {
    when(harness.resource.isCacheable()).thenReturn(true);
    doAnswer(invocationOnMock -> {
      harness.getEngine().onEngineJobComplete(harness.cacheKey, harness.resource);
      return null;
    }).when(harness.job).start(any(DecodeJob.class));
    harness.doLoad();
    harness.doLoad();
    verify(harness.cb).onResourceReady(any(Resource.class), eq(DataSource.MEMORY_CACHE));
  }

  @Test
  public void load_afterResourceIsLoadedAndReleased_returnsFromMemoryCache() {
    harness.cache = new LruResourceCache(100);
    when(harness.resource.isCacheable()).thenReturn(true);
    doAnswer(invocationOnMock -> {
      harness.getEngine().onEngineJobComplete(harness.cacheKey, harness.resource);
      return null;
    }).when(harness.job).start(any(DecodeJob.class));
    harness.doLoad();
    harness.getEngine().onResourceReleased(harness.cacheKey, harness.resource);
    harness.doLoad();
    verify(harness.cb).onResourceReady(any(Resource.class), eq(DataSource.MEMORY_CACHE));
  }

  @Test
  public void load_afterResourceIsGcedFromActive_returnsFromMemoryCache() {
    when(harness.resource.getResource()).thenReturn(mock(Resource.class));
    when(harness.resource.isCacheable()).thenReturn(true);
    harness.cache = new LruResourceCache(100);
    doAnswer(invocationOnMock -> {
      harness.getEngine().onEngineJobComplete(harness.cacheKey, harness.resource);
      return null;
    }).when(harness.job).start(any(DecodeJob.class));
    harness.doLoad();
    ArgumentCaptor<IdleHandler> captor = ArgumentCaptor.forClass(IdleHandler.class);
    verify(GlideShadowLooper.queue).addIdleHandler(captor.capture());
    captor.getValue().queueIdle();
    harness.doLoad();
    verify(harness.cb).onResourceReady(any(Resource.class), eq(DataSource.MEMORY_CACHE));
  }

  private static class EngineTestHarness {
    final EngineKey cacheKey = mock(EngineKey.class);
    final EngineKeyFactory keyFactory = mock(EngineKeyFactory.class);
    ResourceCallback cb = mock(ResourceCallback.class);
    @SuppressWarnings("rawtypes")
    final EngineResource resource = mock(EngineResource.class);
    final Map<Key, EngineJob<?>> jobs = new HashMap<>();
    final ActiveResources activeResources = new ActiveResources();

    final int width = 100;
    final int height = 100;

    final Object model = new Object();
    MemoryCache cache = mock(MemoryCache.class);
    final EngineJob<?> job;
    private Engine engine;
    final Engine.EngineJobFactory engineJobFactory = mock(Engine.EngineJobFactory.class);
    final Engine.DecodeJobFactory decodeJobFactory = mock(Engine.DecodeJobFactory.class);
    final ResourceRecycler resourceRecycler = mock(ResourceRecycler.class);
    final Key signature = mock(Key.class);
    final Map<Class<?>, Transformation<?>> transformations = new HashMap<>();
    final Options options = new Options();
    final GlideContext glideContext = mock(GlideContext.class);
    boolean isMemoryCacheable = true;
    boolean useUnlimitedSourceGeneratorPool = false;
    final boolean onlyRetrieveFromCache = false;
    final boolean isScaleOnlyOrNoTransform = true;

    EngineTestHarness() {
      when(keyFactory.buildKey(eq(model), eq(signature), anyInt(), anyInt(), eq(transformations),
          eq(Object.class), eq(Object.class), eq(options))).thenReturn(cacheKey);
      when(resource.getResource()).thenReturn(mock(Resource.class));

      job = mock(EngineJob.class);

    }

    Engine.LoadStatus doLoad() {
      when(engineJobFactory.build(eq(cacheKey), anyBoolean(), anyBoolean(), anyBoolean()))
          .thenReturn((EngineJob<Object>) job);
      return getEngine().load(glideContext,
          model,
          signature,
          width,
          height,
          Object.class /*resourceClass*/,
          Object.class /*transcodeClass*/,
          Priority.HIGH,
          DiskCacheStrategy.ALL,
          transformations,
          false /*isTransformationRequired*/,
          isScaleOnlyOrNoTransform,
          options,
          isMemoryCacheable,
          useUnlimitedSourceGeneratorPool,
          /*useAnimationPool=*/ false,
          onlyRetrieveFromCache,
          cb);
    }

    Engine getEngine() {
      if (engine == null) {
        engine =
            new Engine(
                cache,
                mock(DiskCache.Factory.class),
                GlideExecutor.newDiskCacheExecutor(),
                MockGlideExecutor.newMainThreadExecutor(),
                MockGlideExecutor.newMainThreadUnlimitedExecutor(),
                MockGlideExecutor.newMainThreadUnlimitedExecutor(),
                jobs,
                keyFactory,
                activeResources,
                engineJobFactory,
                decodeJobFactory,
                resourceRecycler);
      }
      return engine;
    }
  }

}
