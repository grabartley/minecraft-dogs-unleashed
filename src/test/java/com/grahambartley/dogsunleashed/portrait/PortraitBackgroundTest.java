package com.grahambartley.dogsunleashed.portrait;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class PortraitBackgroundTest {

  @ParameterizedTest(name = "{0} sits in front of {1}")
  @CsvSource({
    "HUSKY, SNOW",
    "DACHSHUND, PLAINS",
    "BEAGLE, FOREST",
    "GOLDEN_RETRIEVER, BEACH",
    "SHIBA_INU, CHERRY_GROVE",
    "PINSCHER, PLAINS",
    "DIRE_WOLF, PEAKS",
    "CROSS_BREED, PLAINS"
  })
  @DisplayName("each breed is painted in front of the biome it comes from")
  void breedPicksItsHomeBiome(final UnleashedDogBreed breed, final PortraitBackground background) {
    assertEquals(background, PortraitBackground.of(breed));
  }

  static Stream<Arguments> backgroundsAndSizes() {
    return Stream.of(PortraitBackground.values())
        .flatMap(
            background ->
                Stream.of(PortraitSize.values()).map(size -> Arguments.of(background, size)));
  }

  @ParameterizedTest(name = "{0} at {1}")
  @MethodSource("backgroundsAndSizes")
  @DisplayName("every background and size points at a shipped texture")
  void texturePathIsShipped(final PortraitBackground background, final PortraitSize size) {
    final String path = background.texturePath(size);
    assertEquals(
        "textures/portrait/background/" + background.id() + "_" + size.serializedName() + ".png",
        path);
    assertNotNull(PortraitBackgroundTest.class.getResource("/assets/dogs-unleashed/" + path), path);
  }
}
