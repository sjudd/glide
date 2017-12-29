package com.bumptech.glide.test;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.test.FailModelLoader.FailModel;
import java.io.InputStream;


/**
 * Allows callers to force a failure when loading an object.
 */
public final class FailModelLoader<Model, Data> implements ModelLoader<FailModel<Model>, Data> {

  private final ModelLoader<Model, Data> wrapped;

  private FailModelLoader(ModelLoader<Model, Data> wrapped) {
    this.wrapped = wrapped;
  }

  @Nullable
  @Override
  public LoadData<Data> buildLoadData(
      @NonNull FailModel<Model> failModel, int width, int height, @NonNull Options options) {
    LoadData<Data> wrappedLoadData = wrapped
        .buildLoadData(failModel.wrapped, width, height, options);
    if (wrappedLoadData == null) {
      return null;
    }
    return new LoadData<>(
        wrappedLoadData.sourceKey, new FailFetcher<>(wrappedLoadData.fetcher, failModel.toThrow));
  }

  @Override
  public boolean handles(@NonNull FailModel<Model> failModel) {
    return wrapped.handles(failModel.wrapped);
  }

  public static final class FailModel<Model> {
    private final Model wrapped;
    private final RuntimeException toThrow;

    FailModel(Model wrapped, RuntimeException toThrow) {
      this.wrapped = wrapped;
      this.toThrow = toThrow;
    }
  }

  public static final class Factory<Model, Data>
      implements ModelLoaderFactory<FailModel<Model>, Data> {

    private final Class<Model> modelClass;
    private final Class<Data> dataClass;

    Factory(Class<Model> modelClass, Class<Data> dataClass) {
      this.modelClass = modelClass;
      this.dataClass = dataClass;
    }

    public static synchronized <T> FailModel<T> failWith(T model) {
      return failWith(model, new RuntimeException());
    }

    public static synchronized <T> FailModel<T> failWith(T model, RuntimeException toThrow) {
      @SuppressWarnings("unchecked") ModelLoaderFactory<FailModel<T>, InputStream> streamFactory =
          new Factory<>((Class<T>) model.getClass(), InputStream.class);
      Glide.get(InstrumentationRegistry.getTargetContext())
          .getRegistry()
          .replace(FailModel.class, InputStream.class, streamFactory);

      return new FailModel<>(model, toThrow);
    }

    @NonNull
    @Override
    public ModelLoader<FailModel<Model>, Data> build(
        @NonNull MultiModelLoaderFactory multiFactory) {
      return new FailModelLoader<>(multiFactory.build(modelClass, dataClass));
    }

    @Override
    public void teardown() {
      // Do nothing.
    }
  }

  private static final class FailFetcher<Data> implements DataFetcher<Data> {

    private final DataFetcher<Data> wrapped;
    private RuntimeException toThrow;

    FailFetcher(DataFetcher<Data> wrapped, RuntimeException toThrow) {
      this.wrapped = wrapped;
      this.toThrow = toThrow;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super Data> callback) {
      throw toThrow;
    }

    @Override
    public void cleanup() {
      wrapped.cleanup();
    }

    @Override
    public void cancel() {
      wrapped.cancel();
    }

    @NonNull
    @Override
    public Class<Data> getDataClass() {
      return wrapped.getDataClass();
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
      return wrapped.getDataSource();
    }
  }
}
