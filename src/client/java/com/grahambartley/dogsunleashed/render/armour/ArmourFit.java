package com.grahambartley.dogsunleashed.render.armour;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.Map;
import java.util.Set;

public final class ArmourFit {

  private static final int ALL_CUBES = -1;

  private static final Map<UnleashedDogBreed, Map<String, Integer>> TORSO =
      Map.of(
          UnleashedDogBreed.HUSKY,
          Map.of("main", ALL_CUBES, "frontchest", ALL_CUBES, "belly", ALL_CUBES),
          UnleashedDogBreed.BEAGLE,
          Map.of("Torso", ALL_CUBES, "bone", ALL_CUBES),
          UnleashedDogBreed.DACHSHUND,
          Map.of("torso", ALL_CUBES, "bunda", ALL_CUBES),
          UnleashedDogBreed.SHIBA_INU,
          Map.of("Torso", ALL_CUBES, "bone", ALL_CUBES),
          UnleashedDogBreed.GOLDEN_RETRIEVER,
          Map.of("Torso", ALL_CUBES, "bone", 0));

  private ArmourFit() {}

  public static Set<String> torsoBones(final UnleashedDogBreed breed) {
    return TORSO.getOrDefault(breed, Map.of()).keySet();
  }

  public static boolean covers(
      final UnleashedDogBreed breed, final String bone, final int cubeIndex) {
    final Integer only = TORSO.getOrDefault(breed, Map.of()).get(bone);
    return only != null && (only == ALL_CUBES || only == cubeIndex);
  }
}
