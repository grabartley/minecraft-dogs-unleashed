package com.grahambartley.dogsunleashed.render.cosmetic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmetic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class CosmeticFitTest {

  private static final float HEAD_WIDTH = 8.0f;
  private static final float CHEST_WIDTH = 10.0f;

  @ParameterizedTest(name = "{0} on an 8 px head is {1} px wide")
  @CsvSource({
    "STRAW_HAT, 12.8",
    "PARTY_HAT, 4.0",
    "BANDANA, 0.0",
    "COLLAR_BOW, 0.0",
    "ROSETTE, 0.0"
  })
  @DisplayName(
      "hats take their width from the head, and the straw hat brim clears it on both sides")
  void hatWidthFollowsTheHead(final DogCosmetic cosmetic, final float width) {
    assertEquals(width, CosmeticFit.hatWidth(cosmetic, HEAD_WIDTH), 1.0e-5f);
  }

  @ParameterizedTest(name = "{0} on a 10 px chest is {1} px wide")
  @CsvSource({
    "BANDANA, 9.0",
    "COLLAR_BOW, 6.0",
    "ROSETTE, 5.5",
    "PARTY_HAT, 0.0",
    "STRAW_HAT, 0.0"
  })
  @DisplayName("collar pieces take their width from the chest")
  void collarWidthFollowsTheChest(final DogCosmetic cosmetic, final float width) {
    assertEquals(width, CosmeticFit.collarWidth(cosmetic, CHEST_WIDTH), 1.0e-5f);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogCosmetic.class)
  @DisplayName("every cosmetic is sized from exactly one of the head or the chest")
  void everyCosmeticIsSizedFromOneMount(final DogCosmetic cosmetic) {
    final boolean fromHead = CosmeticFit.headShare(cosmetic) > 0.0f;
    final boolean fromChest = CosmeticFit.chestShare(cosmetic) > 0.0f;
    assertTrue(fromHead != fromChest, cosmetic + " must be sized from one mount");
    assertEquals(cosmetic.mount() == DogCosmetic.Mount.HEAD, fromHead);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = DogCosmetic.class,
      names = {"COLLAR_BOW", "ROSETTE"})
  @DisplayName("a bow or rosette is narrower than a bandana on the same chest")
  void collarPieceIsNarrowerThanBandana(final DogCosmetic cosmetic) {
    assertTrue(
        CosmeticFit.collarWidth(cosmetic, CHEST_WIDTH)
            < CosmeticFit.collarWidth(DogCosmetic.BANDANA, CHEST_WIDTH));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogCosmetic.class)
  @DisplayName("every cosmetic grows in step with what it sits on")
  void widthScalesWithTheBody(final DogCosmetic cosmetic) {
    assertEquals(
        2.0f * CosmeticFit.hatWidth(cosmetic, HEAD_WIDTH),
        CosmeticFit.hatWidth(cosmetic, 2.0f * HEAD_WIDTH),
        1.0e-5f);
    assertEquals(
        2.0f * CosmeticFit.collarWidth(cosmetic, CHEST_WIDTH),
        CosmeticFit.collarWidth(cosmetic, 2.0f * CHEST_WIDTH),
        1.0e-5f);
  }
}
