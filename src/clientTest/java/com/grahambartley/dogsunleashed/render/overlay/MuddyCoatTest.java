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

class MuddyCoatTest {

  private static final Path ASSETS = Path.of("src/client/resources/assets/dogs-unleashed");

  @ParameterizedTest(name = "a {0} dog shows mud: {1}")
  @CsvSource({"muddy, true", "clean, false", "wet, false"})
  @DisplayName("the mud overlay shows only on muddy dogs")
  void showsOnlyOnMuddyDogs(final String dog, final boolean shows) {
    final OverlayEffects<String> effects = new OverlayEffects<>();
    MuddyCoat.registerOn(effects, "muddy"::equals);
    assertEquals(shows ? List.of(MuddyCoat.SHEET) : List.<Identifier>of(), effects.activeOn(dog));
  }

  @Test
  @DisplayName("the mud sheet is the committed mud art in the shared sheet layout")
  void sheetIsTheCommittedArt() throws IOException {
    final BufferedImage sheet = ImageIO.read(ASSETS.resolve(MuddyCoat.SHEET.getPath()).toFile());
    assertEquals(OverlaySheet.WIDTH, sheet.getWidth());
    assertEquals(OverlaySheet.HEIGHT, sheet.getHeight());
  }
}
