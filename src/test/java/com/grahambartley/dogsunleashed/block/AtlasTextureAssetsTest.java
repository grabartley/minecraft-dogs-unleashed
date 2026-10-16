package com.grahambartley.dogsunleashed.block;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AtlasTextureAssetsTest {

  private static final String TEXTURES = "assets/dogs-unleashed/textures";
  private static final List<String> ATLAS_FOLDERS = List.of("block", "item");

  static Stream<Arguments> atlasTextures() throws IOException, URISyntaxException {
    final List<Arguments> textures = new ArrayList<>();
    for (final String folder : ATLAS_FOLDERS) {
      for (final URL root :
          Collections.list(
              AtlasTextureAssetsTest.class
                  .getClassLoader()
                  .getResources(TEXTURES + "/" + folder))) {
        final Path folderPath = Path.of(root.toURI());
        try (Stream<Path> walk = Files.walk(folderPath)) {
          walk.filter(path -> path.toString().endsWith(".png"))
              .sorted()
              .forEach(
                  path ->
                      textures.add(Arguments.of(folder + "/" + folderPath.relativize(path), path)));
        }
      }
    }
    return textures.stream();
  }

  private static boolean isPowerOfTwo(final int value) {
    return value > 0 && Integer.bitCount(value) == 1;
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("atlasTextures")
  @DisplayName(
      "every texture stitched into the blocks atlas has power-of-two sides so mipmapping stays on")
  void atlasTextureHasPowerOfTwoSides(final String name, final Path file) throws IOException {
    final BufferedImage image = ImageIO.read(file.toFile());
    assertNotNull(image, name + " should be a readable PNG");
    assertTrue(
        isPowerOfTwo(image.getWidth()) && isPowerOfTwo(image.getHeight()),
        name + " is " + image.getWidth() + "x" + image.getHeight());
  }
}
