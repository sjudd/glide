package com.bumptech.glide.samples.gallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.Log;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapDrawableResource;
import java.io.File;
import java.io.IOException;

public class FileDecoder implements ResourceDecoder<File,Drawable> {

  private final BitmapPool bitmapPool;

  FileDecoder(BitmapPool bitmapPool) {
    this.bitmapPool = bitmapPool;
  }

  @Override
  public boolean handles(File source, Options options) throws IOException {
    return true;
  }

  @Nullable
  @Override
  public Resource<Drawable> decode(File source, int width, int height, Options options)
      throws IOException {
    Log.d("TEST", "decode: " + source);
    Bitmap bitmap = BitmapFactory.decodeFile(source.getAbsolutePath());
    BitmapDrawable drawable = new BitmapDrawable(bitmap);
    return (Resource) new BitmapDrawableResource(drawable, bitmapPool);
  }
}
