package com.bumptech.glide.load.model;


import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader.LoadData;
import com.google.common.collect.ImmutableList;
import java.io.InputStream;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public class ByteArrayModelLoaderTest {
  @Mock private ModelLoader<byte[], InputStream> wrapped;
  @Mock private Key key;
  private final byte[] data = new byte[0];

  private ByteArrayModelLoader<InputStream> loader;
  private ByteArrayModel model;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    model = new ByteArrayModel(key, data);
    loader = new ByteArrayModelLoader<>(wrapped);
  }

  @Test
  public void constructor_withNullWrappedModelLoader_throws() {
    assertThrows(NullPointerException.class, new ThrowingRunnable() {
      @SuppressWarnings("ConstantConditions")
      @Override
      public void run() throws Throwable {
        new ByteArrayModelLoader<>(/*wrapped=*/ null);
      }
    });
  }

  @Test
  public void handles_whenWrappedHandles_returnsTrue() {
    when(wrapped.handles(any(byte[].class))).thenReturn(true);
    assertThat(loader.handles(model)).isTrue();
  }

  @Test
  public void handles_whenWrappedDoesNotHandle_returnsFalse() {
    assertThat(loader.handles(model)).isFalse();
  }

  @Test
  public void buildLoadData_whenWrappedReturnsNull_returnsNull() {
    assertThat(loader.buildLoadData(model, /*width=*/ 100, /*height=*/ 100, new Options()))
        .isNull();
  }

  @Test
  public void buildLoadData_whenWrappedReturnsFetcher_returnsLoadDataWithFetcher() {
    int width = 100;
    int height = 200;
    Options options = new Options();
    DataFetcher<InputStream> fetcher = mockDataFetcher();
    when(wrapped.buildLoadData(data, width, height, options))
        .thenReturn(new LoadData<>(mock(Key.class), fetcher));

    LoadData<InputStream> result = loader.buildLoadData(model, width, height, options);

    assertThat(result).isNotNull();
    assertThat(result.fetcher).isEqualTo(fetcher);
  }

  @Test
  public void buildLoadData_whenWrappedReturnsKey_returnsKeyFromModel() {
    int width = 100;
    int height = 200;
    Options options = new Options();
    DataFetcher<InputStream> fetcher = mockDataFetcher();
    when(wrapped.buildLoadData(data, width, height, options))
        .thenReturn(new LoadData<>(mock(Key.class), fetcher));

    LoadData<InputStream> result = loader.buildLoadData(model, width, height, options);

    assertThat(result).isNotNull();
    assertThat(result.sourceKey).isEqualTo(key);
  }

  @Test
  public void buildLoadData_whenWrappedReturnsAlternateKeys_returnsNoAlternateKeys() {
    int width = 100;
    int height = 200;
    Options options = new Options();
    DataFetcher<InputStream> fetcher = mockDataFetcher();

    List<Key> alternateKeysFromWrapped = ImmutableList.of(mock(Key.class), mock(Key.class));

    when(wrapped.buildLoadData(data, width, height, options))
        .thenReturn(new LoadData<>(mock(Key.class), alternateKeysFromWrapped, fetcher));

    LoadData<InputStream> result = loader.buildLoadData(model, width, height, options);

    assertThat(result).isNotNull();
    assertThat(result.alternateKeys).isEmpty();
  }

  @SuppressWarnings("unchecked")
  private static DataFetcher<InputStream> mockDataFetcher() {
    return mock(DataFetcher.class);
  }
}
