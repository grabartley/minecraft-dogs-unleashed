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

class SledHarnessCoatTest {

  private static final Path ASSETS = Path.of("src/client/resources/assets/dogs-unleashed");

  @ParameterizedTest(name = "a {0} dog wears the harness: {1}")
  @CsvSource({"hitched, true", "leashed, false", "free, false"})
  @DisplayName("the harness shows only while a dog is hitched to a sled")
  void showsOnlyWhileHitched(final String dog, final boolean shows) {
    final OverlayEffects<String> effects = new OverlayEffects<>();
    SledHarnessCoat.registerOn(effects, "hitched"::equals);
    assertEquals(
        shows ? List.of(SledHarnessCoat.SHEET) : List.<Identifier>of(), effects.activeOn(dog));
  }

  @Test
  @DisplayName("the harness sheet is the committed harness art in the shared sheet layout")
  void sheetIsTheCommittedArt() throws IOException {
    final BufferedImage sheet =
        ImageIO.read(ASSETS.resolve(SledHarnessCoat.SHEET.getPath()).toFile());
    assertEquals(OverlaySheet.WIDTH, sheet.getWidth());
    assertEquals(OverlaySheet.HEIGHT, sheet.getHeight());
  }
}
