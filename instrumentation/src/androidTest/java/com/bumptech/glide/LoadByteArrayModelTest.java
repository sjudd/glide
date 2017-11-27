package com.bumptech.glide;

import static com.bumptech.glide.test.Matchers.anyBitmapTarget;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.executor.GlideExecutor;
import com.bumptech.glide.load.engine.executor.MockGlideExecutor;
import com.bumptech.glide.load.model.ByteArrayModel;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.bumptech.glide.test.BitmapSubject;
import com.bumptech.glide.test.ConcurrencyHelper;
import com.bumptech.glide.test.GlideApp;
import com.bumptech.glide.test.ResourceIds;
import com.bumptech.glide.test.ResourceUtil;
import com.bumptech.glide.test.TearDownGlide;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(AndroidJUnit4.class)
public class LoadByteArrayModelTest {
  @Rule public final TearDownGlide tearDownGlide = new TearDownGlide();
  @Mock private RequestListener<Bitmap> requestListener;
  private final ConcurrencyHelper concurrencyHelper = new ConcurrencyHelper();
  private Context context;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    context = InstrumentationRegistry.getTargetContext();
  }

  @Test
  public void load_withNullByteArrayModel_callsOnLoadFailed() {
    try {
      concurrencyHelper.get(
          Glide.with(context)
              .load((ByteArrayModel) null)
              .submit());
      fail("Expected request to throw");
    } catch (RuntimeException e) {
      // Expected.
    }
  }

  @Test
  public void load_withValidBytes_returnsValidImage() {
    byte[] data = getCanonicalBytes();

    Bitmap bitmap =
        concurrencyHelper.get(
            Glide.with(context)
                .asBitmap()
                .load(new ByteArrayModel(mock(Key.class), getCanonicalBytes()))
                .submit());

    Bitmap expected =
        BitmapFactory.decodeByteArray(data, /*offset=*/ 0, data.length);

    BitmapSubject.assertThat(bitmap).sameAs(expected);
  }

  @Test
  public void load_withValidKeyBytesAndPreviousLoadInMemory_returnsFromMemory() {
    byte[] data = getCanonicalBytes();
    Key key = new ObjectKey("data");

    concurrencyHelper.get(
        Glide.with(context)
            .asBitmap()
            .load(new ByteArrayModel(key, data))
            .submit());

    concurrencyHelper.get(
        Glide.with(context)
            .asBitmap()
            .load(new ByteArrayModel(key, data))
            .listener(requestListener)
            .submit());

    verify(requestListener)
        .onResourceReady(
            any(Bitmap.class),
            any(),
            anyBitmapTarget(),
            eq(DataSource.MEMORY_CACHE),
            anyBoolean());
  }

  @Test
  public void load_withValidKeyBytesAndPreviousLoadInResourceCache_returnsFromResourceCache() {
    // Resource cache encodes happen after requests complete and Targets are notified. If we
    // allow a default number of threads, it's possible our second request in this test will check
    // the resource disk cache before the first request finishes writing it.
    ExecutorService executor = Executors.newFixedThreadPool(1);
    GlideExecutor glideExecutor = MockGlideExecutor.newTestExecutor(executor);
    Glide.init(
        context,
        new GlideBuilder()
            .setDiskCacheExecutor(glideExecutor)
            .setSourceExecutor(glideExecutor)
            .setAnimationExecutor(glideExecutor));

    byte[] data = getCanonicalBytes();
    Key key = new ObjectKey("data");

    Target<Bitmap> target =
        concurrencyHelper.wait(
            GlideApp.with(context)
                .asBitmap()
                .load(new ByteArrayModel(key, data))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .submit());

    Glide.with(context).clear(target);
    concurrencyHelper.runOnMainThread(new Runnable() {
      @Override
      public void run() {
        Glide.get(context).clearMemory();
      }
    });

    concurrencyHelper.get(
        GlideApp.with(context)
            .asBitmap()
            .load(new ByteArrayModel(key, data))
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .listener(requestListener)
            .submit());

    verify(requestListener)
        .onResourceReady(
            any(Bitmap.class),
            any(),
            anyBitmapTarget(),
            eq(DataSource.RESOURCE_DISK_CACHE),
            anyBoolean());
  }

  @Test
  public void load_withValidKeyBytesAndPreviousLoadInDataCache_returnsFromDataCache() {
    byte[] data = getCanonicalBytes();
    Key key = new ObjectKey("data");

    Target<Bitmap> target =
        concurrencyHelper.wait(
            GlideApp.with(context)
                .asBitmap()
                .load(new ByteArrayModel(key, data))
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .submit());

    Glide.with(context).clear(target);
    concurrencyHelper.runOnMainThread(new Runnable() {
      @Override
      public void run() {
        Glide.get(context).clearMemory();
      }
    });

    concurrencyHelper.get(
        GlideApp.with(context)
            .asBitmap()
            .load(new ByteArrayModel(key, data))
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .listener(requestListener)
            .submit());

    verify(requestListener)
        .onResourceReady(
            any(Bitmap.class),
            any(),
            anyBitmapTarget(),
            eq(DataSource.DATA_DISK_CACHE),
            anyBoolean());
  }

  private byte[] getCanonicalBytes() {
    return ResourceUtil.loadData(ResourceIds.raw.canonical);
  }
}
