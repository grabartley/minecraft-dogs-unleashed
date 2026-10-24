package com.grahambartley.dogsunleashed.render.fur;

import com.grahambartley.dogsunleashed.entity.DogFurStage;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

public final class DrawnFur {

  private DrawnFur() {}

  public static Set<String> on(
      final BakedGeoModel model, final UnleashedDogEntity dog, final Set<String> bones) {
    return keep(
        bones,
        RigProfiles.of(dog.getRigSourceBreed()),
        dog.getFur().stage(),
        FurCover.on(dog),
        name -> model.getBone(name).map(GeoBone::getParent).map(GeoBone::getName).orElse(null));
  }

  public static Set<String> keep(
      final Set<String> bones,
      final RigProfile profile,
      final DogFurStage stage,
      final FurCover cover,
      final Function<String, @Nullable String> parentOf) {
    return bones.stream()
        .filter(bone -> draws(profile, stage, cover, bone, parentOf.apply(bone)))
        .collect(Collectors.toUnmodifiableSet());
  }

  public static boolean draws(
      final RigProfile profile,
      final DogFurStage stage,
      final FurCover cover,
      final String bone,
      final @Nullable String parent) {
    return FurLook.shows(profile, bone, stage) && !cover.hides(profile, bone, parent);
  }
}
