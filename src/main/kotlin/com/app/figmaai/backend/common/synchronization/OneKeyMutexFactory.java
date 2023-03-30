package com.app.figmaai.backend.common.synchronization;

import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ConcurrentReferenceHashMap.ReferenceType;

import java.util.concurrent.ConcurrentMap;

public class OneKeyMutexFactory<Key> {

  private static final Integer DEFAULT_CONCURRENCY_LEVEL = 16;
  private static final Integer DEFAULT_INITIAL_CAPACITY = 16;
  private static final Float DEFAULT_LOAD_FACTOR = 0.75f;

  private final ConcurrentMap<Key, OneKeyMutex<Key>> map;

  public OneKeyMutexFactory() {
    this.map = new ConcurrentReferenceHashMap<>(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR,
        DEFAULT_CONCURRENCY_LEVEL, ReferenceType.WEAK);
  }

  public OneKeyMutexFactory(final Integer concurrencyLevel, final ReferenceType referenceType) {
    this.map = new ConcurrentReferenceHashMap<>(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR,
        concurrencyLevel, referenceType);
  }

  public OneKeyMutex<Key> getMutex(final Key key) {
    return this.map.computeIfAbsent(key, OneKeyMutex::new);
  }

  public long size() {
    return this.map.size();
  }

  public void purgeUnreferenced() {
    ((ConcurrentReferenceHashMap<Key, OneKeyMutex<Key>>) this.map).purgeUnreferencedEntries();
  }
}
