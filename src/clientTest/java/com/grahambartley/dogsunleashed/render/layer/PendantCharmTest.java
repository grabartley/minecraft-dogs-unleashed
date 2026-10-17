package com.grahambartley.dogsunleashed.render.layer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.item.pendant.PendantFamily;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class PendantCharmTest {

  private static final Path ASSETS = Path.of("src/client/resources/assets/dogs-unleashed");

  static Stream<Arguments> families() {
    return Stream.of(
        Arguments.of(PendantFamily.WARDING, PendantCharm.WARD),
        Arguments.of(PendantFamily.GUARDIAN, PendantCharm.GUARDIAN),
        Arguments.of(PendantFamily.VITALITY, PendantCharm.VITALITY),
        Arguments.of(PendantFamily.LOYALTY, PendantCharm.LOYALTY));
  }

  @ParameterizedTest(name = "{0} wears the {1} charm")
  @MethodSource("families")
  void eachFamilyHasItsCharm(final PendantFamily family, final PendantCharm charm) {
    assertEquals(charm, PendantCharm.of(family));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(PendantCharm.class)
  void everyCharmShipsItsModelAndTexture(final PendantCharm charm) {
    assertTrue(
        Files.isRegularFile(ASSETS.resolve(charm.model().getPath())), charm.model().toString());
    assertTrue(
        Files.isRegularFile(ASSETS.resolve(charm.texture().getPath())), charm.texture().toString());
  }
}
