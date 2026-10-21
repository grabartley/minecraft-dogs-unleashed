package com.grahambartley.dogsunleashed.entity;

import java.util.Map;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.loading.object.BakedAnimations;

public final class RigClipPresence {

  private RigClipPresence() {}

  public static boolean has(final UnleashedDogEntity dog, final String clip) {
    return has(GeckoLibCache.getBakedAnimations(), dog.getRigSourceBreed().animationId(), clip);
  }

  static boolean has(
      final Map<Identifier, BakedAnimations> loaded, final Identifier rig, final String clip) {
    final BakedAnimations animations = loaded.get(rig);
    return animations != null && animations.animations().containsKey(clip);
  }
}
