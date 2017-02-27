package com.bumptech.glide.gifdecoder;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import com.bumptech.glide.testutil.TestUtil;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowBitmap;

/**
 * Tests for {@link com.bumptech.glide.gifdecoder.GifDecoder}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 18)
public class GifDecoderTest {

  private MockProvider provider;

  @Before
  public void setUp() {
    provider = new MockProvider();
  }

  @Test
  public void testCanDecodeFramesFromTestGif() throws IOException {
    byte[] data = TestUtil.resourceToBytes(getClass(), "partial_gif_decode.gif");
    GifHeaderParser headerParser = new GifHeaderParser();
    headerParser.setData(data);
    GifHeader header = headerParser.parseHeader();
    GifDecoder decoder = new StandardGifDecoder(provider);
    decoder.setData(header, data);
    decoder.advance();
    Bitmap bitmap = decoder.getNextFrame();
    assertThat(bitmap).isNotNull();
    assertThat(decoder.getStatus()).isEqualTo(GifDecoder.STATUS_OK);
  }

  @Test
  public void testFrameIndexStartsAtNegativeOne() {
    GifHeader gifheader = new GifHeader();
    gifheader.frameCount = 4;
    byte[] data = new byte[0];
    GifDecoder decoder = new StandardGifDecoder(provider);
    decoder.setData(gifheader, data);
    assertThat(decoder.getCurrentFrameIndex()).isEqualTo(-1);
  }

  @Test
  public void testTotalIterationCountIsOneIfNetscapeLoopCountDoesntExist() {
    GifHeader gifheader = new GifHeader();
    gifheader.loopCount = GifHeader.NETSCAPE_LOOP_COUNT_DOES_NOT_EXIST;
    byte[] data = new byte[0];
    GifDecoder decoder = new StandardGifDecoder(provider);
    decoder.setData(gifheader, data);
    assertEquals(1, decoder.getTotalIterationCount());
  }

  @Test
  public void testTotalIterationCountIsForeverIfNetscapeLoopCountIsForever() {
    GifHeader gifheader = new GifHeader();
    gifheader.loopCount = GifHeader.NETSCAPE_LOOP_COUNT_FOREVER;
    byte[] data = new byte[0];
    GifDecoder decoder = new StandardGifDecoder(provider);
    decoder.setData(gifheader, data);
    assertEquals(GifDecoder.TOTAL_ITERATION_COUNT_FOREVER, decoder.getTotalIterationCount());
  }

  @Test
  public void testTotalIterationCountIsTwoIfNetscapeLoopCountIsOne() {
    GifHeader gifheader = new GifHeader();
    gifheader.loopCount = 1;
    byte[] data = new byte[0];
    GifDecoder decoder = new StandardGifDecoder(provider);
    decoder.setData(gifheader, data);
    assertEquals(2, decoder.getTotalIterationCount());
  }

  @Test
  public void testAdvanceIncrementsFrameIndex() {
    GifHeader gifheader = new GifHeader();
    gifheader.frameCount = 4;
    byte[] data = new byte[0];
    GifDecoder decoder = new StandardGifDecoder(provider);
    decoder.setData(gifheader, data);
    decoder.advance();
    assertThat(decoder.getCurrentFrameIndex()).isEqualTo(0);
  }

  @Test
  public void testAdvanceWrapsIndexBackToZero() {
    GifHeader gifheader = new GifHeader();
    gifheader.frameCount = 2;
    byte[] data = new byte[0];
    GifDecoder decoder = new StandardGifDecoder(provider);
    decoder.setData(gifheader, data);
    decoder.advance();
    decoder.advance();
    decoder.advance();
    assertThat(decoder.getCurrentFrameIndex()).isEqualTo(0);
  }

  @Test
  public void testSettingDataResetsFramePointer() {
    GifHeader gifheader = new GifHeader();
    gifheader.frameCount = 4;
    byte[] data = new byte[0];
    GifDecoder decoder = new StandardGifDecoder(provider);
    decoder.setData(gifheader, data);
    decoder.advance();
    decoder.advance();
    assertThat(decoder.getCurrentFrameIndex()).isEqualTo(1);

    decoder.setData(gifheader, data);
    assertThat(decoder.getCurrentFrameIndex()).isEqualTo(-1);
  }

  @Test
  @Config(shadows = {CustomShadowBitmap.class})
  public void testFirstFrameMustClearBeforeDrawingWhenLastFrameIsDisposalBackground()
      throws IOException {
    byte[] data = TestUtil.resourceToBytes(getClass(), "transparent_disposal_background.gif");
    GifHeaderParser headerParser = new GifHeaderParser();
    headerParser.setData(data);
    GifHeader header = headerParser.parseHeader();
    GifDecoder decoder = new StandardGifDecoder(provider);
    decoder.setData(header, data);
    decoder.advance();
    Bitmap firstFrame = decoder.getNextFrame();
    decoder.advance();
    decoder.getNextFrame();
    decoder.advance();
    Bitmap firstFrameTwice = decoder.getNextFrame();
    assertThat((((CustomShadowBitmap) shadowOf(firstFrameTwice))).getPixels())
        .isEqualTo((((CustomShadowBitmap) shadowOf(firstFrame))).getPixels());
  }

  @Test
  @Config(shadows = {CustomShadowBitmap.class})
  public void testFirstFrameMustClearBeforeDrawingWhenLastFrameIsDisposalNone() throws IOException {
    byte[] data = TestUtil.resourceToBytes(getClass(), "transparent_disposal_none.gif");
    GifHeaderParser headerParser = new GifHeaderParser();
    headerParser.setData(data);
    GifHeader header = headerParser.parseHeader();
    GifDecoder decoder = new StandardGifDecoder(provider);
    decoder.setData(header, data);
    decoder.advance();
    Bitmap firstFrame = decoder.getNextFrame();
    decoder.advance();
    decoder.getNextFrame();
    decoder.advance();
    Bitmap firstFrameTwice = decoder.getNextFrame();
    assertThat((((CustomShadowBitmap) shadowOf(firstFrameTwice))).getPixels())
        .isEqualTo((((CustomShadowBitmap) shadowOf(firstFrame))).getPixels());
  }

  /**
   * Preserve generated bitmap data for checking.
   */
  @Implements(Bitmap.class)
  public static class CustomShadowBitmap extends ShadowBitmap {

    private int[] pixels;

    @Implementation
    public void setPixels(int[] pixels, int offset, int stride,
        int x, int y, int width, int height) {
      this.pixels = new int[pixels.length];
      System.arraycopy(pixels, 0, this.pixels, 0, this.pixels.length);
    }

    public int[] getPixels() {
      return pixels;
    }
  }

  private static class MockProvider implements GifDecoder.BitmapProvider {

    @NonNull
    @Override
    public Bitmap obtain(int width, int height, Bitmap.Config config) {
      Bitmap result = Bitmap.createBitmap(width, height, config);
      Shadows.shadowOf(result).setMutable(true);
      return result;
    }

    @Override
    public void release(Bitmap bitmap) {
      // Do nothing.
    }

    @Override
    public byte[] obtainByteArray(int size) {
      return new byte[size];
    }

    @Override
    public void release(byte[] bytes) {
      // Do nothing.
    }

    @Override
    public int[] obtainIntArray(int size) {
      return new int[size];
    }

    @Override
    public void release(int[] array) {
      // Do Nothing
    }

  }
}
