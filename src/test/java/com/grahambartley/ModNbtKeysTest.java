package com.grahambartley;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ModNbtKeysTest {

  static Stream<Arguments> savedNbtKeys() {
    return Stream.of(
        Arguments.of("PET_ID", ModNbtKeys.PET_ID, "PetId"),
        Arguments.of("OWNER_ID", ModNbtKeys.OWNER_ID, "OwnerId"),
        Arguments.of("BREED_TYPE", ModNbtKeys.BREED_TYPE, "BreedType"),
        Arguments.of("NAME", ModNbtKeys.NAME, "Name"),
        Arguments.of("HEALTH", ModNbtKeys.HEALTH, "Health"),
        Arguments.of("MAX_HEALTH", ModNbtKeys.MAX_HEALTH, "MaxHealth"),
        Arguments.of("POS_X", ModNbtKeys.POS_X, "PosX"),
        Arguments.of("POS_Y", ModNbtKeys.POS_Y, "PosY"),
        Arguments.of("POS_Z", ModNbtKeys.POS_Z, "PosZ"),
        Arguments.of("DIMENSION", ModNbtKeys.DIMENSION, "Dimension"),
        Arguments.of("ALIVE", ModNbtKeys.ALIVE, "Alive"),
        Arguments.of("PORTRAIT_BABY", ModNbtKeys.PORTRAIT_BABY, "PortraitBaby"),
        Arguments.of("PORTRAIT_COLLAR", ModNbtKeys.PORTRAIT_COLLAR, "PortraitCollar"),
        Arguments.of(
            "PORTRAIT_COAT_VARIANT", ModNbtKeys.PORTRAIT_COAT_VARIANT, "portraitCoatVariant"),
        Arguments.of("PORTRAIT_HUSKY_EYE", ModNbtKeys.PORTRAIT_HUSKY_EYE, "PortraitHuskyEye"),
        Arguments.of("PETS", ModNbtKeys.PETS, "Pets"),
        Arguments.of("PETS_BY_OWNER", ModNbtKeys.PETS_BY_OWNER, "PetsByOwner"),
        Arguments.of(
            "PREFERENCES_BY_PLAYER", ModNbtKeys.PREFERENCES_BY_PLAYER, "PreferencesByPlayer"),
        Arguments.of("BREED_FILTER", ModNbtKeys.BREED_FILTER, "BreedFilter"),
        Arguments.of("ALIVE_FILTER", ModNbtKeys.ALIVE_FILTER, "AliveFilter"),
        Arguments.of("COAT_VARIANT", ModNbtKeys.COAT_VARIANT, "CoatVariant"),
        Arguments.of("EYE_COLOR_VARIANT", ModNbtKeys.EYE_COLOR_VARIANT, "EyeColorVariant"),
        Arguments.of("COLLAR_COLOR", ModNbtKeys.COLLAR_COLOR, "CollarColor"),
        Arguments.of("SHAKE_PROGRESS", ModNbtKeys.SHAKE_PROGRESS, "ShakeProgress"),
        Arguments.of("WAS_IN_WATER", ModNbtKeys.WAS_IN_WATER, "WasInWater"),
        Arguments.of(
            "TICKS_SINCE_LEFT_WATER", ModNbtKeys.TICKS_SINCE_LEFT_WATER, "TicksSinceLeftWater"),
        Arguments.of("SLEEPING_IN_BED", ModNbtKeys.SLEEPING_IN_BED, "SleepingInBed"),
        Arguments.of("BED_POS_X", ModNbtKeys.BED_POS_X, "BedPosX"),
        Arguments.of("BED_POS_Y", ModNbtKeys.BED_POS_Y, "BedPosY"),
        Arguments.of("BED_POS_Z", ModNbtKeys.BED_POS_Z, "BedPosZ"),
        Arguments.of("CARRYING_BALL", ModNbtKeys.CARRYING_BALL, "CarryingBall"),
        Arguments.of("ACTIVE_FETCH_TYPE_ID", ModNbtKeys.ACTIVE_FETCH_TYPE_ID, "ActiveFetchTypeId"),
        Arguments.of(
            "CARRIED_FETCH_ITEM_STACK",
            ModNbtKeys.CARRIED_FETCH_ITEM_STACK,
            "CarriedFetchItemStack"));
  }

  @ParameterizedTest(name = "{0} -> \"{2}\"")
  @MethodSource("savedNbtKeys")
  @DisplayName("persisted NBT keys keep their on-disk strings stable for save compatibility")
  void persistedNbtKeysAreStable(
      final String constantName, final String actual, final String expected) {
    assertEquals(expected, actual, constantName);
  }
}
