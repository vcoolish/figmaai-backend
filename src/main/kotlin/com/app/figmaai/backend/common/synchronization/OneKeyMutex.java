package com.app.figmaai.backend.common.synchronization;

import java.util.Objects;

public record OneKeyMutex<Key>(Key key) {

  public static <Key> OneKeyMutex<Key> of(final Key key) {
    return new OneKeyMutex<>(key);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    return Objects.equals(key, ((OneKeyMutex<?>) o).key);
  }

}
