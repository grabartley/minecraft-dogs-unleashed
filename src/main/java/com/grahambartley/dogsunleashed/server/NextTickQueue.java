package com.grahambartley.dogsunleashed.server;

import java.util.ArrayList;
import java.util.List;

public final class NextTickQueue {
  private final List<Runnable> pending = new ArrayList<>();

  public void enqueue(final Runnable task) {
    pending.add(task);
  }

  public void runPending() {
    if (pending.isEmpty()) {
      return;
    }
    final List<Runnable> tasks = new ArrayList<>(pending);
    pending.clear();
    for (final Runnable task : tasks) {
      task.run();
    }
  }

  public void clear() {
    pending.clear();
  }
}
