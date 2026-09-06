package com.grahambartley.dogsunleashed.render.coat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.render.coat.UndeadDogEyes.EyePixel;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class UndeadDogEyesTest {

  /**
   * Each entry's rig geo and a texture authored on it. The atlas-size assertions below exist
   * because a mismatch fails silently in production: the eye stamp is skipped and the glow samples
   * the wrong texels, which shipped as shibas with no red eyes.
   */
  private static final Map<UndeadDogEyes, String> GEO_BY_RIG =
      Map.of(
          UndeadDogEyes.BEAGLE, "beagle",
          UndeadDogEyes.GOLDEN_RETRIEVER, "goldenretriever",
          UndeadDogEyes.HUSKY, "husky",
          UndeadDogEyes.DACHSHUND, "dachshund",
          UndeadDogEyes.SHIBA_INU, "shibainu");

  private static final Map<UndeadDogEyes, List<String>> TEXTURES_BY_RIG =
      Map.of(
          UndeadDogEyes.BEAGLE, List.of("beagle_blackwhite", "beagle_tri1"),
          UndeadDogEyes.GOLDEN_RETRIEVER, List.of("goldenretriever"),
          UndeadDogEyes.HUSKY, List.of("husky_blackwhite_blueblue", "husky_graywhite_hazelhazel"),
          UndeadDogEyes.DACHSHUND, List.of("dachshund_blacktan", "dachshund_red"),
          UndeadDogEyes.SHIBA_INU, List.of("shibainu_red", "shibainu_sesame"));

  private static final int PUPIL_MAX_LUMA = 90;

  @ParameterizedTest(name = "{0} resolves to an eye table entry")
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("every rig source breed has somewhere for its eyes to glow")
  void everyBreedResolvesToAnEntry(final UnleashedDogBreed breed) {
    assertNotNull(UndeadDogEyes.of(breed));
  }

  @ParameterizedTest(name = "{0} has a matched pair of pupils inside its atlas")
  @EnumSource(UndeadDogEyes.class)
  @DisplayName("each rig carries exactly two pupils, both within the atlas")
  void eachRigCarriesTwoPupilsWithinTheAtlas(final UndeadDogEyes eyes) {
    assertEquals(2, eyes.pixels().size(), "a dog has two eyes");
    for (final EyePixel pixel : eyes.pixels()) {
      assertTrue(
          pixel.x() >= 0 && pixel.x() < eyes.atlasSize(),
          pixel + " x out of the " + eyes.atlasSize() + "px atlas");
      assertTrue(
          pixel.y() >= 0 && pixel.y() < eyes.atlasSize(),
          pixel + " y out of the " + eyes.atlasSize() + "px atlas");
    }
  }

  @ParameterizedTest(name = "{0} matches its geo's declared texture size")
  @EnumSource(UndeadDogEyes.class)
  @DisplayName("each entry's atlas size matches the rig geometry it maps")
  void atlasSizeMatchesTheRigGeometry(final UndeadDogEyes eyes) throws IOException {
    final JsonObject description =
        JsonParser.parseReader(
                new InputStreamReader(
                    resource("geo/" + GEO_BY_RIG.get(eyes) + ".geo.json"), StandardCharsets.UTF_8))
            .getAsJsonObject()
            .getAsJsonArray("minecraft:geometry")
            .get(0)
            .getAsJsonObject()
            .getAsJsonObject("description");

    assertEquals(eyes.atlasSize(), description.get("texture_width").getAsInt(), "texture_width");
    assertEquals(eyes.atlasSize(), description.get("texture_height").getAsInt(), "texture_height");
  }

  static Stream<Arguments> rigTextures() {
    return TEXTURES_BY_RIG.entrySet().stream()
        .flatMap(entry -> entry.getValue().stream().map(tex -> Arguments.of(entry.getKey(), tex)));
  }

  @ParameterizedTest(name = "{0} pupils land on dark pixels of {1}")
  @MethodSource("rigTextures")
  @DisplayName("every declared pupil lands on an actual dark eye pixel of the shipped art")
  void pupilsLandOnDarkPixelsOfTheShippedArt(final UndeadDogEyes eyes, final String texture)
      throws IOException {
    final BufferedImage image = ImageIO.read(resource("textures/entity/" + texture + ".png"));
    assertEquals(eyes.atlasSize(), image.getWidth(), texture + " width");
    assertEquals(eyes.atlasSize(), image.getHeight(), texture + " height");

    for (final EyePixel pixel : eyes.pixels()) {
      final int rgb = image.getRGB(pixel.x(), pixel.y());
      final int luma =
          (((rgb >> 16) & 0xFF) * 299 + ((rgb >> 8) & 0xFF) * 587 + (rgb & 0xFF) * 114) / 1000;
      assertTrue(
          luma <= PUPIL_MAX_LUMA,
          eyes + " pupil " + pixel + " on " + texture + " reads bright fur (luma " + luma + ")");
    }
  }

  private static InputStream resource(final String path) {
    final InputStream stream =
        UndeadDogEyesTest.class.getResourceAsStream("/assets/dogs-unleashed/" + path);
    assertNotNull(stream, "missing test asset: " + path);
    return stream;
  }

  @ParameterizedTest(name = "{0} maps to the entry for the rig it renders on")
  @MethodSource("breedRigEntries")
  @DisplayName("each breed maps to the eye table entry for its own rig")
  void breedsMapToTheirOwnRigEntry(final UnleashedDogBreed breed, final UndeadDogEyes expected) {
    assertEquals(expected, UndeadDogEyes.of(breed));
  }

  static Stream<Arguments> breedRigEntries() {
    return Stream.of(
        Arguments.of(UnleashedDogBreed.BEAGLE, UndeadDogEyes.BEAGLE),
        Arguments.of(UnleashedDogBreed.GOLDEN_RETRIEVER, UndeadDogEyes.GOLDEN_RETRIEVER),
        Arguments.of(UnleashedDogBreed.HUSKY, UndeadDogEyes.HUSKY),
        Arguments.of(UnleashedDogBreed.DACHSHUND, UndeadDogEyes.DACHSHUND),
        Arguments.of(UnleashedDogBreed.SHIBA_INU, UndeadDogEyes.SHIBA_INU),
        Arguments.of(
            UnleashedDogBreed.CROSS_BREED, UndeadDogEyes.of(UnleashedDogBreed.FALLBACK_RIG)));
  }
}
