package com.grahambartley.dogsunleashed.gesture;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.Hand;

public final class PlayerGesturePoses {

  public static final PlayerGesturePoses SHARED = new PlayerGesturePoses();
  public static final float NOT_PLAYING = -1.0f;

  private final Map<Integer, Pose> poses = new HashMap<>();

  public void start(
      final int entityId, final Hand hand, final PlayerGesture gesture, final long worldTime) {
    this.poses.values().removeIf(pose -> !pose.isPlaying(worldTime));
    this.poses.put(entityId, new Pose(hand, gesture, worldTime));
  }

  public boolean isPlaying(
      final int entityId, final Hand hand, final PlayerGesture gesture, final long worldTime) {
    return this.elapsedTicks(entityId, hand, gesture, worldTime, 0.0f) != NOT_PLAYING;
  }

  public float elapsedTicks(
      final int entityId,
      final Hand hand,
      final PlayerGesture gesture,
      final long worldTime,
      final float tickDelta) {
    final Pose pose = this.poses.get(entityId);
    if (pose == null || pose.hand() != hand || pose.gesture() != gesture) {
      return NOT_PLAYING;
    }
    final float elapsed = worldTime - pose.startTime() + tickDelta;
    return gesture.isPlaying(elapsed) ? elapsed : NOT_PLAYING;
  }

  public void clear() {
    this.poses.clear();
  }

  private record Pose(Hand hand, PlayerGesture gesture, long startTime) {

    boolean isPlaying(final long worldTime) {
      return this.gesture.isPlaying(worldTime - this.startTime);
    }
  }
}
