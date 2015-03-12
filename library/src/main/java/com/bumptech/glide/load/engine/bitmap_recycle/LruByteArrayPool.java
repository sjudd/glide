package com.bumptech.glide.load.engine.bitmap_recycle;

import android.util.Log;

import java.util.Arrays;
import java.util.TreeMap;

/**
 * A fixed size LruByteArrayPool.
 */
public final class LruByteArrayPool implements ByteArrayPool {
  private static final String TAG = "LruBytesPool";
  // 4mb.
  private static final int DEFAULT_SIZE = 4 * 1024 * 1024;
  private static final int MAX_SIZE_MULTIPLE = 8;
  private final GroupedLinkedMap<Key, byte[]> groupedMap = new GroupedLinkedMap<>();
  private final KeyPool keyPool = new KeyPool();
  private final TreeMap<Integer, Integer> sortedSizes = new TreeMap<>();
  private final int maxSizeBytes;

  private int currentSizeBytes;

  public LruByteArrayPool() {
    this(DEFAULT_SIZE);
  }

  public LruByteArrayPool(int maxSizeBytes) {
    this.maxSizeBytes = maxSizeBytes;
  }

  @Override
  public synchronized void put(byte[] bytes) {
    int size = bytes.length;
    Key key = keyPool.get(size);

    groupedMap.put(key, bytes);
    Integer current = sortedSizes.get(size);
    sortedSizes.put(size, current == null ? 1 : current + 1);
    currentSizeBytes += size;
    evict();
  }

  @Override
  public byte[] get(int size) {
    byte[] result;
    synchronized (this) {
      Integer possibleSize = sortedSizes.ceilingKey(size);
      final Key key;
      if (possibleSize != null && possibleSize <= (MAX_SIZE_MULTIPLE * size)) {
        key = keyPool.get(possibleSize);
      } else {
        key = keyPool.get(size);
      }

      result = groupedMap.get(key);
      if (result != null) {
        currentSizeBytes -= result.length;
        decrementByteArrayOfSize(result.length);
      }
    }

    if (result != null) {
      Arrays.fill(result, (byte) 0);
    } else {
      if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "Allocated " + size + " bytes");
      }
      result = new byte[size];
    }

    return result;
  }

  @Override
  public synchronized void clearMemory() {
    evictToSize(0);
  }

  @Override
  public synchronized void trimMemory(int level) {
     if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_MODERATE) {
      clearMemory();
    } else if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
      evictToSize(maxSizeBytes / 2);
    }
  }

  private void evict() {
    evictToSize(maxSizeBytes);
  }

  private void evictToSize(int size) {
    while (currentSizeBytes >= size) {
      byte[] evicted = groupedMap.removeLast();
      currentSizeBytes -= evicted.length;
      decrementByteArrayOfSize(evicted.length);
      if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, "evicted: " + evicted.length);
      }
    }
  }

  private void decrementByteArrayOfSize(int size) {
    Integer current = sortedSizes.get(size);
    if (current == 1) {
      sortedSizes.remove(current);
    } else {
      sortedSizes.put(size, current - 1);
    }
  }

  static final class KeyPool extends BaseKeyPool<Key> {

    Key get(int size) {
      Key result = get();
      result.init(size);
      return result;
    }

    @Override
    protected Key create() {
      return new Key(this);
    }
  }

  static final class Key implements Poolable {
    private final KeyPool pool;
    private int size;

    Key(KeyPool pool) {
      this.pool = pool;
    }

    void init(int length) {
      this.size = length;
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof Key) {
        Key other = (Key) o;
        return size == other.size;
      }
      return false;
    }

    @Override
    public int hashCode() {
      return size;
    }

    @Override
    public String toString() {
      return "Key{"
          + "size=" + size
          + '}';
    }

    @Override
    public void offer() {
      pool.offer(this);
    }
  }
}
