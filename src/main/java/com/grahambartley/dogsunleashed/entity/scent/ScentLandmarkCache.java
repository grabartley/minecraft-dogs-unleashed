package com.grahambartley.dogsunleashed.entity.scent;

import java.util.Optional;
import java.util.function.Supplier;

public final class ScentLandmarkCache {

  private boolean resolved;
  private Optional<ScentLandmark> landmark = Optional.empty();

  public Optional<ScentLandmark> resolve(final Supplier<Optional<ScentLandmark>> lookup) {
    if (!this.resolved) {
      this.landmark = lookup.get();
      this.resolved = true;
    }
    return this.landmark;
  }

  public Optional<ScentLandmark> known() {
    return this.landmark;
  }

  public void remember(final ScentLandmark found) {
    this.landmark = Optional.of(found);
    this.resolved = true;
  }

  public void clear() {
    this.landmark = Optional.empty();
    this.resolved = false;
  }
}
