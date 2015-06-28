package com.bumptech.glide;

import android.annotation.TargetApi;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.Engine;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.engine.bitmap_recycle.ByteArrayPool;
import com.bumptech.glide.load.engine.cache.MemoryCache;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTargetFactory;
import com.bumptech.glide.request.target.Target;

/**
 * Global context for all loads in Glide containing and exposing the various registries and classes
 * required to load resources.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class GlideContext extends ContextWrapper implements ComponentCallbacks2 {
  private final Handler mainHandler;
  private final Registry registry;
  private final ImageViewTargetFactory imageViewTargetFactory;
  private final RequestOptions defaultRequestOptions;
  private final Engine engine;
  private final int logLevel;
  private final BitmapPool bitmapPool;
  private final ByteArrayPool byteArrayPool;
  private final MemoryCache memoryCache;

  GlideContext(Context context, Registry registry,
      ImageViewTargetFactory imageViewTargetFactory, RequestOptions defaultRequestOptions,
      Engine engine, int logLevel, BitmapPool bitmapPool,
      ByteArrayPool byteArrayPool, MemoryCache memoryCache) {
    super(context.getApplicationContext());
    this.registry = registry;
    this.imageViewTargetFactory = imageViewTargetFactory;
    this.defaultRequestOptions = defaultRequestOptions;
    this.engine = engine;
    this.logLevel = logLevel;
    this.bitmapPool = bitmapPool;
    this.byteArrayPool = byteArrayPool;
    this.memoryCache = memoryCache;

    mainHandler = new Handler(Looper.getMainLooper());
  }

  public RequestOptions getDefaultRequestOptions() {
    return defaultRequestOptions;
  }

  public <X> Target<X> buildImageViewTarget(ImageView imageView, Class<X> transcodeClass) {
    return imageViewTargetFactory.buildTarget(imageView, transcodeClass);
  }

  public Handler getMainHandler() {
    return mainHandler;
  }

  public Engine getEngine() {
    return engine;
  }

  public Registry getRegistry() {
    return registry;
  }

  public int getLogLevel() {
    return logLevel;
  }

  public BitmapPool getBitmapPool() {
    return bitmapPool;
  }

  public ByteArrayPool getByteArrayPool() {
    return byteArrayPool;
  }

  void setMemorySizeMultiplier(float multiplier) {
    memoryCache.setSizeMultiplier(multiplier);
    bitmapPool.setSizeMultiplier(multiplier);
  }

  void clearMemory() {
    memoryCache.clearMemory();
    bitmapPool.clearMemory();
    byteArrayPool.clearMemory();
  }

  @Override
  public void onTrimMemory(int level) {
    memoryCache.trimMemory(level);
    bitmapPool.trimMemory(level);
    byteArrayPool.trimMemory(level);
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    // Do nothing.
  }

  @Override
  public void onLowMemory() {
    clearMemory();
  }
}
