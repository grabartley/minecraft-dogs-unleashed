package com.grahambartley.dogsunleashed.render.cross;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.cross.CoatPalettes.PaletteWeight;
import com.grahambartley.dogsunleashed.render.cross.CrossCoatTextureCache.CrossCoat;
import java.util.List;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CrossCoatTexturesTest {

  private static final Identifier LIVING =
      Identifier.of("dogs-unleashed", "textures/entity/beagle_tri1.png");
  private static final Identifier OWN =
      Identifier.of("dogs-unleashed", "textures/entity/palette/beagle_tri1.png");
  private static final Identifier HUSKY =
      Identifier.of("dogs-unleashed", "textures/entity/palette/husky_agouti.png");
  private static final Identifier SHIBA =
      Identifier.of("dogs-unleashed", "textures/entity/palette/shibainu_red.png");

  private static CrossCoat coat(final PaletteWeight... pulls) {
    return new CrossCoat(LIVING, UnleashedDogBreed.BEAGLE, OWN, List.of(pulls));
  }

  @Test
  @DisplayName("a painted coat is named after its living coat and every palette and share")
  void paintedCoatNamesItsInputs() {
    assertEquals(
        Identifier.of(
            "dogs-unleashed", "cross/textures/entity/beagle_tri1/husky_agouti_25/shibainu_red_13"),
        CrossCoatTextures.idOf(coat(new PaletteWeight(HUSKY, 25), new PaletteWeight(SHIBA, 13))));
  }

  @ParameterizedTest(name = "{0} and {1} percent")
  @CsvSource({"25, 50", "50, 49", "13, 87"})
  @DisplayName("two crosses with different shares get different textures")
  void differentSharesGetDifferentTextures(final int first, final int second) {
    assertNotEquals(
        CrossCoatTextures.idOf(coat(new PaletteWeight(HUSKY, first))),
        CrossCoatTextures.idOf(coat(new PaletteWeight(HUSKY, second))));
  }
}
