package com.grahambartley.dogsunleashed.entity;

import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

public final class SleepClipLatch {

  static final long STALE_TICKS = 40;

  private @Nullable String held = null;
  private long lastSeenAt = 0;

  public String hold(final long now, final Supplier<String> chooser) {
    if (this.held == null || Math.abs(now - this.lastSeenAt) > STALE_TICKS) {
      this.held = chooser.get();
    }
    this.lastSeenAt = now;
    return this.held;
  }

  public void release() {
    this.held = null;
  }
}
