package com.grahambartley.dogsunleashed.whistle;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

public final class WhistleBlowPoses {

  public static final WhistleBlowPoses SHARED = new WhistleBlowPoses();

  static final int BLOW_TICKS = 14;
  static final float RAISE_TICKS = 3.0f;

  private final Map<Integer, Blow> blows = new HashMap<>();

  public void start(final int entityId, final Hand hand, final long worldTime) {
    this.blows.values().removeIf(blow -> blow.isOver(worldTime));
    this.blows.put(entityId, new Blow(hand, worldTime));
  }

  public boolean isBlowing(final int entityId, final Hand hand, final long worldTime) {
    final Blow blow = this.blows.get(entityId);
    return blow != null && blow.hand() == hand && !blow.isOver(worldTime);
  }

  public float lift(
      final int entityId, final Hand hand, final long worldTime, final float tickDelta) {
    final Blow blow = this.blows.get(entityId);
    if (blow == null || blow.hand() != hand) {
      return 0.0f;
    }
    return liftAt(worldTime - blow.startTime() + tickDelta);
  }

  public void clear() {
    this.blows.clear();
  }

  static float liftAt(final float elapsedTicks) {
    if (elapsedTicks < 0.0f || elapsedTicks >= BLOW_TICKS) {
      return 0.0f;
    }
    final float rising = elapsedTicks / RAISE_TICKS;
    final float falling = (BLOW_TICKS - elapsedTicks) / RAISE_TICKS;
    return MathHelper.clamp(Math.min(rising, falling), 0.0f, 1.0f);
  }

  private record Blow(Hand hand, long startTime) {

    boolean isOver(final long worldTime) {
      return worldTime < this.startTime || worldTime - this.startTime >= BLOW_TICKS;
    }
  }
}
