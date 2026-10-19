package com.grahambartley.dogsunleashed.item.cosmetic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DogCosmeticTest {

  @ParameterizedTest(name = "{0} sits on the {1}")
  @CsvSource({
    "BANDANA, COLLAR",
    "COLLAR_BOW, COLLAR",
    "PARTY_HAT, HEAD",
    "STRAW_HAT, HEAD",
    "ROSETTE, COLLAR"
  })
  @DisplayName("hats sit on the head and everything else on the collar")
  void mountMatchesWhereItIsWorn(final DogCosmetic cosmetic, final DogCosmetic.Mount mount) {
    assertEquals(mount, cosmetic.mount());
  }

  @ParameterizedTest(name = "{0} dyeable={1}")
  @CsvSource({
    "BANDANA, true",
    "COLLAR_BOW, true",
    "PARTY_HAT, false",
    "STRAW_HAT, false",
    "ROSETTE, false"
  })
  @DisplayName("only bandanas and bows take dye")
  void clothTakesDye(final DogCosmetic cosmetic, final boolean dyeable) {
    assertEquals(dyeable, cosmetic.isDyeable());
  }

  @Test
  @DisplayName("an undyed bandana is red and an undyed bow is pink")
  void undyedColours() {
    assertEquals(0xB02E26, DogCosmetic.BANDANA.undyedColor());
    assertEquals(0xF38BAA, DogCosmetic.COLLAR_BOW.undyedColor());
  }

  @Test
  @DisplayName("a cosmetic that takes no dye draws in its own colours")
  void undyeableDrawsUntinted() {
    assertEquals(0xFFFFFF, DogCosmetic.PARTY_HAT.undyedColor());
    assertEquals(0xFFFFFF, DogCosmetic.STRAW_HAT.undyedColor());
    assertEquals(0xFFFFFF, DogCosmetic.ROSETTE.undyedColor());
  }

  @Test
  @DisplayName("only the bandana takes banner patterns")
  void onlyBandanaTakesPatterns() {
    assertTrue(DogCosmetic.BANDANA.takesBannerPatterns());
    assertFalse(DogCosmetic.COLLAR_BOW.takesBannerPatterns());
    assertFalse(DogCosmetic.PARTY_HAT.takesBannerPatterns());
    assertFalse(DogCosmetic.STRAW_HAT.takesBannerPatterns());
    assertFalse(DogCosmetic.ROSETTE.takesBannerPatterns());
  }

  @Test
  @DisplayName("every cosmetic has its own item id")
  void itemIdsAreUnique() {
    final Set<String> ids =
        Arrays.stream(DogCosmetic.values()).map(DogCosmetic::itemId).collect(Collectors.toSet());

    assertEquals(DogCosmetic.values().length, ids.size());
  }
}
