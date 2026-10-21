package com.grahambartley.dogsunleashed.render.cross;

import com.grahambartley.dogsunleashed.entity.DireWolfGrowthStage.SlotScales;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.entity.variant.UnleashedDogCoat;
import com.grahambartley.dogsunleashed.render.cross.CrossCoatTextureCache.CrossCoat;
import com.grahambartley.dogsunleashed.render.growth.GrowthRigScale;
import com.grahambartley.dogsunleashed.render.rig.RigModels;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public final class CrossBreedRendering {

  private CrossBreedRendering() {}

  public static Optional<CrossBreedLook> lookOf(final UnleashedDogEntity dog) {
    final DogGenome genome = dog.getGenome();
    final UnleashedDogCoat coat = dog.getCoatVariant();
    return CrossBreedLook.ofDog(
        dog.getBreed(),
        genome == null ? null : genome.composition(),
        coat == null ? -1 : coat.getOrdinal());
  }

  public static Identifier coatOf(final UnleashedDogEntity dog, final Identifier living) {
    return lookOf(dog)
        .map(
            look ->
                CrossCoatTextures.tintedOf(
                    new CrossCoat(
                        living,
                        look.rig(),
                        CoatPalettes.idOf(look.rig(), dog.getCoatVariant()),
                        CoatPalettes.pullsOf(look))))
        .orElse(living);
  }

  public static @Nullable GrowthRigScale shapeOf(
      final UnleashedDogEntity dog, final BakedGeoModel model, final RigProfile profile) {
    final Optional<CrossBreedLook> look = lookOf(dog);
    if (look.isEmpty()) {
      return null;
    }
    final List<CrossProportions.Pull> pulls = new ArrayList<>();
    for (final CrossBreedLook.Influence influence : look.get().influences()) {
      RigModels.loaded(influence.breed())
          .map(other -> RigSlotSizes.of(other, RigProfiles.of(influence.breed())))
          .ifPresent(sizes -> pulls.add(new CrossProportions.Pull(sizes, influence.weight())));
    }
    final SlotScales scales = CrossProportions.scalesOf(RigSlotSizes.of(model, profile), pulls);
    return scales.equals(SlotScales.NONE) ? null : GrowthRigScale.of(model, profile, scales);
  }

  public static Set<String> hiddenEars(
      final UnleashedDogEntity dog, final RigProfile profile, final Set<String> hiddenByHeadwear) {
    if (profile.ears().variants().isEmpty()) {
      return hiddenByHeadwear;
    }
    final Optional<String> wanted =
        lookOf(dog).flatMap(look -> RigProfiles.of(look.earSource()).ears().shape());
    final Set<String> nativeEars = new HashSet<>(profile.bonesIn(Slot.EAR_LEFT));
    nativeEars.addAll(profile.bonesIn(Slot.EAR_RIGHT));
    return profile.ears().hiddenFor(wanted, nativeEars, hiddenByHeadwear);
  }
}
