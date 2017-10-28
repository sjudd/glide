package com.bumptech.glide;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.bumptech.glide.test.GlideApp;
import com.bumptech.glide.test.ResourceIds;
import com.bumptech.glide.test.TearDownGlide;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class Issue2515Test {
  @Rule public TearDownGlide tearDownGlide = new TearDownGlide();
  @Rule public ExpectedException expectedException = ExpectedException.none();
  private Context context;

  @Before
  public void setUp() {
    context = InstrumentationRegistry.getTargetContext();
  }

  @After
  public void tearDown() {
    File tempFile = getTempFile();
    if (tempFile.exists() && !tempFile.delete()) {
      throw new IllegalStateException("Failed to delete temp file: " + tempFile);
    }
  }

  @Test
  public void load_asBitmap_failsWithError() throws ExecutionException, InterruptedException {
    expectedException.expect(ExecutionException.class);
    GlideApp.with(context)
        .asBitmap()
        .load(ResourceIds.raw.issue_2515)
        .submit()
        .get();
  }

  @Test
  public void load_asBitmap_withFile_failsWithError()
      throws ExecutionException, InterruptedException {
    File tempFile = createTempFile();

    expectedException.expect(ExecutionException.class);
    GlideApp.with(context)
        .asBitmap()
        .load(tempFile)
        .submit()
        .get();
  }

  @Test
  public void load_asBitmap_withFilePath_failsWithError()
      throws ExecutionException, InterruptedException {
    File tempFile = createTempFile();

    expectedException.expect(ExecutionException.class);
    GlideApp.with(context)
        .asBitmap()
        .load(tempFile.getAbsolutePath())
        .submit()
        .get();
  }

  private File getTempFile() {
    File cacheDir = context.getCacheDir();
    return new File(cacheDir, "temp");
  }

  private File createTempFile() {
    File tempFile = getTempFile();

    InputStream is = null;
    OutputStream os = null;
    try {
      is = context.getResources().openRawResource(ResourceIds.raw.issue_2515);
      os = new FileOutputStream(tempFile);
      byte[] buffer = new byte[16 * 1024];
      int read;
      while ((read = is.read(buffer)) != -1) {
        os.write(buffer, 0, read);
      }
      os.close();
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
      if (os != null) {
        try {
          os.close();
        } catch (IOException e) {
          // Ignored.
        }
      }
    }
    return tempFile;
  }
}
