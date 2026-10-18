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
  @DisplayName("on an open head a straw hat covers most of it and a party hat about half")
  void openHeadSetsTheSize() {
    assertEquals(0.85f * 4.0f, CosmeticFit.hatWidth(DogCosmetic.STRAW_HAT, 4.0f, 10.0f), EPSILON);
    assertEquals(0.5f * 4.0f, CosmeticFit.hatWidth(DogCosmetic.PARTY_HAT, 4.0f, 10.0f), EPSILON);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = DogCosmetic.class,
      names = {"PARTY_HAT", "STRAW_HAT"})
  @DisplayName("a hat never grows past the clear patch between the ears")
  void earsCapTheSize(final DogCosmetic hat) {
    final float width = CosmeticFit.hatWidth(hat, 4.0f, 1.0f);

    assertTrue(width < 1.0f, hat + " was " + width);
  }

  @Test
  @DisplayName("a bow is narrower than a bandana on the same chest")
  void bowIsNarrowerThanBandana() {
    assertTrue(
        CosmeticFit.collarWidth(DogCosmetic.COLLAR_BOW, 3.0f)
            < CosmeticFit.collarWidth(DogCosmetic.BANDANA, 3.0f));
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
