package com.grahambartley.dogsunleashed.model;

import java.util.Map;
import java.util.WeakHashMap;

public final class SharedBonePoses<B> {

  private record Poser(Object model, long instance) {}

  private final Map<B, Poser> lastPosers = new WeakHashMap<>();

  public synchronized boolean lastPosedBy(final B bones, final Object model, final long instance) {
    final Poser poser = this.lastPosers.get(bones);
    return poser != null && poser.model() == model && poser.instance() == instance;
  }

  public synchronized void posedBy(final B bones, final Object model, final long instance) {
    this.lastPosers.put(bones, new Poser(model, instance));
  }
}
