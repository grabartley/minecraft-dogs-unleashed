package com.grahambartley.dogsunleashed.entity;

import java.util.Set;

public final class DogMudGround {

  private static final Set<String> MUD = Set.of("minecraft:mud", "minecraft:muddy_mangrove_roots");
  private static final Set<String> SOFT_IN_RAIN =
      Set.of(
          "minecraft:dirt",
          "minecraft:coarse_dirt",
          "minecraft:rooted_dirt",
          "minecraft:grass_block",
          "minecraft:podzol",
          "minecraft:farmland");

  private DogMudGround() {}

  public static boolean isMud(final String blockId) {
    return MUD.contains(blockId);
  }

  public static boolean turnsMuddyInRain(final String blockId) {
    return SOFT_IN_RAIN.contains(blockId);
  }

  public static boolean dirties(
      final String feet, final String ground, final boolean inSwampWater, final boolean raining) {
    return isMud(feet) || isMud(ground) || inSwampWater || (raining && turnsMuddyInRain(ground));
  }
}
