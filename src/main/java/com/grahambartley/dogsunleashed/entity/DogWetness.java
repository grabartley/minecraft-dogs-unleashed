package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

/**
 * Tracks how long a dog has been drying off and decides when a shake should start. Water and rain
 * are the same thing to this timer: any wet tick resets it, and the shake fires a fixed delay after
 * the last one, whether the dog climbed out of a lake, walked under shelter, or the rain stopped.
 */
public final class DogWetness {

  static final int SHAKE_DELAY_TICKS = 20;

  private boolean wasWet = false;
  private int ticksSinceWet = 0;

  public static int nextTicksSinceWet(
      final boolean wet, final boolean wasWet, final int ticksSinceWet) {
    if (wet) {
      return 0;
    }
    if (wasWet) {
      return 1;
    }
    return ticksSinceWet > 0 ? ticksSinceWet + 1 : ticksSinceWet;
  }

  public static boolean isShakeStartTick(final boolean wet, final int ticksSinceWet) {
    return !wet && ticksSinceWet == SHAKE_DELAY_TICKS;
  }

  /**
   * @return whether this tick is the one that should start a shake
   */
  public boolean tick(final boolean wet) {
    this.ticksSinceWet = nextTicksSinceWet(wet, this.wasWet, this.ticksSinceWet);
    this.wasWet = wet;
    return isShakeStartTick(wet, this.ticksSinceWet);
  }

  void writeNbt(final NbtCompound nbt) {
    nbt.putBoolean(ModNbtKeys.WAS_WET, this.wasWet);
    nbt.putInt(ModNbtKeys.TICKS_SINCE_WET, this.ticksSinceWet);
  }

  void readNbt(final NbtCompound nbt) {
    if (nbt.contains(ModNbtKeys.WAS_WET)) {
      this.wasWet = nbt.getBoolean(ModNbtKeys.WAS_WET);
    }
    if (nbt.contains(ModNbtKeys.TICKS_SINCE_WET, NbtElement.NUMBER_TYPE)) {
      this.ticksSinceWet = nbt.getInt(ModNbtKeys.TICKS_SINCE_WET);
    }
  }
}
