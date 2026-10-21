package com.grahambartley.dogsunleashed.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.loading.object.BakedAnimations;

public final class RigSleepClips {

  private static final RigSleepClips SHARED = new RigSleepClips();

  private final Map<Identifier, Variants> variantsByRig = new HashMap<>();

  RigSleepClips() {}

  public static RigSleepClips shared() {
    return SHARED;
  }

  public String clipOf(final UnleashedDogEntity dog) {
    return SleepClipRules.clipFor(
        this.variantsOf(GeckoLibCache.getBakedAnimations(), dog.getRigSourceBreed().animationId()),
        dog.getUuid(),
        dog.getWorld().getTimeOfDay(),
        dog.isBaby(),
        dog.isUndead(),
        dog.getSleepController().isNapping());
  }

  synchronized List<String> variantsOf(
      final Map<Identifier, BakedAnimations> loadedAnimations, final Identifier rigAnimations) {
    final BakedAnimations source = loadedAnimations.get(rigAnimations);
    if (source == null) {
      this.variantsByRig.remove(rigAnimations);
      return List.of();
    }
    final Variants cached = this.variantsByRig.get(rigAnimations);
    if (cached != null && cached.source() == source) {
      return cached.clips();
    }
    final List<String> clips = SleepClipRules.variantsOf(source.animations().keySet());
    this.variantsByRig.put(rigAnimations, new Variants(source, clips));
    return clips;
  }

  private record Variants(BakedAnimations source, List<String> clips) {}
}
