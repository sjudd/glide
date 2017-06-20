package com.bumptech.glide.samples.gallery;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.module.AppGlideModule;
import java.io.File;

/**
 * Ensures that Glide's generated API is created for the Gallery sample.
 */
@GlideModule
public final class GalleryModule extends AppGlideModule {
  // Intentionally empty.
  @Override
  public void registerComponents(Context context, Registry registry) {
    super.registerComponents(context, registry);
    BitmapPool bitmapPool = Glide.get(context).getBitmapPool();
    registry.prepend(File.class, Drawable.class, new FileDecoder(bitmapPool));
  }
}
