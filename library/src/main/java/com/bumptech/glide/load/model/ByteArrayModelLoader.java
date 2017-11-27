package com.bumptech.glide.load.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.util.Preconditions;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Loads {@link ByteArrayModel}s with coherent disk cache keys based on the {@link Key} provided
 * to the {@link ByteArrayModel} by delegating the loading process to a wrapped {@link ModelLoader}
 * for {@code byte[]}.
 *
 * @param <Data> the type of data that will be loaded from a given {@code byte[]}.
 */
public final class ByteArrayModelLoader<Data> implements ModelLoader<ByteArrayModel, Data> {

  private final ModelLoader<byte[], Data> wrapped;

  ByteArrayModelLoader(@NonNull ModelLoader<byte[], Data> wrapped) {
    this.wrapped = Preconditions.checkNotNull(wrapped);
  }

  @Nullable
  @Override
  public LoadData<Data> buildLoadData(
      ByteArrayModel byteArrayModel, int width, int height, Options options) {
    LoadData<Data> data = wrapped.buildLoadData(byteArrayModel.data, width, height, options);
    if (data == null) {
      return null;
    }
    return new LoadData<>(byteArrayModel.key, data.fetcher);
  }

  @Override
  public boolean handles(ByteArrayModel byteArrayModel) {
    return wrapped.handles(byteArrayModel.data);
  }

  /**
   * Builds {@link ByteArrayModelLoader}s for {@link InputStream}s.
   */
  public static final class StreamFactory
      implements ModelLoaderFactory<ByteArrayModel, InputStream> {

    @Override
    public ModelLoader<ByteArrayModel, InputStream> build(MultiModelLoaderFactory multiFactory) {
      return new ByteArrayModelLoader<>(multiFactory.build(byte[].class, InputStream.class));
    }

    @Override
    public void teardown() {
      // Do nothing.
    }
  }

  /**
   * Builds {@link ByteArrayModelLoader}s for {@link ByteBuffer}s.
   */
  public static final class ByteBufferFactory
      implements ModelLoaderFactory<ByteArrayModel, ByteBuffer> {

    @Override
    public ModelLoader<ByteArrayModel, ByteBuffer> build(MultiModelLoaderFactory multiFactory) {
      return new ByteArrayModelLoader<>(multiFactory.build(byte[].class, ByteBuffer.class));
    }

    @Override
    public void teardown() {
      // Do nothing.
    }
  }
}
