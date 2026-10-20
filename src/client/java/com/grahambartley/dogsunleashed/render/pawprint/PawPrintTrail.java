package com.grahambartley.dogsunleashed.render.pawprint;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PawPrintTrail {

  private final Deque<Print> prints = new ArrayDeque<>();
  private final Map<Integer, Step> lastSteps = new HashMap<>();
  private long tick;

  public void step(
      final int dogId,
      final double x,
      final double y,
      final double z,
      final float yaw,
      final float dogWidth) {
    final Step last = this.lastSteps.get(dogId);
    if (last != null && !PawPrintRules.stamps(x - last.x(), z - last.z())) {
      return;
    }
    final boolean left = last == null || !last.left();
    this.lastSteps.put(dogId, new Step(x, z, left));
    this.prints.addLast(
        new Print(
            x,
            y,
            z,
            yaw,
            PawPrintRules.size(dogWidth),
            PawPrintRules.sideOffset(left, dogWidth),
            this.tick));
    while (this.prints.size() > PawPrintRules.MAX_PRINTS) {
      this.prints.removeFirst();
    }
  }

  public void forget(final int dogId) {
    this.lastSteps.remove(dogId);
  }

  public void keepOnly(final Set<Integer> dogIds) {
    this.lastSteps.keySet().retainAll(dogIds);
  }

  public void advance() {
    this.tick++;
    while (!this.prints.isEmpty()
        && PawPrintRules.expired(this.tick - this.prints.peekFirst().bornTick())) {
      this.prints.removeFirst();
    }
  }

  public long tick() {
    return this.tick;
  }

  public List<Print> prints() {
    return List.copyOf(this.prints);
  }

  public void clear() {
    this.prints.clear();
    this.lastSteps.clear();
  }

  public record Print(
      double x, double y, double z, float yaw, float size, float sideOffset, long bornTick) {}

  private record Step(double x, double z, boolean left) {}
}
