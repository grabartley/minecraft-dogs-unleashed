package com.grahambartley.dogsunleashed.entity;

import java.util.Map;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.loading.object.BakedAnimations;

public final class RigClipPresence {

  private RigClipPresence() {}

  public static boolean has(final UnleashedDogEntity dog, final String clip) {
    return has(GeckoLibCache.getBakedAnimations(), dog.getRigSourceBreed().animationId(), clip);
  }

  public static @Nullable Animation find(final UnleashedDogEntity dog, final String clip) {
    return find(GeckoLibCache.getBakedAnimations(), dog.getRigSourceBreed().animationId(), clip);
  }

  static @Nullable Animation find(
      final Map<Identifier, BakedAnimations> loaded, final Identifier rig, final String clip) {
    final BakedAnimations animations = loaded.get(rig);
    return animations == null ? null : animations.animations().get(clip);
  }

  static boolean has(
      final Map<Identifier, BakedAnimations> loaded, final Identifier rig, final String clip) {
    return find(loaded, rig, clip) != null;
  }
}
