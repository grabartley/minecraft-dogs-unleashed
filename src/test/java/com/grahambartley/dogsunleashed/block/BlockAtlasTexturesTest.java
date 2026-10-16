package com.grahambartley.dogsunleashed.block;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class BlockAtlasTexturesTest {

  private static final String TEXTURES = "/assets/dogs-unleashed/textures";

  static Stream<Path> atlasTextures() throws Exception {
    final List<Path> files;
    try (Stream<Path> walk =
        Stream.of("block", "item")
            .map(BlockAtlasTexturesTest::texturePath)
            .filter(Files::isDirectory)
            .flatMap(BlockAtlasTexturesTest::walk)) {
      files = walk.filter(path -> path.toString().endsWith(".png")).sorted().toList();
    }
    return files.stream();
  }

  private static Path texturePath(final String folder) {
    try {
      return Path.of(BlockAtlasTexturesTest.class.getResource(TEXTURES + "/" + folder).toURI());
    } catch (Exception ex) {
      throw new IllegalStateException("cannot find textures/" + folder, ex);
    }
  }

  private static Stream<Path> walk(final Path folder) {
    try {
      return Files.walk(folder);
    } catch (IOException ex) {
      throw new IllegalStateException("cannot list " + folder, ex);
    }
  }

  private static boolean isPowerOfTwo(final int value) {
    return value > 0 && Integer.bitCount(value) == 1;
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("atlasTextures")
  @DisplayName(
      "every texture stitched into the blocks atlas has power-of-two sides so mipmapping stays on")
  void atlasTextureHasPowerOfTwoSides(final Path file) throws IOException {
    final BufferedImage image = ImageIO.read(file.toFile());
    assertNotNull(image, file.getFileName() + " should be a readable PNG");
    assertTrue(
        isPowerOfTwo(image.getWidth()) && isPowerOfTwo(image.getHeight()),
        file.getFileName() + " is " + image.getWidth() + "x" + image.getHeight());
  }
}
