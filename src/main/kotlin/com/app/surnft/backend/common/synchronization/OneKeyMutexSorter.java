package com.app.surnft.backend.common.synchronization;

import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class OneKeyMutexSorter<Key> {

  private final OneKeyMutexFactory<Key> factory;

  public OneKeyMutexSorter(final OneKeyMutexFactory<Key> factory) {
    this.factory = factory;
  }

  /**
   * Check existing of collisions in the list of mutexes.
   *
   * @param mutexes list of mutexes to run checking
   * @return true if there are at least two mutexes with the same identityHashCode
   * value in the list, and false if all mutexes have different hash values
   * or if empty list of mutexes
   */
  public boolean isExistCollision(final List<OneKeyMutex<Key>> mutexes) {
    return !CollectionUtils.isEmpty(mutexes) && mutexes.stream()
        .map(System::identityHashCode)
        .collect(Collectors.toSet())
        .size() < mutexes.size();
  }

  /**
   * Obtain a list of mutexes from {@link OneKeyMutexFactory} by the collection
   * of keys and sort this list by identityHashCode values of a mutex.
   *
   * @param keys collection of keys which necessary to sort in the same order
   *             depends just on the external values of identityHashCode.
   * @return sorted list of mutexes
   */
  public List<OneKeyMutex<Key>> getOrderedMutexList(final Collection<Key> keys) {
    if (CollectionUtils.isEmpty(keys)) {
      return Collections.emptyList();
    }

    return keys.stream()
        .map(factory::getMutex)
        .sorted(Comparator.comparingInt(System::identityHashCode))
        .collect(Collectors.toList());
  }
}
