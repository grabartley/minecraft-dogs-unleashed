package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DogMudGroundTest {

  @ParameterizedTest(name = "{0}: mud={1}, muddy in rain={2}")
  @CsvSource({
    "minecraft:mud, true, false",
    "minecraft:muddy_mangrove_roots, true, false",
    "minecraft:dirt, false, true",
    "minecraft:coarse_dirt, false, true",
    "minecraft:rooted_dirt, false, true",
    "minecraft:grass_block, false, true",
    "minecraft:podzol, false, true",
    "minecraft:farmland, false, true",
    "minecraft:stone, false, false",
    "minecraft:sand, false, false",
    "minecraft:packed_mud, false, false",
    "minecraft:mud_bricks, false, false",
    "minecraft:snow_block, false, false"
  })
  @DisplayName("mud is always dirty and soft ground only turns muddy in rain")
  void classifiesGround(final String block, final boolean mud, final boolean softInRain) {
    assertEquals(mud, DogMudGround.isMud(block));
    assertEquals(softInRain, DogMudGround.turnsMuddyInRain(block));
  }

  @ParameterizedTest(name = "feet={0}, ground={1}, swamp water={2}, raining={3} -> {4}")
  @CsvSource({
    "minecraft:air, minecraft:mud, false, false, true",
    "minecraft:mud, minecraft:stone, false, false, true",
    "minecraft:air, minecraft:grass_block, false, false, false",
    "minecraft:air, minecraft:grass_block, false, true, true",
    "minecraft:air, minecraft:farmland, false, true, true",
    "minecraft:air, minecraft:stone, false, true, false",
    "minecraft:water, minecraft:stone, true, false, true",
    "minecraft:air, minecraft:sand, false, false, false"
  })
  @DisplayName("a dog is dirtied by mud, swamp water, or rain on soft ground")
  void dirties(
      final String feet,
      final String ground,
      final boolean swamp,
      final boolean raining,
      final boolean expected) {
    assertEquals(expected, DogMudGround.dirties(feet, ground, swamp, raining));
  }
}
