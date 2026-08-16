package com.grahambartley.dogsunleashed.entity.rig;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Per-bone adjustments that give each breed its silhouette on the shared skeleton, and the
 * composition-weighted blend of those adjustments for a cross-breed.
 *
 * <p>Offsets are in model units against the shared rig, which is built at golden retriever
 * proportions. The leg bones hang off {@code chest} and {@code Torso}, so an offset applied to the
 * body carries them with it; the leg entries cancel that back out to leave the body sitting lower
 * between legs that stay where they were.
 */
public record DogProportions(Map<String, BoneAdjustment> bones, float adultScale, float babyScale) {

  public record BoneAdjustment(
      float offsetX, float offsetY, float offsetZ, float scaleX, float scaleY, float scaleZ) {

    public static final BoneAdjustment NONE = new BoneAdjustment(0, 0, 0, 1, 1, 1);

    public boolean isIdentity() {
      return this.equals(NONE);
    }
  }

  private static final Map<UnleashedDogBreed, DogProportions> BY_BREED =
      new EnumMap<>(UnleashedDogBreed.class);

  static {
    BY_BREED.put(UnleashedDogBreed.GOLDEN_RETRIEVER, new DogProportions(Map.of(), 1.7f, 0.85f));
    // Per-bone shape values are calibrated against how the breed rendered on its own rig, not
    // derived from pivot differences: the rig is nested, so a body offset carries its whole
    // subtree and a pivot delta does not translate into an offset one for one. Both breeds
    // currently differ by overall size alone; real per-bone shape values are a tuning pass with
    // the artist, and the blending below is ready for them.
    BY_BREED.put(UnleashedDogBreed.BEAGLE, new DogProportions(Map.of(), 1.5f, 0.75f));
  }

  public static DogProportions of(final UnleashedDogBreed breed) {
    return BY_BREED.getOrDefault(breed, new DogProportions(Map.of(), 1.5f, 0.75f));
  }

  public BoneAdjustment forBone(final String boneName) {
    return this.bones.getOrDefault(boneName, BoneAdjustment.NONE);
  }

  /**
   * Blends each ancestor's adjustments by its share of the composition, so a cross lands between
   * its ancestors rather than snapping to whichever one dominates.
   */
  public static DogProportions blend(final List<BreedShare> composition) {
    final List<BreedShare> weighted = normalised(composition);
    if (weighted.isEmpty()) {
      return of(UnleashedDogBreed.CROSS_BREED);
    }
    if (weighted.size() == 1) {
      return of(weighted.get(0).breed());
    }

    final Set<String> boneNames = new HashSet<>();
    for (final BreedShare share : weighted) {
      boneNames.addAll(of(share.breed()).bones().keySet());
    }

    final Map<String, BoneAdjustment> blended = new LinkedHashMap<>();
    for (final String boneName : boneNames) {
      float offsetX = 0;
      float offsetY = 0;
      float offsetZ = 0;
      float scaleX = 0;
      float scaleY = 0;
      float scaleZ = 0;
      for (final BreedShare share : weighted) {
        final BoneAdjustment adjustment = of(share.breed()).forBone(boneName);
        offsetX += adjustment.offsetX() * share.share();
        offsetY += adjustment.offsetY() * share.share();
        offsetZ += adjustment.offsetZ() * share.share();
        scaleX += adjustment.scaleX() * share.share();
        scaleY += adjustment.scaleY() * share.share();
        scaleZ += adjustment.scaleZ() * share.share();
      }
      final BoneAdjustment adjustment =
          new BoneAdjustment(offsetX, offsetY, offsetZ, scaleX, scaleY, scaleZ);
      if (!adjustment.isIdentity()) {
        blended.put(boneName, adjustment);
      }
    }

    float adultScale = 0;
    float babyScale = 0;
    for (final BreedShare share : weighted) {
      adultScale += of(share.breed()).adultScale() * share.share();
      babyScale += of(share.breed()).babyScale() * share.share();
    }
    return new DogProportions(Map.copyOf(blended), adultScale, babyScale);
  }

  private static List<BreedShare> normalised(final List<BreedShare> composition) {
    float total = 0;
    for (final BreedShare share : composition) {
      if (share.share() > 0) {
        total += share.share();
      }
    }
    if (total <= 0) {
      return List.of();
    }
    final List<BreedShare> normalised = new ArrayList<>(composition.size());
    for (final BreedShare share : composition) {
      if (share.share() > 0) {
        normalised.add(new BreedShare(share.breed(), share.share() / total));
      }
    }
    return normalised;
  }
}
