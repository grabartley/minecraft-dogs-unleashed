package com.grahambartley.dogsunleashed.entity.scent;

import org.jetbrains.annotations.Nullable;

public final class ScentShellScan {

  public static final long NO_MATCH = -1L;

  public enum Status {
    SEARCHING,
    FOUND,
    EXHAUSTED
  }

  @FunctionalInterface
  public interface Probe {
    long squaredDistance(int dx, int dy, int dz);
  }

  public record Offset(int dx, int dy, int dz) {
    public long squaredLength() {
      return (long) dx * dx + (long) dy * dy + (long) dz * dz;
    }
  }

  private final int radius;
  private final int verticalReach;
  private final int cellSize;
  private int shell;
  private int cell;
  private int dy;
  private @Nullable Offset best;
  private long bestSquared = Long.MAX_VALUE;
  private Status status = Status.SEARCHING;

  public ScentShellScan(final int radius, final int verticalReach) {
    this(radius, verticalReach, 1);
  }

  public ScentShellScan(final int radius, final int verticalReach, final int cellSize) {
    if (radius < 0 || verticalReach < 0 || cellSize < 1) {
      throw new IllegalArgumentException("Scan radius, reach and cell size must be valid");
    }
    this.radius = radius;
    this.verticalReach = verticalReach;
    this.cellSize = cellSize;
    this.dy = -verticalReach;
  }

  public static int cellsInShell(final int shell) {
    return shell == 0 ? 1 : 8 * shell;
  }

  public static long volume(final int radius, final int verticalReach) {
    final long side = 2L * radius + 1;
    return side * side * (2L * verticalReach + 1);
  }

  static long closestSquaredInShell(final int shell, final int cellSize) {
    final long closest = shell == 0 ? 0L : (long) shell * cellSize - (cellSize - 1);
    return closest * closest;
  }

  public Status advance(final int budget, final Probe probe) {
    int probes = 0;
    while (this.status == Status.SEARCHING && probes < budget) {
      final int dx = cellX(this.shell, this.cell);
      final int dz = cellZ(this.shell, this.cell);
      probes++;
      final long squared = probe.squaredDistance(dx, this.dy, dz);
      if (squared != NO_MATCH && squared < this.bestSquared) {
        this.bestSquared = squared;
        this.best = new Offset(dx, this.dy, dz);
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

  private void step() {
    if (++this.dy <= this.verticalReach) {
      return;
    }
    this.dy = -this.verticalReach;
    if (++this.cell < cellsInShell(this.shell)) {
      return;
    }
    this.cell = 0;
    this.shell++;
    if (this.best != null
        && (this.shell > this.radius
            || closestSquaredInShell(this.shell, this.cellSize) >= this.bestSquared)) {
      this.status = Status.FOUND;
    } else if (this.shell > this.radius) {
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
