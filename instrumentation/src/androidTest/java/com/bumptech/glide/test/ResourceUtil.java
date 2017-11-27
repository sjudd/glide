package com.bumptech.glide.test;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.IdRes;
import android.support.test.InstrumentationRegistry;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utilities for interacting with test resources.
 */
public final class ResourceUtil {

  private ResourceUtil() {
    // Utility class.
  }

  public static byte[] loadData(@IdRes int resourceId) {
    Context context = InstrumentationRegistry.getTargetContext();
    Resources resources = context.getResources();
    InputStream is = null;
    try {
      is = resources.openRawResource(resourceId);
      return ByteStreams.toByteArray(is);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          // Ignored.
        }
      }
    }
  }
}
