package com.bumptech.glide.load.model;

import android.support.annotation.NonNull;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.util.Preconditions;
import java.security.MessageDigest;

/**
 * Pairs a {@code byte[]} with an identifying {@link com.bumptech.glide.load.Key} that allows Glide
 * to perform coherent caching when loading {@code byte[]}s.
 */
public final class ByteArrayModel implements Key {
  @NonNull final Key key;
  @NonNull final byte[] data;

  public ByteArrayModel(@NonNull Key key, @NonNull byte[] data) {
    this.key = Preconditions.checkNotNull(key);
    this.data = Preconditions.checkNotNull(data);
  }

  @Override
  public void updateDiskCacheKey(MessageDigest messageDigest) {
    key.updateDiskCacheKey(messageDigest);
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ByteArrayModel) {
      return key.equals(((ByteArrayModel) obj).key);
    }
    return false;
  }
}
