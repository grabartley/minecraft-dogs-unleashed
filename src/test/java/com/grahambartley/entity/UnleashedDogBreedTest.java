package com.grahambartley.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class UnleashedDogBreedTest {

  @Test
  @DisplayName("Breed parser should support canonical ids and legacy aliases")
  void testBreedAliases() {
    assertEquals(UnleashedDogBreed.HUSKY, UnleashedDogBreed.fromSerializedId("husky"));
    assertEquals(
        UnleashedDogBreed.GOLDEN_RETRIEVER, UnleashedDogBreed.fromSerializedId("goldenretriever"));
    assertEquals(
        UnleashedDogBreed.GOLDEN_RETRIEVER, UnleashedDogBreed.fromSerializedId("golden_retriever"));
    assertEquals(UnleashedDogBreed.SHIBA_INU, UnleashedDogBreed.fromSerializedId("shibainu"));
    assertEquals(UnleashedDogBreed.SHIBA_INU, UnleashedDogBreed.fromSerializedId("shiba_inu"));
  }

  @Test
  @DisplayName("Unknown breed ids should safely fall back or return null")
  void testUnknownBreedFallback() {
    assertEquals(UnleashedDogBreed.HUSKY, UnleashedDogBreed.fromSerializedId("???"));
    assertNull(UnleashedDogBreed.fromSerializedIdOrNull("???"));
    assertNull(UnleashedDogBreed.fromSerializedIdOrNull(null));
  }

  @ParameterizedTest
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("every breed declares a non-null carry profile for tennis ball, stick, and frisbee")
  void everyBreedDeclaresProfilesForEveryFetchItem(UnleashedDogBreed breed) {
    FetchCarryProfiles profiles = breed.fetchCarryProfiles();
    assertNotNull(profiles, "fetchCarryProfiles for " + breed);
    assertNotNull(profiles.tennisBall(), "tennis ball profile for " + breed);
    assertNotNull(profiles.stick(), "stick profile for " + breed);
    assertNotNull(profiles.frisbee(), "frisbee profile for " + breed);
  }

  @ParameterizedTest
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("every carry profile has a positive scale and finite offsets")
  void everyCarryProfileIsWellFormed(UnleashedDogBreed breed) {
    FetchCarryProfiles profiles = breed.fetchCarryProfiles();
    for (CarryProfile profile :
        List.of(profiles.tennisBall(), profiles.stick(), profiles.frisbee())) {
      assertTrue(profile.scale() > 0f, "scale must be positive: " + breed + " " + profile);
      assertTrue(
          Double.isFinite(profile.verticalOffset()),
          "vertical offset must be finite: " + breed + " " + profile);
      assertTrue(
          Double.isFinite(profile.forwardOffset()),
          "forward offset must be finite: " + breed + " " + profile);
    }
  }

  @ParameterizedTest
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName(
      "frisbee carry scale exceeds tennis ball carry scale to compensate for the smaller "
          + "frisbee item GROUND scale")
  void frisbeeCarryScaleExceedsTennisBallScale(UnleashedDogBreed breed) {
    FetchCarryProfiles profiles = breed.fetchCarryProfiles();
    assertTrue(
        profiles.frisbee().scale() > profiles.tennisBall().scale(),
        "frisbee carry scale must exceed tennis ball carry scale for breed="
            + breed
            + ": frisbee="
            + profiles.frisbee().scale()
            + " tennisBall="
            + profiles.tennisBall().scale());
  }
}
