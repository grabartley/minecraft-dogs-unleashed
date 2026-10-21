package com.grahambartley.dogsunleashed.render.sled;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.math.Vec3d;

public final class HarnessRumps {

  static final double FALLBACK_HEIGHT = 0.6;
  private static final Map<Integer, Vec3d> THIS_FRAME = new HashMap<>();

  private HarnessRumps() {}

  public static void record(final UnleashedDogEntity dog, final Vec3d world) {
    THIS_FRAME.put(dog.getId(), world);
  }

  static Vec3d of(final UnleashedDogEntity dog, final float tickDelta) {
    final Vec3d recorded = THIS_FRAME.get(dog.getId());
    return recorded != null
        ? recorded
        : dog.getLerpedPos(tickDelta).add(0.0, dog.getHeight() * FALLBACK_HEIGHT, 0.0);
  }

  static void startFrame() {
    THIS_FRAME.clear();
  }
}
