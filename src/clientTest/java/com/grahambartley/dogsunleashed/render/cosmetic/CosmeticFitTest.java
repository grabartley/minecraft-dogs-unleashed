package com.grahambartley.dogsunleashed.render.cosmetic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmetic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class CosmeticFitTest {

  private static final float EPSILON = 1.0e-6f;

  @Test
  @DisplayName("a straw hat covers most of the head and a party hat about half")
  void headSetsTheSize() {
    assertEquals(0.85f * 4.0f, CosmeticFit.hatWidth(DogCosmetic.STRAW_HAT, 4.0f), EPSILON);
    assertEquals(0.5f * 4.0f, CosmeticFit.hatWidth(DogCosmetic.PARTY_HAT, 4.0f), EPSILON);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = DogCosmetic.class,
      names = {"COLLAR_BOW", "ROSETTE"})
  @DisplayName("a bow or rosette is narrower than a bandana on the same chest")
  void collarPieceIsNarrowerThanBandana(final DogCosmetic cosmetic) {
    assertTrue(
        CosmeticFit.collarWidth(cosmetic, 3.0f)
            < CosmeticFit.collarWidth(DogCosmetic.BANDANA, 3.0f));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = DogCosmetic.class,
      names = {"BANDANA", "COLLAR_BOW", "ROSETTE"})
  @DisplayName("collar cosmetics take no head width")
  void collarCosmeticsTakeNoHeadWidth(final DogCosmetic cosmetic) {
    assertEquals(0.0f, CosmeticFit.hatWidth(cosmetic, 4.0f), EPSILON);
  }

  @Test
  @DisplayName("collar cosmetics grow with the chest")
  void collarWidthScalesWithChest() {
    assertEquals(
        2.0f * CosmeticFit.collarWidth(DogCosmetic.BANDANA, 2.0f),
        CosmeticFit.collarWidth(DogCosmetic.BANDANA, 4.0f),
        EPSILON);
  }
}
