package com.grahambartley.dogsunleashed.render.overlay;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SeniorMuzzleTest {

  private static final Path ASSETS = Path.of("src/client/resources/assets/dogs-unleashed");

  @ParameterizedTest(name = "a {0} dog shows the muzzle: {1}")
  @CsvSource({"senior, true", "adult, false", "puppy, false"})
  @DisplayName("the grey muzzle shows only on senior dogs")
  void showsOnlyOnSeniors(final String dog, final boolean shows) {
    final OverlayEffects<String> effects = new OverlayEffects<>();
    SeniorMuzzle.registerOn(effects, "senior"::equals);
    assertEquals(
        shows ? List.of(SeniorMuzzle.SHEET) : List.<Identifier>of(), effects.activeOn(dog));
  }

  @Test
  @DisplayName("the muzzle sheet is the committed grey muzzle art in the shared sheet layout")
  void sheetIsTheCommittedArt() throws IOException {
    final BufferedImage sheet = ImageIO.read(ASSETS.resolve(SeniorMuzzle.SHEET.getPath()).toFile());
    assertEquals(OverlaySheet.WIDTH, sheet.getWidth());
    assertEquals(OverlaySheet.HEIGHT, sheet.getHeight());
  }
}
