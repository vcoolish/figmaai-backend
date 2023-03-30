package com.app.figmaai.backend.common.synchronization;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class SyncTemplate<Key> {

  private static final Object GLOBAL_LOCK = new Object();

  private final OneKeyMutexFactory<Key> mutexFactory;
  private final OneKeyMutexSorter<Key> mutexSorter;

  public SyncTemplate() {
    this.mutexFactory = new OneKeyMutexFactory<>();
    this.mutexSorter = new OneKeyMutexSorter<>(mutexFactory);
  }

  public SyncTemplate(final OneKeyMutexFactory<Key> mutexFactory) {
    this.mutexFactory = mutexFactory;
    this.mutexSorter = new OneKeyMutexSorter<>(mutexFactory);
  }

  public void execute(final Key mutexKey, final Runnable runnable) {
    synchronized (mutexFactory.getMutex(mutexKey)) {
      runnable.run();
    }
  }

  public <Result> Result evaluate(final Key mutexKey, final Supplier<Result> supplier) {
    try {
      return this.evaluateThrows(mutexKey, supplier::get);
    } catch (RuntimeException ex) {
      throw ex;
    } catch (Exception ignore) {
      return null;
    }
  }

  public <Result> Result evaluateThrows(final Key mutexKey, final Callable<Result> callable) throws Exception {
    synchronized (mutexFactory.getMutex(mutexKey)) {
      return callable.call();
    }
  }

  /**
   * Execute the runnable within a pair of synchronized blocks
   * which built from the first and the second keys.
   * <p>
   * Note that the ordering of keys in synchronized blocks doesn't depend
   * on the ordering of keys in this method arguments. The order depends
   * on the values of keys, it prevents your code from deadlocking.
   *
   * @param firstKey  the first key to use in the synchronization blocks
   * @param secondKey the second key
   * @param runnable  code which you want to synchronize
   */
  public void execute(final Key firstKey, final Key secondKey, final Runnable runnable) {
    OneKeyMutex<Key> firstMutex = mutexFactory.getMutex(firstKey);
    OneKeyMutex<Key> secondMutex = mutexFactory.getMutex(secondKey);

    int firstHash = System.identityHashCode(firstKey);
    int secondHash = System.identityHashCode(secondKey);

    if (firstHash > secondHash) {
      OneKeyMutex<Key> tmp = firstMutex;
      firstMutex = secondMutex;
      secondMutex = tmp;
    }

    if (firstHash != secondHash) {
      synchronized (firstMutex) {
        synchronized (secondMutex) {
          runnable.run();
        }
      }
    } else {
      synchronized (GLOBAL_LOCK) {
        synchronized (firstMutex) {
          synchronized (secondMutex) {
            runnable.run();
          }
        }
      }
    }
  }

  public <Result> Result evaluate(final Key firstKey, final Key secondKey, final Supplier<Result> supplier) {
    try {
      return this.evaluateThrows(firstKey, secondKey, supplier::get);
    } catch (RuntimeException ex) {
      throw ex;
    } catch (Exception ignore) {
      return null;
    }
  }

  /**
   * Evaluate the {@link Supplier} within a pair of synchronized blocks
   * which built from the first and the second keys.
   * <p>
   * Note that the ordering of keys in synchronized blocks doesn't depend
   * on the ordering of keys in this method arguments. The order depends
   * on the values of keys, it prevents your code from deadlocking.
   *
   * @param firstKey  the first key to use in the synchronization blocks
   * @param secondKey the second key
   * @param callable  code which you want to synchronize
   * @param <Result>  the type of supplier's result
   * @return the result of the supplier
   */
  public <Result> Result evaluateThrows(final Key firstKey, final Key secondKey, final Callable<Result> callable)
      throws Exception {
    OneKeyMutex<Key> firstMutex = mutexFactory.getMutex(firstKey);
    OneKeyMutex<Key> secondMutex = mutexFactory.getMutex(secondKey);

    int firstHash = System.identityHashCode(firstKey);
    int secondHash = System.identityHashCode(secondKey);

    if (firstHash > secondHash) {
      OneKeyMutex<Key> tmp = firstMutex;
      firstMutex = secondMutex;
      secondMutex = tmp;
    }

    if (firstHash != secondHash) {
      synchronized (firstMutex) {
        synchronized (secondMutex) {
          return callable.call();
        }
      }
    } else {
      synchronized (GLOBAL_LOCK) {
        synchronized (firstMutex) {
          synchronized (secondMutex) {
            return callable.call();
          }
        }
      }
    }
  }

  /**
   * Execute the runnable in a multi-keys synchronization block
   * which compose step-by-step on the each key from the keys collection.
   * <p>
   * Note:<p>
   * The ordering of synchronization depends on the key value (not on
   * the key order in the collection), it prevents your code of deadlocks
   * in another code with synchronized blocks on the same keys.
   *
   * @param keys     collection of keys to sequentially synchronization
   * @param runnable code block which is necessary to synchronize by the sequence of keys
   */
  public void execute(final Collection<Key> keys, final Runnable runnable) {
    if (keys.size() < 1) {
      throw new RuntimeException("Empty key list");
    }

    List<OneKeyMutex<Key>> mutexes = mutexSorter.getOrderedMutexList(keys);
    if (mutexSorter.isExistCollision(mutexes)) {
      synchronized (GLOBAL_LOCK) {
        recursiveExecute(mutexes, runnable);
      }
    } else {
      recursiveExecute(mutexes, runnable);
    }
  }

  public <Result> Result evaluate(final Collection<Key> keys, final Supplier<Result> supplier) {
    try {
      return this.evaluateThrows(keys, supplier::get);
    } catch (RuntimeException ex) {
      throw ex;
    } catch (Exception ignore) {
      return null;
    }
  }

  /**
   * Evaluate the supplier in a multi-keys synchronization block
   * which compose step-by-step sync on the each key from the keys collection.
   * <p>
   * Note that the ordering of synchronization depends on the key value (not on
   * the key order in the collection), it prevents your code of deadlocks
   * in another code with synchronized blocks on the same keys.
   *
   * @param keys     collection of keys to sequentially synchronization
   * @param callable running of this code should be synchronized by the sequence of keys
   * @param <Result> the type of a supplier result
   * @return the result of supplier execution
   */
  public <Result> Result evaluateThrows(final Collection<Key> keys, final Callable<Result> callable) throws Exception {
    if (keys.size() < 1) {
      throw new RuntimeException("Empty key list");
    }

    List<OneKeyMutex<Key>> mutexes = mutexSorter.getOrderedMutexList(keys);
    if (mutexSorter.isExistCollision(mutexes)) {
      synchronized (GLOBAL_LOCK) {
        return recursiveEvaluate(mutexes, callable);
      }
    } else {
      return recursiveEvaluate(mutexes, callable);
    }
  }

  private void recursiveExecute(final List<OneKeyMutex<Key>> mutexes, final Runnable runnable) {
    OneKeyMutex<Key> currentMutex = mutexes.get(0);
    mutexes.remove(currentMutex);

    if (mutexes.size() == 0) {
      synchronized (currentMutex) {
        runnable.run();
      }
    } else {
      synchronized (currentMutex) {
        recursiveExecute(mutexes, runnable);
      }
    }
  }

  private <Result> Result recursiveEvaluate(final List<OneKeyMutex<Key>> mutexes, final Callable<Result> callable)
      throws Exception {
    OneKeyMutex<Key> currentMutex = mutexes.get(0);
    mutexes.remove(currentMutex);

    if (mutexes.size() == 0) {
      synchronized (currentMutex) {
        return callable.call();
      }
    } else {
      synchronized (currentMutex) {
        return recursiveEvaluate(mutexes, callable);
      }
    }
  }
}
