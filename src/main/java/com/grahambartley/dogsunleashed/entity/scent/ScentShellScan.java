package com.grahambartley.dogsunleashed.entity.scent;

import org.jetbrains.annotations.Nullable;

public final class ScentShellScan {

  public enum Status {
    SEARCHING,
    FOUND,
    EXHAUSTED
  }

  @FunctionalInterface
  public interface Probe {
    boolean matches(int dx, int dy, int dz);
  }

  public record Offset(int dx, int dy, int dz) {
    public long squaredLength() {
      return (long) dx * dx + (long) dy * dy + (long) dz * dz;
    }
  }

  private final int radius;
  private final int verticalReach;
  private int shell;
  private int cell;
  private int dy;
  private @Nullable Offset best;
  private Status status = Status.SEARCHING;

  public ScentShellScan(final int radius, final int verticalReach) {
    if (radius < 0 || verticalReach < 0) {
      throw new IllegalArgumentException("Scan radius and vertical reach must not be negative");
    }
    this.radius = radius;
    this.verticalReach = verticalReach;
    this.dy = -verticalReach;
  }

  public static int cellsInShell(final int shell) {
    return shell == 0 ? 1 : 8 * shell;
  }

  public static long volume(final int radius, final int verticalReach) {
    final long side = 2L * radius + 1;
    return side * side * (2L * verticalReach + 1);
  }

  public Status advance(final int budget, final Probe probe) {
    int probes = 0;
    while (this.status == Status.SEARCHING && probes < budget) {
      final int dx = cellX(this.shell, this.cell);
      final int dz = cellZ(this.shell, this.cell);
      probes++;
      if (probe.matches(dx, this.dy, dz)) {
        this.keepNearest(new Offset(dx, this.dy, dz));
      }
      this.step();
    }
    return this.status;
  }

  public Status status() {
    return this.status;
  }

  public @Nullable Offset found() {
    return this.best;
  }

  private void keepNearest(final Offset hit) {
    if (this.best == null || hit.squaredLength() < this.best.squaredLength()) {
      this.best = hit;
    }
  }

  private void step() {
    if (++this.dy <= this.verticalReach) {
      return;
    }
    this.dy = -this.verticalReach;
    if (++this.cell < cellsInShell(this.shell)) {
      return;
    }
    this.cell = 0;
    if (this.best != null) {
      this.status = Status.FOUND;
    } else if (++this.shell > this.radius) {
      this.status = Status.EXHAUSTED;
    }
  }

  static int cellX(final int shell, final int cell) {
    if (shell == 0) {
      return 0;
    }
    final int edge = 2 * shell;
    final int along = cell % edge;
    return switch (cell / edge) {
      case 0 -> -shell + along;
      case 1 -> shell;
      case 2 -> shell - along;
      default -> -shell;
    };
  }

  static int cellZ(final int shell, final int cell) {
    if (shell == 0) {
      return 0;
    }
    final int edge = 2 * shell;
    final int along = cell % edge;
    return switch (cell / edge) {
      case 0 -> -shell;
      case 1 -> -shell + along;
      case 2 -> shell;
      default -> shell - along;
    };
  }
}
